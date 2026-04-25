package com.example.smartphone.listeners;

import com.example.smartphone.SmartPhonePlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

/**
 * Слушатель дуэлей
 */
public class DuelListener implements Listener {

    private final SmartPhonePlugin plugin;

    public DuelListener(SmartPhonePlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Обработка смерти в дуэли
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        
        if (plugin.getDuelManager().isInDuel(player)) {
            // Отменяем дроп предметов
            event.setKeepInventory(true);
            event.setKeepLevel(true);
            event.getDrops().clear();
            event.setDroppedExp(0);
            
            // Обработка дуэли
            plugin.getDuelManager().onPlayerDeath(player);
        }
    }
}
