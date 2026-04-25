package com.example.smartphone.listeners;

import com.example.smartphone.SmartPhonePlugin;
import com.example.smartphone.data.PlayerDataManager;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Слушатель перка Регенерация - пассивное восстановление HP
 */
public class RegenListener {

    private final SmartPhonePlugin plugin;
    private final Map<UUID, Long> lastDamageTime;

    public RegenListener(SmartPhonePlugin plugin) {
        this.plugin = plugin;
        this.lastDamageTime = new HashMap<>();
        
        // Запускаем задачу регенерации каждые 2 секунды
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    checkRegen(player);
                }
            }
        }.runTaskTimer(plugin, 40L, 40L);
    }

    private void checkRegen(Player player) {
        PlayerDataManager.PlayerData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());

        // Проверяем что перк включён
        if (!data.regenEnabled) {
            return;
        }
        
        // Проверяем уровень регенерации
        if (data.regenLevel <= 0) {
            return;
        }

        // Проверяем кулдаун
        long currentTime = System.currentTimeMillis();
        long cooldown = getRegenCooldown(data.regenLevel);
        Long lastRegen = lastDamageTime.get(player.getUniqueId());
        
        if (lastRegen != null && currentTime - lastRegen < cooldown) {
            return;
        }

        // Проверяем что игрок не в бою (не получал урон 3 секунды)
        Long lastDamage = lastDamageTime.get(player.getUniqueId());
        if (lastDamage != null && currentTime - lastDamage < 3000) {
            return;
        }

        // Проверяем что здоровье не полное
        if (player.getHealth() >= player.getMaxHealth()) {
            return;
        }

        // Восстанавливаем HP: 2/4/6 HP в зависимости от уровня
        double healAmount = data.regenLevel * 2;
        double newHealth = Math.min(player.getHealth() + healAmount, player.getMaxHealth());
        player.setHealth(newHealth);

        // Обновляем время
        lastDamageTime.put(player.getUniqueId(), currentTime);

        // Эффекты регенерации
        player.getWorld().spawnParticle(org.bukkit.Particle.HEART, player.getLocation().add(0, 1, 0), 5, 0.3, 0.3, 0.3, 0.1);
    }

    private long getRegenCooldown(int level) {
        // Уровень 1: 4 сек, Уровень 2: 2 сек, Уровень 3: 1 сек
        return (5 - level) * 1000L;
    }
    
    /**
     * Отметить что игрок получил урон
     */
    public void onPlayerDamage(Player player) {
        lastDamageTime.put(player.getUniqueId(), System.currentTimeMillis());
    }
}
