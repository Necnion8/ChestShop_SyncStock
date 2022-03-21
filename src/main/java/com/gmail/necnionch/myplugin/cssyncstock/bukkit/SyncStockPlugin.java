package com.gmail.necnionch.myplugin.cssyncstock.bukkit;

import com.gmail.necnionch.myplugin.cssyncstock.bukkit.command.CommandBukkit;
import com.gmail.necnionch.myplugin.cssyncstock.bukkit.commands.MainCommand;
import com.gmail.necnionch.myplugin.cssyncstock.bukkit.listeners.ContainerListener;
import com.gmail.necnionch.myplugin.cssyncstock.bukkit.listeners.ShopListener;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class SyncStockPlugin extends JavaPlugin {
    private static SyncStockPlugin instance;
    private final MainCommand mainCommand = new MainCommand(this);

    @Override
    public void onEnable() {
        instance = this;
        getServer().getPluginManager().registerEvents(new ShopListener(this), this);
        getServer().getPluginManager().registerEvents(new ContainerListener(this), this);
        CommandBukkit.register(mainCommand, Objects.requireNonNull(getCommand("cssyncstock")));

    }

    @Override
    public void onDisable() {
    }

    public static SyncStockPlugin getInstance() {
        return instance;
    }


    public static NamespacedKey getExtraValueKey() {
        Objects.requireNonNull(instance, "Plugin is not loaded");
        return new NamespacedKey(instance, "buy_extra");
    }

    public static NamespacedKey getSellPriceExtraKey() {
        Objects.requireNonNull(instance, "Plugin is not loaded");
        return new NamespacedKey(instance, "sell_extra");
    }

}
