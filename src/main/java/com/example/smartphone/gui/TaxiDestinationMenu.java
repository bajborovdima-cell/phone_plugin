package com.example.smartphone.gui;

import com.example.smartphone.SmartPhonePlugin;
import com.example.smartphone.data.PlayerDataManager;
import com.example.smartphone.data.PlayerDataManager.Home;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;

public class TaxiDestinationMenu implements InventoryHolder {

    private final SmartPhonePlugin plugin;
    private static final String MENU_TITLE = ChatColor.YELLOW + "Куда едем?";

    public TaxiDestinationMenu(SmartPhonePlugin plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        PlayerDataManager.PlayerData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
        
        // [ПОДСЧЁТ] Сколько пунктов назначения
        int homeCount = data.homes.size();
        int friendCount = plugin.getFriendSystem().getOnlineFriends(player).size();
        int total = homeCount + friendCount + 1; // +1 для спавна
        
        int rows = Math.max(1, (total + 8) / 9);
        int size = rows * 9;
        
        Inventory inventory = Bukkit.createInventory(this, size, MENU_TITLE);
        fillMenu(inventory, player, data);
        player.openInventory(inventory);
    }

    private void fillMenu(Inventory inventory, Player player, PlayerDataManager.PlayerData data) {
        int slot = 0;
        
        // [СПАВН]
        inventory.setItem(slot++, createSpawnItem());
        
        // [ДОМА]
        for (Map.Entry<String, Home> entry : data.homes.entrySet()) {
            inventory.setItem(slot++, createHomeItem(entry.getKey(), entry.getValue()));
        }
        
        // [ДРУЗЬЯ]
        for (Player friend : plugin.getFriendSystem().getOnlineFriends(player)) {
            inventory.setItem(slot++, createFriendItem(friend));
        }
        
        // [ЗАПОЛНИТЕЛЬ]
        while (slot < inventory.getSize()) {
            inventory.setItem(slot++, createGlassPane());
        }
    }

    private ItemStack createSpawnItem() {
        ItemStack item = new ItemStack(Material.WHITE_BED);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.GREEN + "🏠 Спавн");
            meta.setLore(Arrays.asList(
                ChatColor.GRAY + "Поездка на спавн",
                ChatColor.YELLOW + "Цена: 50 монет",
                "",
                ChatColor.GREEN + "→ Клик для выбора"
            ));
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createHomeItem(String name, Home home) {
        ItemStack item = new ItemStack(Material.OAK_DOOR);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.BLUE + "🏠 Дом: " + name);
            meta.setLore(Arrays.asList(
                ChatColor.GRAY + "Мир: " + home.world,
                ChatColor.GRAY + "Координаты: " + (int)home.x + ", " + (int)home.y + ", " + (int)home.z,
                ChatColor.YELLOW + "Цена: 50 монет",
                "",
                ChatColor.GREEN + "→ Клик для выбора"
            ));
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createFriendItem(Player friend) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.AQUA + "👤 Друг: " + friend.getName());
            meta.setLore(Arrays.asList(
                ChatColor.GRAY + "Мир: " + friend.getWorld().getName(),
                ChatColor.YELLOW + "Цена: 50 монет",
                "",
                ChatColor.GREEN + "→ Клик для выбора"
            ));
            meta.setOwningPlayer(friend);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createGlassPane() {
        ItemStack item = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(" ");
            item.setItemMeta(meta);
        }
        return item;
    }

    @Override
    public Inventory getInventory() {
        return null;
    }
}
