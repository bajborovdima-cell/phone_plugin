package com.example.smartphone.gui;

import com.example.smartphone.SmartPhonePlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.Map;

/**
 * Меню выхода с острова "Один блок"
 */
public class OneBlockMenu implements InventoryHolder {

    private final SmartPhonePlugin plugin;
    private static final String MENU_TITLE = ChatColor.DARK_RED + "Один блок";

    public OneBlockMenu(SmartPhonePlugin plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        Inventory inventory = Bukkit.createInventory(this, 27, MENU_TITLE);
        fillMenu(inventory, player);
        player.openInventory(inventory);
    }

    private void fillMenu(Inventory inventory, Player player) {
        // Кнопка выхода
        inventory.setItem(11, createExitItem());
        
        // Кнопка продажи блоков
        inventory.setItem(15, createSellItem(player));

        // Статистика добычи
        Map<Material, Integer> minedBlocks = plugin.getOneBlockManager().getMinedBlocks(player);
        int totalBlocks = minedBlocks.values().stream().mapToInt(Integer::intValue).sum();
        
        inventory.setItem(4, createInfoItem(
            Material.DIAMOND_PICKAXE,
            ChatColor.YELLOW + "Статистика добычи",
            ChatColor.GRAY + "Всего добыто: " + totalBlocks + " блоков"
        ));

        // Заполнители
        for (int i = 0; i < 27; i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, createGlassPane());
            }
        }
    }

    private ItemStack createExitItem() {
        ItemStack item = new ItemStack(Material.BARRIER);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.RED + "🚪 Выйти с острова");
            meta.setLore(Arrays.asList(
                ChatColor.GRAY + "Нажмите для возврата на спавн",
                "",
                ChatColor.YELLOW + "ЛКМ для выхода"
            ));
            item.setItemMeta(meta);
        }
        return item;
    }
    
    private ItemStack createSellItem(Player player) {
        Map<Material, Integer> minedBlocks = plugin.getOneBlockManager().getMinedBlocks(player);
        int totalBlocks = minedBlocks.values().stream().mapToInt(Integer::intValue).sum();
        
        // Подсчитываем общую стоимость
        double totalValue = 0.0;
        for (Map.Entry<Material, Integer> entry : minedBlocks.entrySet()) {
            double price = plugin.getConfig().getDouble("jobs.mining.blocks." + entry.getKey().name(), 1.0);
            totalValue += price * entry.getValue();
        }
        
        ItemStack item = new ItemStack(Material.GOLD_INGOT);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.GREEN + "💰 Продать все блоки");
            meta.setLore(Arrays.asList(
                ChatColor.GRAY + "Добыто блоков: " + totalBlocks,
                ChatColor.GRAY + "Общая стоимость: " + (int)totalValue + " рублей",
                "",
                ChatColor.GREEN + "ЛКМ для продажи"
            ));
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createInfoItem(Material material, String name, String lore) {
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

    @Override
    public Inventory getInventory() {
        return null;
    }
}
