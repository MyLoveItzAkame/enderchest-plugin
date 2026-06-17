/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.kyori.adventure.text.Component
 *  net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
 *  org.bukkit.Bukkit
 *  org.bukkit.entity.Player
 *  org.bukkit.inventory.Inventory
 */
package com.enderextend.managers;

import com.enderextend.EnderExtendPlugin;
import com.enderextend.storage.EnderChestStorage;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class EnderChestManager {
    private final EnderExtendPlugin plugin;
    private final EnderChestStorage storage;
    private final Map<UUID, Inventory> openInventories = new HashMap<UUID, Inventory>();

    public EnderChestManager(EnderExtendPlugin plugin) {
        this.plugin = plugin;
        this.storage = new EnderChestStorage(plugin);
    }

    public void openEnderChest(Player player) {
        String msg;
        int rows = this.plugin.getLuckPermsHook().getRows(player);
        int size = rows * 9;
        Component title = this.getTitle();
        Inventory inventory = Bukkit.createInventory(null, (int)size, (Component)title);
        this.storage.load(player.getUniqueId(), inventory);
        this.openInventories.put(player.getUniqueId(), inventory);
        player.openInventory(inventory);
        if (this.plugin.getConfig().getBoolean("show-open-message", false) && (msg = this.plugin.getConfig().getString("open-message", "&7[EnderExtend] &a\u042d\u043d\u0434\u0435\u0440-\u0441\u0443\u043d\u0434\u0443\u043a \u043e\u0442\u043a\u0440\u044b\u0442 (&e{rows} \u0441\u0442\u0440\u043e\u043a&a)")) != null) {
            player.sendMessage(EnderExtendPlugin.color(msg.replace("{rows}", String.valueOf(rows))));
        }
    }

    public void onInventoryClose(Player player, Inventory inventory) {
        UUID uuid = player.getUniqueId();
        if (!this.openInventories.containsKey(uuid)) {
            return;
        }
        if (!this.openInventories.get(uuid).equals((Object)inventory)) {
            return;
        }
        this.storage.save(uuid, inventory);
        this.openInventories.remove(uuid);
    }

    public void saveAllOpenInventories() {
        for (Map.Entry<UUID, Inventory> entry : this.openInventories.entrySet()) {
            this.storage.save(entry.getKey(), entry.getValue());
        }
        this.openInventories.clear();
    }

    public boolean hasOpenInventory(UUID uuid) {
        return this.openInventories.containsKey(uuid);
    }

    public Inventory getOpenInventory(UUID uuid) {
        return this.openInventories.get(uuid);
    }

    private Component getTitle() {
        String titleStr = this.plugin.getConfig().getString("inventory-title", "&8\u042d\u043d\u0434\u0435\u0440-\u0441\u0443\u043d\u0434\u0443\u043a");
        return LegacyComponentSerializer.legacyAmpersand().deserialize(titleStr);
    }

    public EnderChestStorage getStorage() {
        return this.storage;
    }
}

