package com.example.smartphone.listeners;

import com.example.smartphone.SmartPhonePlugin;
import com.example.smartphone.data.PlayerDataManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.Random;

/**
 * Слушатель перка Щит - шанс блокировать урон
 */
public class ShieldListener implements Listener {

    private final SmartPhonePlugin plugin;
    private final Random random;

    public ShieldListener(SmartPhonePlugin plugin) {
        this.plugin = plugin;
        this.random = new Random();
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getEntity();
        PlayerDataManager.PlayerData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());

        // Проверяем что перк включён
        if (!data.shieldEnabled) {
            return;
        }
        
        // Проверяем уровень щита
        if (data.shieldLevel <= 0) {
            return;
        }

        // Шанс блока: 5% за уровень
        int blockChance = data.shieldLevel * 5;
        
        if (random.nextInt(100) < blockChance) {
            // Блокируем урон
            event.setCancelled(true);
            
            // Эффекты блока
            player.sendMessage(ChatColor.BLUE + "🛡️ Щит заблокировал урон!");
            player.getWorld().spawnParticle(org.bukkit.Particle.CRIT, player.getLocation().add(0, 1, 0), 20, 0.5, 0.5, 0.5, 0.1);
            player.getWorld().playSound(player.getLocation(), org.bukkit.Sound.ITEM_SHIELD_BLOCK, 1.0f, 1.0f);
        }
    }
}
