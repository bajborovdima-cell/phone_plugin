package com.example.smartphone.listeners;

import com.example.smartphone.SmartPhonePlugin;
import com.example.smartphone.gui.HouseMenu;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * Слушатель меню недвижимости
 */
public class HouseMenuListener implements Listener {

    private final SmartPhonePlugin plugin;

    public HouseMenuListener(SmartPhonePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        Inventory inventory = event.getInventory();

        // [ПРОВЕРКА МЕНЮ НЕДВИЖИМОСТИ]
        if (!(inventory.getHolder() instanceof HouseMenu)) {
            return;
        }

        event.setCancelled(true);

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) {
            return;
        }

        // [ОБРАБОТКА КЛИКА]
        HouseMenu houseMenu = new HouseMenu(plugin);
        houseMenu.handleItemClick(player, event.getSlot());
    }
}
