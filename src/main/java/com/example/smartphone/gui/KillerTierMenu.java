package com.example.smartphone.gui;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

public class KillerTierMenu implements InventoryHolder {

    private static final String MENU_TITLE = ChatColor.DARK_RED + "Уровень киллера";
    
    private final Player target;

    public KillerTierMenu(Player target) {
        this.target = target;
    }

    public void open(Player owner) {
        Inventory inventory = Bukkit.createInventory(this, 9, MENU_TITLE);
        fillMenu(inventory, owner);
        owner.openInventory(inventory);
    }

    private void fillMenu(Inventory inventory, Player owner) {
        // [ОБЫЧНЫЙ] Слот 2
        inventory.setItem(2, createTierItem(
            Material.IRON_SWORD,
            ChatColor.WHITE + "Обычный киллер",
            ChatColor.GRAY + "Цена: §e500 монет",
            ChatColor.GRAY + "Здоровье: §c20 HP",
            ChatColor.GRAY + "Урон: §c5",
            ChatColor.GRAY + "Время: §e2 минуты",
            "",
            ChatColor.GREEN + "→ Клик для найма"
        ));
        
        // [ПРОФЕССИОНАЛ] Слот 4
        inventory.setItem(4, createTierItem(
            Material.GOLDEN_SWORD,
            ChatColor.GOLD + "Профессионал",
            ChatColor.GRAY + "Цена: §e1000 монет",
            ChatColor.GRAY + "Здоровье: §c30 HP",
            ChatColor.GRAY + "Урон: §c8",
            ChatColor.GRAY + "Время: §e3 минуты",
            "",
            ChatColor.GREEN + "→ Клик для найма"
        ));
        
        // [ЛЕГЕНДА] Слот 6
        inventory.setItem(6, createTierItem(
            Material.DIAMOND_SWORD,
            ChatColor.DARK_PURPLE + "Легенда",
            ChatColor.GRAY + "Цена: §e2500 монет",
            ChatColor.GRAY + "Здоровье: §c50 HP",
            ChatColor.GRAY + "Урон: §c12",
            ChatColor.GRAY + "Время: §e5 минут",
            "",
            ChatColor.GREEN + "→ Клик для найма"
        ));
        
        // [ЗАПОЛНИТЕЛИ]
        for (int i : new int[]{0, 1, 3, 5, 7, 8}) {
            inventory.setItem(i, createGlassPane());
        }
    }

    private ItemStack createTierItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(Arrays.asList(lore));
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

    public Player getTarget() {
        return target;
    }

    @Override
    public Inventory getInventory() {
        return null;
    }
}
