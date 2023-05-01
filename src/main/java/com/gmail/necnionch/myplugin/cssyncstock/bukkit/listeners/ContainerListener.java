package com.gmail.necnionch.myplugin.cssyncstock.bukkit.listeners;

import com.gmail.necnionch.myplugin.cssyncstock.bukkit.SyncStockPlugin;
import com.gmail.necnionch.myplugin.cssyncstock.bukkit.SyncStockSign;
import com.google.common.collect.Sets;
import org.bukkit.Location;
import org.bukkit.block.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;

import java.util.Set;


public class ContainerListener implements Listener {
    private final SyncStockPlugin plugin;

    public ContainerListener(SyncStockPlugin plugin) {
        this.plugin = plugin;
    }


    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventory(InventoryCloseEvent event) {
        Location invLocation = event.getInventory().getLocation();
        if (invLocation == null)
            return;

        Block invBlock = invLocation.getBlock();
        if (!(invBlock.getState() instanceof Container))
            return;

        Container container = (Container) invBlock.getState();
        Set<Sign> signs = Sets.newHashSet();

        if (container.getInventory().getHolder() instanceof DoubleChest) {
            DoubleChest doubleChest = (DoubleChest) container.getInventory().getHolder();
            if (doubleChest.getLeftSide() != null)
                signs.addAll(SyncStockSign.findAnyNearbyShopSign(((Chest) doubleChest.getLeftSide()).getBlock()));
            if (doubleChest.getRightSide() != null)
                signs.addAll(SyncStockSign.findAnyNearbyShopSign(((Chest) doubleChest.getRightSide()).getBlock()));
        } else {
            signs.addAll(SyncStockSign.findAnyNearbyShopSign(invBlock));
        }

        signs.forEach(sign -> {
            SyncStockSign syncStockSign = SyncStockSign.from(sign);
            if (syncStockSign != null) {
                syncStockSign.updatePrice();
            }
        });

    }

}
