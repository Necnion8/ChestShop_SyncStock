package com.gmail.necnionch.myplugin.cssyncstock.bukkit.commands;

import com.Acrobot.ChestShop.Signs.ChestShopSign;
import com.gmail.necnionch.myplugin.cssyncstock.bukkit.SyncStockPlugin;
import com.gmail.necnionch.myplugin.cssyncstock.bukkit.SyncStockSign;
import com.gmail.necnionch.myplugin.cssyncstock.bukkit.command.Command;
import com.gmail.necnionch.myplugin.cssyncstock.bukkit.command.CommandSender;
import com.gmail.necnionch.myplugin.cssyncstock.bukkit.command.RootCommand;
import com.gmail.necnionch.myplugin.cssyncstock.bukkit.command.errors.CommandError;
import com.gmail.necnionch.myplugin.cssyncstock.bukkit.command.errors.InternalCommandError;
import com.gmail.necnionch.myplugin.cssyncstock.bukkit.command.errors.NotFoundCommandError;
import com.gmail.necnionch.myplugin.cssyncstock.bukkit.command.errors.PermissionCommandError;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MainCommand extends RootCommand {
    private final SyncStockPlugin plugin;

    public MainCommand(SyncStockPlugin plugin) {
        this.plugin = plugin;

        Command def = addCommand("check", null, this::execCheck);
        addCommand("set", null, this::execSet);
        addCommand("reset", null, this::execReset);
        setDefault(def);
    }

    @Override
    public void onError(@NotNull CommandSender sender, @Nullable Command command, @NotNull CommandError error) {
        String message = "不明なエラーが発生しました";
        if (error instanceof NotFoundCommandError) {
            message = "&f/cssp &e[check]&f\n/cssp set &e(extraValue)\n/cssp reset";
        } else if (error instanceof PermissionCommandError) {
            message = "権限がありません";
        } else if (error instanceof InternalCommandError) {
            message = "内部エラーが発生しました";
        }
        send(sender, "&c" + message);
    }

    private void send(CommandSender sender, String message) {
        sender.sendMessage(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', message)));
    }

    private void send(org.bukkit.command.CommandSender sender, String message) {
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
    }

    private void execCheck(CommandSender sender, List<String> args) {
        if (!(sender.getSender() instanceof Player)) {
            send(sender, "&cプレイヤーのみ実行できるコマンドです");
            return;
        }
        Player player = (Player) sender.getSender();
        Block targetBlock = player.getTargetBlockExact(4);

        if (targetBlock == null || !ChestShopSign.isValid(targetBlock)) {
            send(sender, "&cショップ看板にフォーカスを当てて実行してください");
            return;
        }

        executeLookup(player, ((Sign) targetBlock.getState()));
    }

    private void execSet(CommandSender sender, List<String> args) {
        if (!(sender.getSender() instanceof Player)) {
            send(sender, "&cプレイヤーのみ実行できるコマンドです");
            return;
        }
        Player player = (Player) sender.getSender();
        Block targetBlock = player.getTargetBlockExact(4);

        if (targetBlock == null || !ChestShopSign.isValid(targetBlock)) {
            send(sender, "&cショップ看板にフォーカスを当てて実行してください");
            return;
        }

        float extraValue;
        try {
            if (args.isEmpty())
                throw new NumberFormatException();
            extraValue = Float.parseFloat(args.get(0));
        } catch (NumberFormatException e) {
            send(sender, "&c値を入力してください");
            return;
        }
        executeSet(player, ((Sign) targetBlock.getState()), extraValue);
    }

    private void execReset(CommandSender sender, List<String> args) {
        if (!(sender.getSender() instanceof Player)) {
            send(sender, "&cプレイヤーのみ実行できるコマンドです");
            return;
        }
        Player player = (Player) sender.getSender();
        Block targetBlock = player.getTargetBlockExact(4);

        if (targetBlock == null || !ChestShopSign.isValid(targetBlock)) {
            send(sender, "&cショップ看板にフォーカスを当てて実行してください");
            return;
        }
        executeReset(player, ((Sign) targetBlock.getState()));
    }


    private void executeLookup(Player player, Sign sign) {
        SyncStockSign syncStockSign = SyncStockSign.from(sign);
        if (syncStockSign == null) {
            send(player, "&c設定されていません");
            return;
        }
        send(player, "設定されている価格同期: &6x" + syncStockSign.getExtraValue());
    }

    private void executeSet(Player player, Sign sign, float extraValue) {
        if (!SyncStockSign.checkValid(sign)) {
            send(player, "&cこのショップ看板は適用できません");
            return;
        }
        SyncStockSign syncStockSign = SyncStockSign.from(sign, extraValue);
        syncStockSign.updatePrice();
        send(player, "&a価格同期(x" + extraValue + ")を有効にしました");
    }

    private void executeReset(Player player, Sign sign) {
        if (SyncStockSign.clean(sign)) {
            send(player, "&a価格同期設定を削除しました");
        } else {
            send(player, "&c価格同期は設定されていません");
        }
    }



}
