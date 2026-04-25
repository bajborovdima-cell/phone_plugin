package com.example.smartphone.listeners;

import com.example.smartphone.SmartPhonePlugin;
import com.example.smartphone.gui.PhoneMenu;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class PhoneListener implements Listener {

    private final SmartPhonePlugin plugin;

    public PhoneListener(SmartPhonePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() == EquipmentSlot.OFF_HAND) {
            return;
        }

        if (event.getAction() != Action.RIGHT_CLICK_AIR && 
            event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item == null || item.getType() != Material.LIGHT_BLUE_DYE) {
            return;
        }

        // [ПРОВЕРКА НА ТЕЛЕФОН]
        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            String name = item.getItemMeta().getDisplayName();
            if (name.contains("Телефон")) {
                event.setCancelled(true);
                // [ОТКРЫТИЕ МЕНЮ]
                PhoneMenu menu = new PhoneMenu(plugin);
                menu.open(player);
            }
        }
    }
}
