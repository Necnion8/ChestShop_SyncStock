package com.gmail.necnionch.myplugin.cssyncstock.bukkit.events;

import com.gmail.necnionch.myplugin.cssyncstock.bukkit.SyncStockSign;
import org.bukkit.Location;
import org.bukkit.block.Container;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CSSyncStockPriceUpdateEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final SyncStockSign syncSign;
    private final Container container;
    private final @Nullable Location location;
    private final double buyPrice;
    private final double sellPrice;

    public CSSyncStockPriceUpdateEvent(SyncStockSign syncSign, Container container, @Nullable Location location, double buyPrice, double sellPrice) {
        this.syncSign = syncSign;
        this.container = container;
        this.location = location;
        this.buyPrice = buyPrice;
        this.sellPrice = sellPrice;
    }

    public Container getContainer() {
        return container;
    }

    public @Nullable Location getLocation() {
        return location;
    }

    public SyncStockSign getSyncSign() {
        return syncSign;
    }

    public double getBuyPrice() {
        return buyPrice;
    }

    public double getSellPrice() {
        return sellPrice;
    }


    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return handlers;
    }

}
