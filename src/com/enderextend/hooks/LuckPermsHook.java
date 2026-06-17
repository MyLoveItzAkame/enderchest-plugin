/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.luckperms.api.LuckPerms
 *  net.luckperms.api.LuckPermsProvider
 *  net.luckperms.api.model.user.User
 *  net.luckperms.api.node.Node
 *  net.luckperms.api.node.NodeType
 *  net.luckperms.api.node.types.InheritanceNode
 *  org.bukkit.configuration.ConfigurationSection
 *  org.bukkit.entity.Player
 */
package com.enderextend.hooks;

import com.enderextend.EnderExtendPlugin;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.InheritanceNode;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public class LuckPermsHook {
    private final EnderExtendPlugin plugin;
    private LuckPerms luckPerms;
    private final LinkedHashMap<String, Integer> groupRows = new LinkedHashMap();

    public LuckPermsHook(EnderExtendPlugin plugin) {
        this.plugin = plugin;
        try {
            this.luckPerms = LuckPermsProvider.get();
            this.reloadGroupMap();
        }
        catch (IllegalStateException e) {
            plugin.getLogger().severe("\u041d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u043f\u043e\u0434\u043a\u043b\u044e\u0447\u0438\u0442\u044c\u0441\u044f \u043a LuckPerms: " + e.getMessage());
        }
    }

    public boolean isReady() {
        return this.luckPerms != null;
    }

    public void reloadGroupMap() {
        this.groupRows.clear();
        ConfigurationSection section = this.plugin.getConfig().getConfigurationSection("groups");
        if (section == null) {
            this.plugin.getLogger().warning("\u0421\u0435\u043a\u0446\u0438\u044f 'groups' \u043d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d\u0430 \u0432 config.yml!");
            return;
        }
        LinkedHashMap<String, Integer> raw = new LinkedHashMap<String, Integer>();
        for (String key : section.getKeys(false)) {
            raw.put(key.toLowerCase(), section.getInt(key, 3));
        }
        raw.entrySet().stream().sorted((a, b) -> (Integer)b.getValue() - (Integer)a.getValue()).forEach(e -> this.groupRows.put((String)e.getKey(), (Integer)e.getValue()));
    }

    public int getRows(Player player) {
        int rows;
        if (player.hasPermission("enderextend.bypass")) {
            return 6;
        }
        User user = this.luckPerms.getUserManager().getUser(player.getUniqueId());
        if (user == null) {
            return this.getDefaultRows();
        }
        int maxRows = this.getDefaultRows();
        for (InheritanceNode node : user.getNodes(NodeType.INHERITANCE)) {
            int rows2;
            String groupName = node.getGroupName().toLowerCase();
            if (!this.groupRows.containsKey(groupName) || (rows2 = this.groupRows.get(groupName).intValue()) <= maxRows) continue;
            maxRows = rows2;
        }
        String primaryGroup = user.getPrimaryGroup().toLowerCase();
        if (this.groupRows.containsKey(primaryGroup) && (rows = this.groupRows.get(primaryGroup).intValue()) > maxRows) {
            maxRows = rows;
        }
        return Math.min(maxRows, 6);
    }

    private int getDefaultRows() {
        return this.groupRows.getOrDefault("default", 3);
    }

    public String getGroupsInfo() {
        return this.groupRows.entrySet().stream().filter(e -> !((String)e.getKey()).equals("default")).map(e -> (String)e.getKey() + "=" + String.valueOf(e.getValue()) + "\u0440").collect(Collectors.joining(", "));
    }

    public boolean hasAnyConfiguredGroup(UUID uuid) {
        User user = this.luckPerms.getUserManager().getUser(uuid);
        if (user == null) {
            return false;
        }
        String primaryGroup = user.getPrimaryGroup().toLowerCase();
        if (this.groupRows.containsKey(primaryGroup) && !primaryGroup.equals("default")) {
            return true;
        }
        for (InheritanceNode node : user.getNodes(NodeType.INHERITANCE)) {
            String groupName = node.getGroupName().toLowerCase();
            if (!this.groupRows.containsKey(groupName) || groupName.equals("default")) continue;
            return true;
        }
        return false;
    }

    public void assignGroup(UUID uuid, String groupName) {
        this.luckPerms.getUserManager().modifyUser(uuid, user -> user.data().add((Node)InheritanceNode.builder((String)groupName).build()));
    }

    public Map<String, Integer> getGroupRows() {
        return this.groupRows;
    }
}

