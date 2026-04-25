package com.example.smartphone.managers;

import com.example.smartphone.SmartPhonePlugin;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class TaxiManager {

    private final SmartPhonePlugin plugin;

    public TaxiManager(SmartPhonePlugin plugin) {
        this.plugin = plugin;
    }

    public void startTaxiRide(Player player, Location destination, double cost) {
        // [ПРОВЕРКА БАЛАНСА]
        if (!plugin.getEconomyManager().hasBalance(player.getUniqueId(), cost)) {
            player.sendMessage(ChatColor.RED + "Недостаточно денег! Нужно: " + cost + " монет");
            return;
        }

        // [СООБЩЕНИЯ]
        player.sendMessage(ChatColor.GREEN + "✓ Такси вызвано! Отправляемся...");
        player.sendMessage(ChatColor.YELLOW + "В пути...");
        
        // [ЗВУК]
        player.playSound(player.getLocation(), Sound.ENTITY_MINECART_RIDING, 1.0f, 1.0f);
        
        // [ТЕЛЕПОРТАЦИЯ С ЗАДЕРЖКОЙ]
        Location startLoc = player.getLocation();
        final int[] progress = {0};
        
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancel();
                    return;
                }
                
                progress[0]++;
                
                // [ЧАСТИЦЫ В ПУТИ]
                player.getWorld().spawnParticle(
                    Particle.CLOUD,
                    player.getLocation().add(0, 0.5, 0),
                    5, 0.3, 0.3, 0.3, 0.02
                );
                
                // [ВРЕМЯ В ПУТИ - 3 СЕКУНДЫ]
                if (progress[0] >= 60) {
                    // [ПРИБЫТИЕ]
                    player.teleport(destination);
                    
                    // [ЭФФЕКТЫ ПРИБЫТИЯ]
                    player.getWorld().spawnParticle(
                        Particle.HAPPY_VILLAGER,
                        player.getLocation().add(0, 1, 0),
                        20, 0.5, 0.5, 0.5, 0.1
                    );
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.5f);
                    
                    // [ОПЛАТА]
                    plugin.getEconomyManager().withdraw(player.getUniqueId(), cost);
                    
                    // [СООБЩЕНИЕ]
                    player.sendMessage(ChatColor.GREEN + "✓ Вы прибыли! С вас: " + cost + " монет");
                    player.sendMessage(ChatColor.YELLOW + "Спасибо что выбрали наше такси!");
                    
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
}
