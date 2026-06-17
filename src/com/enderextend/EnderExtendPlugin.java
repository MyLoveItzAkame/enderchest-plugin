/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.kyori.adventure.text.Component
 *  net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
 *  org.bukkit.command.Command
 *  org.bukkit.command.CommandExecutor
 *  org.bukkit.command.CommandSender
 *  org.bukkit.command.PluginCommand
 *  org.bukkit.event.Listener
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.plugin.java.JavaPlugin
 *  org.jetbrains.annotations.NotNull
 */
package com.enderextend;

import com.enderextend.hooks.LuckPermsHook;
import com.enderextend.listeners.EnderChestListener;
import com.enderextend.managers.EnderChestManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public final class EnderExtendPlugin
extends JavaPlugin
implements CommandExecutor {
    private static EnderExtendPlugin instance;
    private LuckPermsHook luckPermsHook;
    private EnderChestManager enderChestManager;

    public void onEnable() {
        instance = this;
        this.saveDefaultConfig();
        if (!this.setupLuckPerms()) {
            this.getLogger().severe("LuckPerms \u043d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d! \u041e\u0442\u043a\u043b\u044e\u0447\u0435\u043d\u0438\u0435 \u043f\u043b\u0430\u0433\u0438\u043d\u0430.");
            this.getServer().getPluginManager().disablePlugin((Plugin)this);
            return;
        }
        this.enderChestManager = new EnderChestManager(this);
        this.getServer().getPluginManager().registerEvents((Listener)new EnderChestListener(this), (Plugin)this);
        PluginCommand cmd = this.getCommand("enderextend");
        if (cmd != null) {
            cmd.setExecutor((CommandExecutor)this);
        }
        this.getLogger().info("EnderExtend v" + this.getDescription().getVersion() + " \u0432\u043a\u043b\u044e\u0447\u0451\u043d.");
        this.getLogger().info("\u0417\u0430\u0433\u0440\u0443\u0436\u0435\u043d\u044b \u0433\u0440\u0443\u043f\u043f\u044b: " + this.luckPermsHook.getGroupsInfo());
    }

    public void onDisable() {
        if (this.enderChestManager != null) {
            this.enderChestManager.saveAllOpenInventories();
        }
        this.getLogger().info("EnderExtend \u043e\u0442\u043a\u043b\u044e\u0447\u0451\u043d. \u0412\u0441\u0435 \u0434\u0430\u043d\u043d\u044b\u0435 \u0441\u043e\u0445\u0440\u0430\u043d\u0435\u043d\u044b.");
    }

    private boolean setupLuckPerms() {
        if (this.getServer().getPluginManager().getPlugin("LuckPerms") == null) {
            return false;
        }
        this.luckPermsHook = new LuckPermsHook(this);
        return this.luckPermsHook.isReady();
    }

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            this.sendHelp(sender);
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "reload": {
                if (!sender.hasPermission("enderextend.admin")) {
                    sender.sendMessage(EnderExtendPlugin.color("&c\u041d\u0435\u0442 \u043f\u0440\u0430\u0432."));
                    return true;
                }
                this.reloadConfig();
                this.luckPermsHook.reloadGroupMap();
                sender.sendMessage(EnderExtendPlugin.color("&a[EnderExtend] \u041a\u043e\u043d\u0444\u0438\u0433\u0443\u0440\u0430\u0446\u0438\u044f \u043f\u0435\u0440\u0435\u0437\u0430\u0433\u0440\u0443\u0436\u0435\u043d\u0430."));
                break;
            }
            case "info": {
                sender.sendMessage(EnderExtendPlugin.color("&6[EnderExtend] &f\u0412\u0435\u0440\u0441\u0438\u044f: &e" + this.getDescription().getVersion()));
                sender.sendMessage(EnderExtendPlugin.color("&6[EnderExtend] &f\u0413\u0440\u0443\u043f\u043f\u044b: &e" + this.luckPermsHook.getGroupsInfo()));
                break;
            }
            default: {
                this.sendHelp(sender);
            }
        }
        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(EnderExtendPlugin.color("&6[EnderExtend] &f/ee reload &7- \u043f\u0435\u0440\u0435\u0437\u0430\u0433\u0440\u0443\u0437\u0438\u0442\u044c \u043a\u043e\u043d\u0444\u0438\u0433"));
        sender.sendMessage(EnderExtendPlugin.color("&6[EnderExtend] &f/ee info &7- \u0438\u043d\u0444\u043e\u0440\u043c\u0430\u0446\u0438\u044f \u043e \u0433\u0440\u0443\u043f\u043f\u0430\u0445"));
    }

    public static Component color(String text) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(text);
    }

    public static EnderExtendPlugin getInstance() {
        return instance;
    }

    public LuckPermsHook getLuckPermsHook() {
        return this.luckPermsHook;
    }

    public EnderChestManager getEnderChestManager() {
        return this.enderChestManager;
    }
}

