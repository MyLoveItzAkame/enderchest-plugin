/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.configuration.ConfigurationSection
 *  org.bukkit.configuration.file.YamlConfiguration
 *  org.bukkit.inventory.Inventory
 *  org.bukkit.inventory.ItemStack
 */
package com.enderextend.storage;

import com.enderextend.EnderExtendPlugin;
import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.logging.Level;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class EnderChestStorage {
    private final EnderExtendPlugin plugin;
    private final File dataFolder;

    public EnderChestStorage(EnderExtendPlugin plugin) {
        this.plugin = plugin;
        this.dataFolder = new File(plugin.getDataFolder(), "data");
        if (!this.dataFolder.exists()) {
            this.dataFolder.mkdirs();
        }
    }

    public void save(UUID uuid, Inventory inventory) {
        File file = this.getFile(uuid);
        YamlConfiguration config = new YamlConfiguration();
        config.set("size", (Object)inventory.getSize());
        for (int i = 0; i < inventory.getSize(); ++i) {
            ItemStack item = inventory.getItem(i);
            if (item == null || item.getType().isAir()) continue;
            config.set("slots." + i, (Object)item);
        }
        try {
            config.save(file);
        }
        catch (IOException e) {
            this.plugin.getLogger().log(Level.SEVERE, "\u041d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u0441\u043e\u0445\u0440\u0430\u043d\u0438\u0442\u044c \u0434\u0430\u043d\u043d\u044b\u0435 \u044d\u043d\u0434\u0435\u0440-\u0441\u0443\u043d\u0434\u0443\u043a\u0430 \u0434\u043b\u044f " + String.valueOf(uuid), e);
        }
    }

    public void load(UUID uuid, Inventory inventory) {
        File file = this.getFile(uuid);
        if (!file.exists()) {
            return;
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration((File)file);
        ConfigurationSection slotsSection = config.getConfigurationSection("slots");
        if (slotsSection == null) {
            return;
        }
        for (String key : slotsSection.getKeys(false)) {
            try {
                ItemStack item;
                int slot = Integer.parseInt(key);
                if (slot < 0 || slot >= inventory.getSize() || (item = config.getItemStack("slots." + key)) == null) continue;
                inventory.setItem(slot, item);
            }
            catch (NumberFormatException numberFormatException) {}
        }
    }

    public int getSavedSize(UUID uuid) {
        File file = this.getFile(uuid);
        if (!file.exists()) {
            return 0;
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration((File)file);
        return config.getInt("size", 0);
    }

    public boolean hasData(UUID uuid) {
        return this.getFile(uuid).exists();
    }

    private File getFile(UUID uuid) {
        return new File(this.dataFolder, String.valueOf(uuid) + ".yml");
    }
}

