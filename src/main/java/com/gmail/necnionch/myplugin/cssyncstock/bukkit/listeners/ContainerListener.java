package com.gmail.necnionch.myplugin.cssyncstock.bukkit.listeners;

import com.Acrobot.ChestShop.Utils.uBlock;
import com.gmail.necnionch.myplugin.cssyncstock.bukkit.SyncStockPlugin;
import com.gmail.necnionch.myplugin.cssyncstock.bukkit.SyncStockSign;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;


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

        Sign sign = uBlock.findAnyNearbyShopSign(invBlock);
        if (sign == null)
            return;

        SyncStockSign syncStockSign = SyncStockSign.from(sign);
        if (syncStockSign != null) {
            syncStockSign.updatePrice();
        }

    }



}
