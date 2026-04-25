package com.example.smartphone.listeners;

import com.example.smartphone.SmartPhonePlugin;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

/**
 * Слушатель огненной палочки с откидыванием (ПКМ)
 */
public class BlazeRodListener implements Listener {

    private final SmartPhonePlugin plugin;

    public BlazeRodListener(SmartPhonePlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Откидывание при ПКМ по мобу/игроку огненной палочкой
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof LivingEntity)) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        // Проверка на огненную палочку
        if (item.getType() != Material.BLAZE_ROD) {
            return;
        }

        // Проверка названия (наша покупная палочка)
        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            String displayName = ChatColor.stripColor(item.getItemMeta().getDisplayName());
            if (!displayName.equals("🔥 Палка")) {
                return;
            }
        } else {
            return;
        }

        // Отменяем взаимодействие (чтобы не открывались инвентари и т.д.)
        event.setCancelled(true);

        // Получаем цель
        LivingEntity livingTarget = (LivingEntity) event.getRightClicked();

        // [ОТКИДЫВАНИЕ 255 УРОВНЯ]
        Vector direction = player.getLocation().getDirection();
        Vector knockback = direction.multiply(5.0); // Сильное откидывание
        knockback.setY(2.0); // Подбрасывание вверх

        livingTarget.setVelocity(knockback);

        // [ЭФФЕКТЫ]
        livingTarget.setFireTicks(100); // Поджигание на 5 секунд

        // Частицы
        livingTarget.getWorld().spawnParticle(
            org.bukkit.Particle.FLAME,
            livingTarget.getLocation(),
            20,
            0.3,
            0.3,
            0.3,
            0.1
        );

        livingTarget.getWorld().playSound(
            livingTarget.getLocation(),
            org.bukkit.Sound.ENTITY_BLAZE_SHOOT,
            1.0f,
            1.0f
        );

        player.sendMessage(ChatColor.RED + "🔥 Откидывание 255 уровня!");
    }
}
