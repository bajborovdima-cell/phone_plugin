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
 * Слушатель перка Вампиризм - шанс восстановить HP при ударе
 */
public class VampireListener implements Listener {

    private final SmartPhonePlugin plugin;
    private final Random random;

    public VampireListener(SmartPhonePlugin plugin) {
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
        if (!data.vampireEnabled) {
            return;
        }
        
        // Проверяем уровень вампиризма
        if (data.vampireLevel <= 0) {
            return;
        }

        // Шанс вампиризма: 10% за уровень
        int vampireChance = data.vampireLevel * 10;
        
        if (random.nextInt(100) < vampireChance) {
            // Восстанавливаем HP
            double healAmount = data.vampireLevel; // 1/2/3 HP
            double newHealth = Math.min(player.getHealth() + healAmount, player.getMaxHealth());
            player.setHealth(newHealth);

            // Эффекты вампиризма
            player.getWorld().spawnParticle(org.bukkit.Particle.HEART, player.getLocation().add(0, 1, 0), 20, 0.5, 0.5, 0.5, 0.1);
            player.getWorld().playSound(player.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_ATTACK_KNOCKBACK, 1.0f, 1.0f);
            
            player.sendMessage(ChatColor.DARK_RED + "🩸 Вампиризм! Восстановлено " + (int)healAmount + " HP!");
        }
    }
}
