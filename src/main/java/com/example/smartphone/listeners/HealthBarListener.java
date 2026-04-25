package com.example.smartphone.listeners;

import com.example.smartphone.SmartPhonePlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Слушатель сердечек (Health Bars) над мобами и игроками
 */
public class HealthBarListener implements Listener {

    private final Map<UUID, BossBar> bossBars;
    private final SmartPhonePlugin plugin;

    public HealthBarListener(SmartPhonePlugin plugin) {
        this.plugin = plugin;
        this.bossBars = new HashMap<>();
    }

    /**
     * Показывает полоску здоровья при взгляде на моба/игрока
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerMove(PlayerMoveEvent event) {
        // Проверяем каждые 0.5 секунды (чтобы не спамить)
        Player player = event.getPlayer();
        Entity target = getTargetEntity(player, 5.0); // 5 блоков

        if (target instanceof LivingEntity && !(target instanceof Player)) {
            showHealthBar(player, (LivingEntity) target);
        } else {
            hideHealthBar(player);
        }
    }

    /**
     * Показывает полоску здоровья при клике по мобу
     */
    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        Entity target = event.getRightClicked();

        if (target instanceof LivingEntity) {
            showHealthBar(player, (LivingEntity) target);
        }
    }

    /**
     * Обновляет полоску здоровья при получении урона
     */
    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof LivingEntity)) {
            return;
        }

        LivingEntity entity = (LivingEntity) event.getEntity();
        
        // Обновляем все полоски для этого существа
        for (Map.Entry<UUID, BossBar> entry : bossBars.entrySet()) {
            Player player = Bukkit.getPlayer(entry.getKey());
            if (player != null && player.getWorld().equals(entity.getWorld())) {
                if (player.getLocation().distance(entity.getLocation()) < 5.0) {
                    updateHealthBar(player, entity);
                }
            }
        }
    }

    /**
     * Показывает полоску здоровья
     */
    private void showHealthBar(Player player, LivingEntity entity) {
        UUID entityId = entity.getUniqueId();
        UUID playerId = player.getUniqueId();

        // Если уже есть полоска - обновляем
        if (bossBars.containsKey(playerId)) {
            updateHealthBar(player, entity);
            return;
        }

        // Создаём новую полоску
        String entityName = entity.getName();
        if (entity.getCustomName() != null) {
            entityName = entity.getCustomName();
        }

        double maxHealth = entity.getAttribute(Attribute.MAX_HEALTH) != null ? 
            entity.getAttribute(Attribute.MAX_HEALTH).getValue() : 20.0;
        
        BossBar bossBar = Bukkit.createBossBar(
            getHealthBarTitle(entityName, entity.getHealth(), maxHealth),
            getHealthBarColor(entity.getHealth(), maxHealth),
            BarStyle.SOLID
        );

        bossBar.addPlayer(player);
        bossBar.setVisible(true);
        bossBars.put(playerId, bossBar);

        // Скрываем через 3 секунды если игрок не двигается
        new BukkitRunnable() {
            @Override
            public void run() {
                if (bossBars.containsKey(playerId)) {
                    hideHealthBar(player);
                }
            }
        }.runTaskLater(plugin, 60L);
    }

    /**
     * Обновляет полоску здоровья
     */
    private void updateHealthBar(Player player, LivingEntity entity) {
        UUID playerId = player.getUniqueId();
        BossBar bossBar = bossBars.get(playerId);

        if (bossBar == null) {
            showHealthBar(player, entity);
            return;
        }

        String entityName = entity.getName();
        if (entity.getCustomName() != null) {
            entityName = entity.getCustomName();
        }

        double maxHealth = entity.getAttribute(Attribute.MAX_HEALTH) != null ? 
            entity.getAttribute(Attribute.MAX_HEALTH).getValue() : 20.0;

        bossBar.setTitle(getHealthBarTitle(entityName, entity.getHealth(), maxHealth));
        bossBar.setColor(getHealthBarColor(entity.getHealth(), maxHealth));

        double healthPercent = entity.getHealth() / maxHealth;
        bossBar.setProgress(Math.max(0.0, Math.min(1.0, healthPercent)));
    }

    /**
     * Скрывает полоску здоровья
     */
    private void hideHealthBar(Player player) {
        UUID playerId = player.getUniqueId();
        BossBar bossBar = bossBars.remove(playerId);

        if (bossBar != null) {
            bossBar.removeAll();
            bossBar.setVisible(false);
        }
    }

    /**
     * Получает целевое существо
     */
    private Entity getTargetEntity(Player player, double maxDistance) {
        var result = player.rayTraceEntities((int) maxDistance);
        return result != null ? result.getHitEntity() : null;
    }

    /**
     * Получает заголовок для полоски
     */
    private String getHealthBarTitle(String name, double health, double maxHealth) {
        int hearts = (int) Math.ceil(health / 2.0);
        int maxHearts = (int) Math.ceil(maxHealth / 2.0);
        return ChatColor.RED + name + " " + ChatColor.WHITE + hearts + "/" + maxHearts + " ❤";
    }

    /**
     * Получает цвет для полоски
     */
    private BarColor getHealthBarColor(double health, double maxHealth) {
        double percent = health / maxHealth;
        if (percent > 0.6) {
            return BarColor.GREEN;
        } else if (percent > 0.3) {
            return BarColor.YELLOW;
        } else {
            return BarColor.RED;
        }
    }

    /**
     * Очищает все полоски при выходе игрока
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        hideHealthBar(event.getPlayer());
    }

    /**
     * Очищает все полоски
     */
    public void cleanup() {
        for (BossBar bossBar : bossBars.values()) {
            bossBar.removeAll();
            bossBar.setVisible(false);
        }
        bossBars.clear();
    }
}
