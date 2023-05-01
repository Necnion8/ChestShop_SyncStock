package com.gmail.necnionch.myplugin.cssyncstock.bukkit;

import com.Acrobot.Breeze.Utils.*;
import com.Acrobot.ChestShop.Events.PreTransactionEvent;
import com.Acrobot.ChestShop.Events.TransactionEvent;
import com.Acrobot.ChestShop.Signs.ChestShopSign;
import com.Acrobot.ChestShop.Utils.uBlock;
import com.gmail.necnionch.myplugin.cssyncstock.bukkit.events.CSSyncStockPriceUpdateEvent;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.Acrobot.ChestShop.Utils.uBlock.SHOP_FACES;

public class SyncStockSign {
    private Sign sign;
    private Float extraValue;

    private SyncStockSign(Sign sign, Float extraValue) {
        this.sign = sign;
        this.extraValue = extraValue;
    }


    public Sign getSign() {
        return sign;
    }

    public Float getExtraValue() {
        return extraValue;
    }

    public void setExtraValue(Float value) {
        PersistentDataContainer container = sign.getPersistentDataContainer();
        if (value != null) {
            container.set(SyncStockPlugin.getExtraValueKey(), PersistentDataType.FLOAT, value);
        } else {
            container.remove(SyncStockPlugin.getExtraValueKey());
        }
        sign.update();
    }

    public boolean checkValid() {
        return uBlock.findConnectedContainer(sign) != null;
    }

    public void refresh() {
        Block block = sign.getLocation().getBlock();
        if (!(block.getState() instanceof Sign))
            throw new IllegalStateException("no sign");

        sign = ((Sign) block.getState());
        PersistentDataContainer container = sign.getPersistentDataContainer();
        extraValue = container.get(SyncStockPlugin.getExtraValueKey(), PersistentDataType.FLOAT);
    }

    public void updatePrice() {
        updatePrice(false);
    }

    public void updatePrice(boolean refresh) {
        Container container = uBlock.findConnectedContainer(sign);
        if (container == null)
            throw new IllegalArgumentException("no container");

        if (refresh)
            refresh();

        int amount = QuantityUtil.parseQuantity(sign.getLine(1));

        double buyPrice = calcCost(extraValue, container, TransactionEvent.TransactionType.BUY, amount);
        double sellPrice = calcCost(extraValue, container, TransactionEvent.TransactionType.SELL, amount);
        setPriceLine(sign, buyPrice, sellPrice);

        SyncStockPlugin.getInstance().getServer().getPluginManager().callEvent(
                new CSSyncStockPriceUpdateEvent(this, container, container.getLocation(), buyPrice, sellPrice)
        );
    }


    private void processShop(PreTransactionEvent event, Container container) {
        Economy economy = SyncStockPlugin.getInstance().getEconomy();
        double balance = economy.getBalance(event.getClient(), event.getClient().getWorld().getName());

        int max = container.getInventory().getSize() * container.getInventory().getMaxStackSize();
        int items = Arrays.stream(container.getInventory().getContents())
                .filter(Objects::nonNull)
                .mapToInt(ItemStack::getAmount)
                .sum();

        // （X%←看板設定時に入力）×（チェストの最大容量-入ってるアイテムの個数）

        int amount = Arrays.stream(event.getStock()).mapToInt(ItemStack::getAmount).sum();
        int amountCount = amount;
        double newPrice = 0;

        if (TransactionEvent.TransactionType.BUY.equals(event.getTransactionType())) {
            double tmpValue;
            while (amountCount-- > 0) {
                tmpValue = (double) extraValue * Math.max(0, max - items--);
                if (newPrice + tmpValue > balance)
                    break;
                newPrice += tmpValue;
                if (items <= 0)
                    break;
//                System.out.println("price " + (amount - amountCount) + " : " + tmpValue + " : " + newPrice);
            }

        } else if (TransactionEvent.TransactionType.SELL.equals(event.getTransactionType())) {
            double tmpValue;
            while (amountCount-- > 0) {
                tmpValue = (double) extraValue * Math.max(0, max - ++items);
                newPrice += tmpValue;
//                System.out.println("price " + (amount - amountCount) + " : " + tmpValue + " : " + newPrice);
            }
        }

        if (amountCount > 0) {
            // changes item amounts
            ItemStack itemStack = event.getStock()[0];
            itemStack.setAmount(amount - amountCount);
            event.setStock(InventoryUtil.getItemsStacked(itemStack));
//            System.out.println("Change Amount " + amount + " TO " + (amount - amountCount));
        }
//        System.out.println("Amount " + event.getExactPrice().doubleValue() + " TO " + newPrice);
        event.setExactPrice(BigDecimal.valueOf(newPrice));

    }

    private double calcCost(float extra, Container container, TransactionEvent.TransactionType transactionType, int amount) {
        // （X%←看板設定時に入力）×（チェストの最大容量-入ってるアイテムの個数）

        int max = container.getInventory().getSize() * container.getInventory().getMaxStackSize();
        int items = Arrays.stream(container.getInventory().getContents())
                .filter(Objects::nonNull)
                .mapToInt(ItemStack::getAmount)
                .sum();
        int newCost = 0;
        double tmpValue;

        if (TransactionEvent.TransactionType.BUY.equals(transactionType)) {
            while (amount-- > 0) {
                newCost += (double) extra * Math.max(0, max - items--);
            }
        } else if (TransactionEvent.TransactionType.SELL.equals(transactionType)) {
            while (amount-- > 0) {
                tmpValue = (double) extra * Math.max(0, max - ++items);
                newCost += tmpValue;
            }
        } else {
            throw new UnsupportedOperationException("not implemented transaction type: " + transactionType.name());
        }

        return Math.max(0, newCost);
    }


    private static void setPriceLine(Sign sign, Double buy, Double sell) {
        StringBuilder line = new StringBuilder();

        if (sell != null)
            sell = (double) Math.round(Math.max(0, sell) * 100) / 100;

        if (buy != null) {
            buy = (double) Math.round(Math.max(0, buy) * 100) / 100;

            line.append("B ").append(formatPriceText(buy));
            if (sell != null) {
                line.append(" : ").append(formatPriceText(sell)).append(" S");
            }
        } else if (sell != null) {
            line.append("S ").append(formatPriceText(sell));
        } else {
            return;
        }

        sign.setLine(ChestShopSign.PRICE_LINE, line.toString());
        sign.update();
    }

    private static String formatPriceText(Double price) {
        if (price == 0)
            return PriceUtil.FREE_TEXT;

        String tmp = String.valueOf(Math.round(price * 100) / 100d);
        if (tmp.endsWith(".0"))
            tmp = tmp.substring(0, tmp.length() - 2);
        return tmp;
    }

    public static Set<Sign> findAnyNearbyShopSign(Block block) {
        return Arrays.stream(SHOP_FACES)
                .map(block::getRelative)
                .filter(BlockUtil::isSign)
                .map(fBlock -> (Sign) ImplementationAdapter.getState(fBlock, false))
                .filter(ChestShopSign::isValid)
                .collect(Collectors.toSet());
    }


    public static boolean checkValid(Sign sign) {
        return uBlock.findConnectedContainer(sign) != null;
    }

    public static boolean has(Sign sign) {
        PersistentDataContainer container = sign.getPersistentDataContainer();
        return container.has(SyncStockPlugin.getExtraValueKey(), PersistentDataType.FLOAT);
    }

    public static boolean clean(Sign sign) {
        PersistentDataContainer container = sign.getPersistentDataContainer();
        if (container.has(SyncStockPlugin.getExtraValueKey(), PersistentDataType.FLOAT)) {
            container.remove(SyncStockPlugin.getExtraValueKey());
            sign.update();
            return true;
        }
        return false;
    }

    public static SyncStockSign from(Sign sign) {
        PersistentDataContainer container = sign.getPersistentDataContainer();
        Float extraValue = container.get(SyncStockPlugin.getExtraValueKey(), PersistentDataType.FLOAT);

        if (extraValue == null)
            return null;
        return new SyncStockSign(sign, extraValue);
    }

    public static SyncStockSign from(Sign sign, float extraValue) {
        SyncStockSign syncStockSign = new SyncStockSign(sign, extraValue);
        syncStockSign.setExtraValue(extraValue);
        return syncStockSign;
    }

    public void onEvent(PreTransactionEvent event) {
        Container container = uBlock.findConnectedContainer(sign);
        if (container == null)
            return;

        processShop(event, container);

    }


    private void debug(PreTransactionEvent event) {
        System.out.println("check type : " + event.getTransactionType().name());
        int amount = 0;
        for (ItemStack itemStack : event.getStock()) {
            amount += itemStack.getAmount();
            System.out.println("type : " + itemStack.getType().name());
        }
        System.out.println("amount : " + amount);
        System.out.println("price : " + event.getExactPrice().doubleValue());

    }


    private Predicate<Double> priceChecker(Player player) {
        return value -> {
            Economy economy = SyncStockPlugin.getInstance().getEconomy();
            double balance = economy.getBalance(player);
            System.out.println("bal : " + balance);
            return economy.has(player, player.getWorld().getName(), -value);
        };
    }


}
