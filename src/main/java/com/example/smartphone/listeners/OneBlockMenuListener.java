package com.example.smartphone.listeners;

import com.example.smartphone.SmartPhonePlugin;
import com.example.smartphone.gui.OneBlockMenu;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * Слушатель меню "Один блок"
 */
public class OneBlockMenuListener implements Listener {

    private final SmartPhonePlugin plugin;

    public OneBlockMenuListener(SmartPhonePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        Inventory inventory = event.getInventory();

        // Проверка меню OneBlock
        if (!(inventory.getHolder() instanceof OneBlockMenu)) {
            return;
        }

        event.setCancelled(true);

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) {
            return;
        }

        // Кнопка выхода (слот 11)
        if (event.getSlot() == 11) {
            player.closeInventory();
            plugin.getOneBlockManager().teleportToSpawn(player);
            return;
        }
        
        // Кнопка продажи (слот 15)
        if (event.getSlot() == 15) {
            player.closeInventory();
            
            double earnings = plugin.getOneBlockManager().sellAllBlocks(player);
            
            if (earnings > 0) {
                player.sendMessage(ChatColor.GREEN + "✓ Продано блоков на " + (int)earnings + " рублей!");
            } else {
                player.sendMessage(ChatColor.RED + "У вас нет добытых блоков для продажи!");
            }
            return;
        }
    }
}
