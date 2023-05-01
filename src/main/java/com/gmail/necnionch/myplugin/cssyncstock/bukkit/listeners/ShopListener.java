package com.gmail.necnionch.myplugin.cssyncstock.bukkit.listeners;

import com.Acrobot.ChestShop.Events.PreTransactionEvent;
import com.Acrobot.ChestShop.Events.TransactionEvent;
import com.Acrobot.ChestShop.Utils.uBlock;
import com.gmail.necnionch.myplugin.cssyncstock.bukkit.SyncStockPlugin;
import com.gmail.necnionch.myplugin.cssyncstock.bukkit.SyncStockSign;
import com.google.common.collect.Sets;
import org.bukkit.block.Chest;
import org.bukkit.block.Container;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.Set;


public class ShopListener implements Listener {
    private final SyncStockPlugin plugin;

    public ShopListener(SyncStockPlugin plugin) {
        this.plugin = plugin;
    }


    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPreTransaction(PreTransactionEvent event) {
        SyncStockSign syncStockSign = SyncStockSign.from(event.getSign());
        if (syncStockSign != null)
            syncStockSign.onEvent(event);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onTransaction(TransactionEvent event) {
        Container container = uBlock.findConnectedContainer(event.getSign());
        if (container == null)
            return;

        Set<Sign> signs = Sets.newHashSet();

        if (container.getInventory().getHolder() instanceof DoubleChest) {
            DoubleChest doubleChest = (DoubleChest) container.getInventory().getHolder();
            if (doubleChest.getLeftSide() != null)
                signs.addAll(SyncStockSign.findAnyNearbyShopSign(((Chest) doubleChest.getLeftSide()).getBlock()));
            if (doubleChest.getRightSide() != null)
                signs.addAll(SyncStockSign.findAnyNearbyShopSign(((Chest) doubleChest.getRightSide()).getBlock()));
        } else {
            signs.addAll(SyncStockSign.findAnyNearbyShopSign(container.getBlock()));
        }

        signs.forEach(sign -> {
            SyncStockSign syncStockSign = SyncStockSign.from(sign);
            if (syncStockSign != null) {
                syncStockSign.updatePrice();
            }
        });

    }

}
