package com.example.smartphone.gui;

import com.example.smartphone.SmartPhonePlugin;
import com.example.smartphone.data.PlayerDataManager;
import com.example.smartphone.data.PlayerDataManager.Home;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;

public class HomeMenu implements InventoryHolder {

    private final SmartPhonePlugin plugin;
    private static final String MENU_TITLE = ChatColor.GREEN + "Ваши дома";

    public HomeMenu(SmartPhonePlugin plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        PlayerDataManager.PlayerData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
        
        // [РАЗМЕР МЕНЮ] 9 слотов на каждые 3 дома, минимум 9
        int homeCount = data.homes.size();
        int rows = Math.max(1, (homeCount + 8) / 9);
        int size = rows * 9;
        
        Inventory inventory = Bukkit.createInventory(this, size, MENU_TITLE);
        fillMenu(inventory, player, data);
        player.openInventory(inventory);
    }

    private void fillMenu(Inventory inventory, Player player, PlayerDataManager.PlayerData data) {
        int slot = 0;
        
        // [ДОБАВЛЕНИЕ ДОМОВ]
        for (Map.Entry<String, Home> entry : data.homes.entrySet()) {
            String homeName = entry.getKey();
            Home home = entry.getValue();
            
            ItemStack item = createHomeItem(homeName, home);
            inventory.setItem(slot++, item);
        }
        
        // [ЗАПОЛНИТЕЛЬ]
        while (slot < inventory.getSize()) {
            inventory.setItem(slot++, createGlassPane());
        }
    }

    private ItemStack createHomeItem(String name, Home home) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(ChatColor.GREEN + "🏠 " + name);

            List<String> lore = Arrays.asList(
                ChatColor.GRAY + "Мир: " + home.world,
                ChatColor.GRAY + "Координаты:",
                ChatColor.YELLOW + "  X: " + (int)home.x,
                ChatColor.YELLOW + "  Y: " + (int)home.y,
                ChatColor.YELLOW + "  Z: " + (int)home.z,
                "",
                ChatColor.GREEN + "ЛКМ: Телепортироваться",
                ChatColor.RED + "ПКМ: Удалить дом"
            );
            meta.setLore(lore);
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
