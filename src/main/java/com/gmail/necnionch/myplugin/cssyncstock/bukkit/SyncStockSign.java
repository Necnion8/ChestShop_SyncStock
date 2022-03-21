package com.gmail.necnionch.myplugin.cssyncstock.bukkit;

import com.Acrobot.Breeze.Utils.PriceUtil;
import com.Acrobot.Breeze.Utils.QuantityUtil;
import com.Acrobot.ChestShop.Events.PreTransactionEvent;
import com.Acrobot.ChestShop.Events.TransactionEvent;
import com.Acrobot.ChestShop.Signs.ChestShopSign;
import com.Acrobot.ChestShop.Utils.uBlock;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.block.Sign;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Objects;

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
    }

    private double calcCost(float extra, Container container, TransactionEvent.TransactionType transactionType, int amount) {
        // （X%←看板設定時に入力）×（チェストの最大容量-入ってるアイテムの個数）

        int max = container.getInventory().getSize() * container.getInventory().getMaxStackSize();
        int items = Arrays.stream(container.getInventory().getContents())
                .filter(Objects::nonNull)
                .mapToInt(ItemStack::getAmount)
                .sum();
        int newCost = 0;

        if (TransactionEvent.TransactionType.BUY.equals(transactionType)) {
            while (amount >= 0) {
                newCost += (double) extra * Math.max(0, max - (items - amount--));
            }
        } else if (TransactionEvent.TransactionType.SELL.equals(transactionType)) {
            while (amount >= 0) {
                newCost += (double) extra * Math.max(0, max - (items + amount--));
            }
        } else {
            throw new UnsupportedOperationException("not implemented transaction type: " + transactionType.name());
        }

        return Math.max(1, newCost);
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


    public static boolean checkValid(Sign sign) {
        return uBlock.findConnectedContainer(sign) != null;
    }

    public static boolean has(Sign sign) {
        PersistentDataContainer container = sign.getPersistentDataContainer();

        NamespacedKey extraKey1 = SyncStockPlugin.getExtraValueKey();
        NamespacedKey extraKey2 = SyncStockPlugin.getExtraValueKey();

        return container.has(extraKey1, PersistentDataType.FLOAT) || container.has(extraKey2, PersistentDataType.FLOAT);
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
            return;  // ignored

        int amount = Arrays.stream(event.getStock()).mapToInt(ItemStack::getAmount).sum();
        double newCost = calcCost(extraValue, container, event.getTransactionType(), amount);
        event.setExactPrice(BigDecimal.valueOf(newCost));
    }

}
