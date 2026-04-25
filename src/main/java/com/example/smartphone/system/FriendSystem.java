package com.example.smartphone.system;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.util.*;

public class FriendSystem {

    private final JavaPlugin plugin;
    private final Map<UUID, Set<UUID>> friends;
    private final File friendsFile;

    public FriendSystem(JavaPlugin plugin) {
        this.plugin = plugin;
        this.friends = new HashMap<>();
        this.friendsFile = new File(plugin.getDataFolder(), "friends.dat");

        if (!friendsFile.getParentFile().exists()) {
            friendsFile.getParentFile().mkdirs();
        }

        loadFriends();
        startAutoSave();
    }

    public boolean addFriend(Player player, Player target) {
        if (player.getUniqueId().equals(target.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "Нельзя добавить себя в друзья!");
            return false;
        }

        UUID playerUUID = player.getUniqueId();
        UUID targetUUID = target.getUniqueId();

        if (isFriend(playerUUID, targetUUID)) {
            player.sendMessage(ChatColor.YELLOW + target.getName() + " уже в друзьях!");
            return false;
        }

        friends.computeIfAbsent(playerUUID, k -> new HashSet<>()).add(targetUUID);
        saveFriends();

        player.sendMessage(ChatColor.GREEN + "✓ " + target.getName() + " добавлен в друзья!");
        target.sendMessage(ChatColor.GREEN + "✓ " + player.getName() + " добавил вас в друзья!");
        return true;
    }

    public boolean removeFriend(Player player, Player target) {
        UUID playerUUID = player.getUniqueId();
        UUID targetUUID = target.getUniqueId();

        if (!isFriend(playerUUID, targetUUID)) {
            player.sendMessage(ChatColor.RED + target.getName() + " не в друзьях!");
            return false;
        }

        Set<UUID> playerFriends = friends.get(playerUUID);
        if (playerFriends != null) {
            playerFriends.remove(targetUUID);
            if (playerFriends.isEmpty()) {
                friends.remove(playerUUID);
            }
        }

        saveFriends();
        player.sendMessage(ChatColor.YELLOW + "✗ " + target.getName() + " удалён из друзей");
        return true;
    }

    public boolean isFriend(UUID player1, UUID player2) {
        Set<UUID> playerFriends = friends.get(player1);
        return playerFriends != null && playerFriends.contains(player2);
    }

    public Set<UUID> getFriends(UUID playerUUID) {
        Set<UUID> playerFriends = friends.get(playerUUID);
        return playerFriends != null ? new HashSet<>(playerFriends) : Collections.emptySet();
    }

    public List<Player> getOnlineFriends(Player player) {
        List<Player> onlineFriends = new ArrayList<>();
        UUID playerUUID = player.getUniqueId();
        Set<UUID> friendUUIDs = getFriends(playerUUID);

        for (UUID friendUUID : friendUUIDs) {
            Player friend = Bukkit.getPlayer(friendUUID);
            if (friend != null && friend.isOnline()) {
                onlineFriends.add(friend);
            }
        }

        return onlineFriends;
    }

    public double getDistanceToFriend(Player player, Player friend) {
        return Math.round(player.getLocation().distance(friend.getLocation()) * 10.0) / 10.0;
    }

    public void saveFriends() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(friendsFile))) {
            oos.writeObject(friends);
        } catch (IOException e) {
            plugin.getLogger().warning("Не удалось сохранить друзей: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public void loadFriends() {
        if (!friendsFile.exists()) {
            plugin.getLogger().info("Файл друзей создан");
            return;
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(friendsFile))) {
            Object obj = ois.readObject();
            if (obj instanceof Map) {
                friends.putAll((Map<UUID, Set<UUID>>) obj);
                plugin.getLogger().info("Загружено друзей: " + friends.size());
            }
        } catch (IOException | ClassNotFoundException e) {
            plugin.getLogger().warning("Не удалось загрузить друзей: " + e.getMessage());
        }
    }

    private void startAutoSave() {
        plugin.getServer().getScheduler().runTaskTimer(plugin, this::saveFriends, 6000L, 6000L);
    }

    public void shutdown() {
        saveFriends();
    }
}
