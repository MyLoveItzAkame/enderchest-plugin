/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Material
 *  org.bukkit.block.Block
 *  org.bukkit.entity.HumanEntity
 *  org.bukkit.entity.Player
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.EventPriority
 *  org.bukkit.event.Listener
 *  org.bukkit.event.block.Action
 *  org.bukkit.event.inventory.InventoryCloseEvent
 *  org.bukkit.event.player.PlayerInteractEvent
 *  org.bukkit.event.player.PlayerJoinEvent
 *  org.bukkit.event.player.PlayerQuitEvent
 *  org.bukkit.inventory.EquipmentSlot
 *  org.bukkit.inventory.Inventory
 *  org.bukkit.plugin.Plugin
 */
package com.enderextend.listeners;

import com.enderextend.EnderExtendPlugin;
import com.enderextend.managers.EnderChestManager;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;

public class EnderChestListener
implements Listener {
    private final EnderExtendPlugin plugin;
    private final EnderChestManager manager;

    public EnderChestListener(EnderExtendPlugin plugin) {
        this.plugin = plugin;
        this.manager = plugin.getEnderChestManager();
    }

    @EventHandler(priority=EventPriority.HIGH, ignoreCancelled=true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        Block block = event.getClickedBlock();
        if (block == null || block.getType() != Material.ENDER_CHEST) {
            return;
        }
        Player player = event.getPlayer();
        if (player.isSneaking() && event.getItem() != null && event.getItem().getType().isBlock()) {
            return;
        }
        event.setCancelled(true);
        this.manager.openEnderChest(player);
    }

    @EventHandler(priority=EventPriority.MONITOR)
    public void onInventoryClose(InventoryCloseEvent event) {
        HumanEntity humanEntity = event.getPlayer();
        if (!(humanEntity instanceof Player)) {
            return;
        }
        Player player = (Player)humanEntity;
        if (!this.manager.hasOpenInventory(player.getUniqueId())) {
            return;
        }
        this.manager.onInventoryClose(player, event.getInventory());
    }

    @EventHandler(priority=EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!this.plugin.getConfig().getBoolean("auto-join-group.enabled", false)) {
            return;
        }
        String groupName = this.plugin.getConfig().getString("auto-join-group.group", "player");
        if (groupName == null || groupName.isEmpty()) {
            return;
        }
        Player player = event.getPlayer();
        this.plugin.getServer().getScheduler().runTaskLater((Plugin)this.plugin, () -> {
            if (!player.isOnline()) {
                return;
            }
            if (!this.plugin.getLuckPermsHook().hasAnyConfiguredGroup(player.getUniqueId())) {
                this.plugin.getLuckPermsHook().assignGroup(player.getUniqueId(), groupName);
            }
        }, 20L);
    }

    @EventHandler(priority=EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Inventory inv;
        Player player = event.getPlayer();
        if (!this.manager.hasOpenInventory(player.getUniqueId())) {
            return;
        }
        if (this.plugin.getConfig().getBoolean("save-on-quit", true) && (inv = this.manager.getOpenInventory(player.getUniqueId())) != null) {
            this.manager.onInventoryClose(player, inv);
        }
    }
}

