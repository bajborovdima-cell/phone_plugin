package com.example.smartphone.listeners;

import com.example.smartphone.SmartPhonePlugin;
import com.example.smartphone.data.PlayerDataManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.Random;

/**
 * Слушатель перка Огненный удар - шанс поджечь цель
 */
public class FireListener implements Listener {

    private final SmartPhonePlugin plugin;
    private final Random random;

    public FireListener(SmartPhonePlugin plugin) {
        this.plugin = plugin;
        this.random = new Random();
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getDamager();
        PlayerDataManager.PlayerData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());

        // Проверяем что перк включён
        if (!data.fireEnabled) {
            return;
        }
        
        // Проверяем уровень огненного удара
        if (data.fireLevel <= 0) {
            return;
        }

        // Шанс поджога: 25% за уровень
        int fireChance = data.fireLevel * 25;
        
        if (random.nextInt(100) < fireChance) {
            // Поджигаем цель
            Entity target = event.getEntity();
            if (target instanceof LivingEntity) {
                LivingEntity livingTarget = (LivingEntity) target;
                int fireTicks = data.fireLevel * 40; // 2/4/6 секунд
                livingTarget.setFireTicks(fireTicks);

                // Эффекты поджога
                player.getWorld().spawnParticle(org.bukkit.Particle.FLAME, livingTarget.getLocation(), 20, 0.5, 0.5, 0.5, 0.1);
                player.getWorld().playSound(player.getLocation(), org.bukkit.Sound.ITEM_FIRECHARGE_USE, 1.0f, 1.0f);
                
                player.sendMessage(ChatColor.GOLD + "🔥 Огненный удар! Цель горит " + (fireTicks / 20) + " сек!");
            }
        }
    }
}
