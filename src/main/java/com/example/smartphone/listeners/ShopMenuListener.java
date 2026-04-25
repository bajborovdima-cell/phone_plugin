package com.example.smartphone.listeners;

import com.example.smartphone.SmartPhonePlugin;
import com.example.smartphone.commands.ShopCommand;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * Слушатель магазина
 */
public class ShopMenuListener implements Listener {

    private final SmartPhonePlugin plugin;

    public ShopMenuListener(SmartPhonePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        Inventory inventory = event.getInventory();

        // Главное меню магазина
        if (inventory.getHolder() instanceof ShopCommand.ShopMenu) {
            event.setCancelled(true);
            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null || clickedItem.getType() == Material.AIR) {
                return;
            }

            ShopCommand.ShopMenu menu = new ShopCommand.ShopMenu(plugin);
            menu.handleItemClick(player, event.getSlot());
            return;
        }

        // Меню предметов категории
        if (inventory.getHolder() instanceof ShopCommand.CategoryItemsMenu) {
            event.setCancelled(true);
            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null || clickedItem.getType() == Material.AIR) {
                return;
            }

            ShopCommand.CategoryItemsMenu menu = (ShopCommand.CategoryItemsMenu) inventory.getHolder();
            menu.handleItemClick(player, event.getSlot(), event.getClick());
            return;
        }
    }
}
