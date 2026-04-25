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
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Arrays;
import java.util.List;

/**
 * Меню дуэли - выбор игрока для вызова
 */
public class DuelMenu implements InventoryHolder {

    private final SmartPhonePlugin plugin;
    private static final String MENU_TITLE = ChatColor.RED + "⚔️ Дуэль";

    public DuelMenu(SmartPhonePlugin plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        Inventory inventory = Bukkit.createInventory(this, 27, MENU_TITLE);
        fillMenu(inventory, player);
        player.openInventory(inventory);
    }

    private void fillMenu(Inventory inventory, Player player) {
        // Заголовок
        inventory.setItem(4, createMenuItem(
            Material.DIAMOND_SWORD,
            ChatColor.GOLD + "Выберите соперника",
            ChatColor.GRAY + "Нажмите на игрока для вызова",
            ChatColor.GRAY + "Кулдаун: 30 сек на принятие"
        ));

        // Список игроков онлайн (кроме себя)
        int slot = 9;
        for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
            if (onlinePlayer.equals(player)) continue;
            if (slot >= 26) break;

            inventory.setItem(slot, createPlayerItem(onlinePlayer));
            slot++;
        }

        // Заполнители
        for (int i = 0; i < 27; i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, createGlassPane());
            }
        }
    }

    private ItemStack createPlayerItem(Player target) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        
        if (meta != null) {
            meta.setOwningPlayer(target);
            meta.setDisplayName(ChatColor.GREEN + target.getName());
            
            boolean inDuel = plugin.getDuelManager().isInDuel(target);
            meta.setLore(Arrays.asList(
                ChatColor.GRAY + "Нажмите для вызова на дуэль",
                inDuel ? ChatColor.RED + "Уже в дуэли" : ChatColor.GREEN + "Доступен"
            ));
            item.setItemMeta(meta);
        }
        
        return item;
    }

    private ItemStack createMenuItem(Material material, String name, String lore1, String lore2) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(Arrays.asList(lore1, lore2));
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
