package com.example.smartphone.listeners;

import com.example.smartphone.SmartPhonePlugin;
import com.example.smartphone.data.PlayerDataManager;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

/**
 * Слушатель перков: урон, выносливость, bhop, двойной прыжок
 */
public class PerksListener implements Listener {

    private final SmartPhonePlugin plugin;

    public PerksListener(SmartPhonePlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Урон от перка (добавляет урон к атаке игрока)
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getDamager();
        PlayerDataManager.PlayerData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());

        // [УРОН ОТ ПЕРКА] Проверяем что перк включён и есть уровень
        if (data.damageLevel > 0 && data.damageEnabled) {
            double bonusDamage = data.damageLevel * 0.5; // +0.5 урона за уровень
            double originalDamage = event.getDamage();
            event.setDamage(originalDamage + bonusDamage);

            // Частицы урона
            if (event.getEntity() instanceof LivingEntity) {
                LivingEntity target = (LivingEntity) event.getEntity();
                target.getWorld().spawnParticle(
                    org.bukkit.Particle.DAMAGE_INDICATOR,
                    target.getLocation().add(0, 1, 0),
                    5,
                    0.3,
                    0.3,
                    0.3,
                    0.1
                );
            }
        }
    }

    /**
     * Bhop - скорость в прыжке
     */
    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        PlayerDataManager.PlayerData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());

        if (data.bhopLevel > 0 && player.isOnGround()) {
            // Сбрасываем эффект если на земле
            player.removePotionEffect(PotionEffectType.SPEED);
            return;
        }

        if (data.bhopLevel > 0 && !player.isOnGround()) {
            // [BHOP В ВОЗДУХЕ]
            int level = Math.min(data.bhopLevel, 5);
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 60, level - 1, false, false));
        }
    }

    /**
     * Высокий прыжок (через PlayerMoveEvent)
     */
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        PlayerDataManager.PlayerData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());

        if (data.doubleJumpLevel <= 0 || !data.doubleJumpEnabled) {
            return;
        }

        // Проверяем, игрок на земле
        if (player.isOnGround()) {
            data.canDoubleJump = true;
            return;
        }

        // Проверяем нажатие клавиши прыжка (пробел) по вертикальной скорости
        // Игрок нажал пробел если вертикальная скорость > 0.1
        double verticalVelocity = player.getVelocity().getY();
        if (verticalVelocity > 0.1 && data.canDoubleJump) {
            // [ВЫСОКИЙ ПРЫЖОК]
            double jumpStrength = 0.4 + (data.doubleJumpLevel * 0.15); // 0.55 / 0.7 / 0.85
            Vector velocity = player.getVelocity();
            velocity.setY(jumpStrength);
            player.setVelocity(velocity);

            // Частицы
            player.spawnParticle(org.bukkit.Particle.CLOUD, player.getLocation(), 20, 0.3, 0.3, 0.3, 0.1);

            data.canDoubleJump = false;
            plugin.getPlayerDataManager().savePlayer(data);
        }
    }

    /**
     * Выносливость - снижение получаемого урона
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onDamageTaken(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getEntity();
        PlayerDataManager.PlayerData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());

        // [ВЫНОСЛИВОСТЬ - СНИЖЕНИЕ УРОНА]
        if (data.staminaLevel > 0) {
            double reduction = data.staminaLevel * 0.05; // 5% за уровень, макс 50%
            event.setDamage(event.getDamage() * (1 - reduction));
        }
    }
}
