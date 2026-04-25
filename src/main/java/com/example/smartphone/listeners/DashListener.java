package com.example.smartphone.listeners;

import com.example.smartphone.SmartPhonePlugin;
import com.example.smartphone.data.PlayerDataManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.block.Action;
import org.bukkit.util.Vector;

/**
 * Слушатель перка Рывок - мгновенный рывок вперёд
 */
public class DashListener implements Listener {

    private final SmartPhonePlugin plugin;

    public DashListener(SmartPhonePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        // Проверяем ПКМ
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();
        
        // Не дашим если держим телефон (чтобы не конфликтовать с открытием меню)
        if (player.getInventory().getItemInMainHand().getType() == Material.LIGHT_BLUE_DYE) {
            return;
        }

        PlayerDataManager.PlayerData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());

        // Проверяем что перк включён
        if (!data.dashEnabled) {
            return;
        }
        
        // Проверяем уровень рывка
        if (data.dashLevel <= 0) {
            return;
        }

        // Проверяем кулдаун (3 секунды)
        long currentTime = System.currentTimeMillis();
        if (currentTime - data.lastDashTime < 3000) {
            long remaining = 3 - (currentTime - data.lastDashTime) / 1000;
            player.sendMessage(ChatColor.YELLOW + "⚡ Кулдаун рывка: " + remaining + " сек.");
            return;
        }

        // Отменяем взаимодействие (чтобы не ставить блоки и т.д.)
        event.setCancelled(true);

        // Выполняем рывок
        Vector direction = player.getLocation().getDirection();
        double distance = data.dashLevel * 5; // 5/10/15 блоков
        
        // Нормализуем и умножаем на расстояние
        direction.normalize().multiply(distance * 0.3);
        direction.setY(0.2); // Небольшой подброс
        
        player.setVelocity(direction);
        
        // Обновляем время
        data.lastDashTime = currentTime;
        plugin.getPlayerDataManager().savePlayer(data);

        // Эффекты рывка
        player.getWorld().spawnParticle(org.bukkit.Particle.CLOUD, player.getLocation(), 30, 0.5, 0.5, 0.5, 0.1);
        player.getWorld().playSound(player.getLocation(), org.bukkit.Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1.0f, 1.0f);
        
        player.sendMessage(ChatColor.YELLOW + "⚡ Рывок на " + distance + " блоков!");
    }
}
