package com.gmail.necnionch.myplugin.cssyncstock.bukkit;

import com.gmail.necnionch.myplugin.cssyncstock.bukkit.command.CommandBukkit;
import com.gmail.necnionch.myplugin.cssyncstock.bukkit.commands.MainCommand;
import com.gmail.necnionch.myplugin.cssyncstock.bukkit.listeners.ContainerListener;
import com.gmail.necnionch.myplugin.cssyncstock.bukkit.listeners.ShopListener;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class SyncStockPlugin extends JavaPlugin {
    private static SyncStockPlugin instance;
    private final MainCommand mainCommand = new MainCommand(this);
    private Economy economy;

    @Override
    public void onEnable() {
        instance = this;
        getServer().getPluginManager().registerEvents(new ShopListener(this), this);
        getServer().getPluginManager().registerEvents(new ContainerListener(this), this);
        CommandBukkit.register(mainCommand, Objects.requireNonNull(getCommand("cssyncstock")));

        RegisteredServiceProvider<Economy> economy = getServer().getServicesManager().getRegistration(Economy.class);
        if (economy != null) {
            this.economy = economy.getProvider();
        } else {
            getLogger().severe("Failed to get Economy service");
            setEnabled(false);
        }
    }

    @Override
    public void onDisable() {
    }

    public static SyncStockPlugin getInstance() {
        return instance;
    }


    public Economy getEconomy() {
        return economy;
    }


    public static NamespacedKey getExtraValueKey() {
        Objects.requireNonNull(instance, "Plugin is not loaded");
        return new NamespacedKey(instance, "value");
    }

}
