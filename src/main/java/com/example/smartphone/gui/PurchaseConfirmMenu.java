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

/**
 * Меню подтверждения покупки с выбором количества
 */
public class PurchaseConfirmMenu implements InventoryHolder {

    private final SmartPhonePlugin plugin;
    private final String itemName;
    private final Material itemMaterial;
    private final int buyPrice;
    private final int sellPrice;
    private final boolean isStackable;

    public PurchaseConfirmMenu(SmartPhonePlugin plugin, String itemName, Material material, int buyPrice, int sellPrice, boolean isStackable) {
        this.plugin = plugin;
        this.itemName = itemName;
        this.itemMaterial = material;
        this.buyPrice = buyPrice;
        this.sellPrice = sellPrice;
        this.isStackable = isStackable;
    }

    public void open(Player player) {
        int size = isStackable ? 27 : 18;
        Inventory inventory = Bukkit.createInventory(this, size, ChatColor.GOLD + "Купить: " + itemName);
        fillMenu(inventory, player);
        player.openInventory(inventory);
    }
    
    public String getItemName() {
        return itemName;
    }
    
    public boolean isStackable() {
        return isStackable;
    }

    private void fillMenu(Inventory inventory, Player player) {
        if (isStackable) {
            // Кнопки количества для стака
            inventory.setItem(10, createQuantityItem(1, buyPrice));
            inventory.setItem(11, createQuantityItem(16, buyPrice * 16));
            inventory.setItem(15, createQuantityItem(32, buyPrice * 32));
            inventory.setItem(16, createQuantityItem(64, buyPrice * 64));

            // Информация о предмете
            inventory.setItem(13, createInfoItem());
            
            // Кнопка продажи (ПКМ)
            inventory.setItem(4, createSellInfoItem());
        } else {
            // Только 1 кнопка для обычных предметов
            inventory.setItem(11, createQuantityItem(1, buyPrice));
            
            // Информация о предмете
            inventory.setItem(4, createInfoItem());
            
            // Кнопка продажи
            inventory.setItem(6, createSellInfoItem());
        }

        // Заполнители
        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, createGlassPane());
            }
        }
    }

    private ItemStack createQuantityItem(int amount, int totalCost) {
        ItemStack item = new ItemStack(itemMaterial, isStackable ? Math.min(amount, itemMaterial.getMaxStackSize()) : 1);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.GREEN + "Купить " + amount + " шт.");
            meta.setLore(Arrays.asList(
                ChatColor.GRAY + "Цена: " + ChatColor.GREEN + totalCost + " монет",
                "",
                ChatColor.GREEN + "ЛКМ - Купить",
                ChatColor.RED + "ПКМ - Продать " + amount + " шт (за " + (sellPrice * amount) + " монет)"
            ));
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createInfoItem() {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.AQUA + itemName);
            if (isStackable) {
                meta.setLore(Arrays.asList(
                    ChatColor.GRAY + "Цена за 1 шт: " + ChatColor.GREEN + buyPrice + " монет",
                    ChatColor.GRAY + "Цена продажи 1 шт: " + ChatColor.GOLD + sellPrice + " монет",
                    "",
                    ChatColor.YELLOW + "Выберите количество:",
                    ChatColor.GREEN + "ЛКМ - Покупка",
                    ChatColor.RED + "ПКМ - Продажа"
                ));
            } else {
                meta.setLore(Arrays.asList(
                    ChatColor.GRAY + "Цена: " + ChatColor.GREEN + buyPrice + " монет",
                    ChatColor.GRAY + "Цена продажи: " + ChatColor.GOLD + sellPrice + " монет",
                    "",
                    ChatColor.GREEN + "ЛКМ - Купить 1 шт",
                    ChatColor.RED + "ПКМ - Продать 1 шт"
                ));
            }
            item.setItemMeta(meta);
        }
        return item;
    }
    
    private ItemStack createSellInfoItem() {
        ItemStack item = new ItemStack(Material.CHEST);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.RED + "💰 Продать предмет");
            meta.setLore(Arrays.asList(
                ChatColor.GRAY + "ПКМ по предмету выше",
                ChatColor.GRAY + "для продажи",
                "",
                ChatColor.YELLOW + "Цена продажи: " + sellPrice + " монет/шт"
            ));
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
