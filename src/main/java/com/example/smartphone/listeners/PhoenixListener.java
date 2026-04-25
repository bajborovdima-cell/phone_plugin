package com.example.smartphone.listeners;

import com.example.smartphone.SmartPhonePlugin;
import com.example.smartphone.data.PlayerDataManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Слушатель перка Феникс - воскрешение после смерти
 */
public class PhoenixListener implements Listener {

    private final SmartPhonePlugin plugin;

    public PhoenixListener(SmartPhonePlugin plugin) {
        this.plugin = plugin;
        
        // Запускаем задачу перезарядки феникса каждые 30 секунд
        new org.bukkit.scheduler.BukkitRunnable() {
            @Override
            public void run() {
                for (org.bukkit.entity.Player player : plugin.getServer().getOnlinePlayers()) {
                    checkPhoenixRecharge(player);
                }
            }
        }.runTaskTimer(plugin, 600L, 600L); // Каждые 30 секунд
    }
    
    /**
     * Перезарядка зарядов феникса (1 заряд каждые 5 минут)
     */
    private void checkPhoenixRecharge(org.bukkit.entity.Player player) {
        PlayerDataManager.PlayerData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
        
        if (data.phoenixLevel <= 0) return;
        if (data.phoenixCharges >= data.phoenixLevel) return; // Уже полный заряд
        
        long currentTime = System.currentTimeMillis();
        long timeSinceLastCharge = currentTime - data.phoenixLastChargeTime;
        long rechargeTime = 5 * 60 * 1000; // 5 минут
        
        if (timeSinceLastCharge >= rechargeTime) {
            data.phoenixCharges++;
            data.phoenixLastChargeTime = currentTime;
            plugin.getPlayerDataManager().savePlayer(data);
            
            player.sendMessage(ChatColor.RED + "🔥 Феникс перезарядился! Зарядов: " + data.phoenixCharges + "/" + data.phoenixLevel);
            player.getWorld().spawnParticle(org.bukkit.Particle.FLAME, player.getLocation(), 20, 0.5, 0.5, 0.5, 0.1);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        PlayerDataManager.PlayerData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());

        // Проверяем что перк включён
        if (!data.phoenixEnabled) {
            return;
        }
        
        // Проверяем уровень феникса и заряды
        if (data.phoenixLevel <= 0 || data.phoenixCharges <= 0) {
            return;
        }

        // Отменяем смерть
        event.setCancelled(true);
        event.setKeepInventory(true);
        event.setKeepLevel(true);
        event.getDrops().clear();
        event.setDroppedExp(0);

        // Восстанавливаем здоровье
        player.setHealth(player.getMaxHealth());
        player.setFoodLevel(20);
        player.setFireTicks(0);

        // Убираем негативные эффекты
        player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));

        // Расходуем заряд
        data.phoenixCharges--;
        plugin.getPlayerDataManager().savePlayer(data);

        // Эффекты воскрешения
        player.sendMessage(ChatColor.RED + "🔥 ФЕНИКС! Вы воскресли! (Осталось зарядов: " + data.phoenixCharges + ")");
        player.getWorld().spawnParticle(org.bukkit.Particle.FLAME, player.getLocation(), 50, 1, 1, 1, 0.1);
        player.getWorld().playSound(player.getLocation(), org.bukkit.Sound.ITEM_TOTEM_USE, 1.0f, 1.0f);

        // Восстановление через 2 секунды (чтобы не спавнились мобы)
        new BukkitRunnable() {
            @Override
            public void run() {
                if (player.isOnline()) {
                    player.setHealth(player.getMaxHealth());
                }
            }
        }.runTaskLater(plugin, 40L);
    }
}
