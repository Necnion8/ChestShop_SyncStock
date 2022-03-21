package com.gmail.necnionch.myplugin.cssyncstock.bukkit.listeners;

import com.Acrobot.ChestShop.Events.PreTransactionEvent;
import com.Acrobot.ChestShop.Events.TransactionEvent;
import com.gmail.necnionch.myplugin.cssyncstock.bukkit.SyncStockPlugin;
import com.gmail.necnionch.myplugin.cssyncstock.bukkit.SyncStockSign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;


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
        SyncStockSign syncStockSign = SyncStockSign.from(event.getSign());
        if (syncStockSign != null)
            syncStockSign.updatePrice();
    }

}
