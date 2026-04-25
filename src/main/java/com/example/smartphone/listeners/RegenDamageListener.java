package com.example.smartphone.listeners;

import com.example.smartphone.SmartPhonePlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

/**
 * Слушатель для отслеживания урона (для регенерации)
 */
public class RegenDamageListener implements Listener {

    private final SmartPhonePlugin plugin;
    private final RegenListener regenListener;

    public RegenDamageListener(SmartPhonePlugin plugin, RegenListener regenListener) {
        this.plugin = plugin;
        this.regenListener = regenListener;
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            regenListener.onPlayerDamage((Player) event.getEntity());
        }
    }
}
