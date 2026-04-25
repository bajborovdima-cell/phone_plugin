package com.example.smartphone.gui;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Arrays;
import java.util.List;

public class GuardMenu implements InventoryHolder {

    private static final String MENU_TITLE = ChatColor.BLUE + "Кого защитить?";

    public void open(Player owner) {
        // [РАЗМЕР МЕНЮ] 9 слотов на каждые 3 игрока
        int playerCount = Bukkit.getOnlinePlayers().size() + 1; // +1 для "Себя"
        int rows = Math.max(1, (playerCount + 8) / 9);
        int size = rows * 9;
        
        Inventory inventory = Bukkit.createInventory(this, size, MENU_TITLE);
        fillMenu(inventory, owner);
        owner.openInventory(inventory);
    }

    private void fillMenu(Inventory inventory, Player owner) {
        int slot = 0;
        
        // [СЕБЯ] Первый слот
        inventory.setItem(slot++, createSelfItem(owner));
        
        // [ИГРОКИ]
        for (Player target : Bukkit.getOnlinePlayers()) {
            if (target.equals(owner)) continue; // Уже добавили "Себя"
            
            ItemStack item = createPlayerHead(target);
            inventory.setItem(slot++, item);
        }
        
        // [ЗАПОЛНИТЕЛЬ]
        while (slot < inventory.getSize()) {
            inventory.setItem(slot++, createGlassPane());
        }
    }

    private ItemStack createSelfItem(Player owner) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName(ChatColor.GREEN + "✓ Себя");
            
            List<String> lore = Arrays.asList(
                ChatColor.GRAY + "Здоровье: " + ChatColor.RED + (int)owner.getHealth() + "/" + (int)owner.getMaxHealth(),
                ChatColor.GRAY + "Мир: " + owner.getWorld().getName(),
                "",
                ChatColor.GREEN + "→ Клик для выбора"
            );
            meta.setLore(lore);
            meta.setOwningPlayer(owner);
            item.setItemMeta(meta);
        }
        
        return item;
    }

    private ItemStack createPlayerHead(Player player) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName(ChatColor.AQUA + "🛡️ " + player.getName());
            
            List<String> lore = Arrays.asList(
                ChatColor.GRAY + "Здоровье: " + ChatColor.RED + (int)player.getHealth() + "/" + (int)player.getMaxHealth(),
                ChatColor.GRAY + "Мир: " + player.getWorld().getName(),
                "",
                ChatColor.GREEN + "→ Клик для выбора"
            );
            meta.setLore(lore);
            meta.setOwningPlayer(player);
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
