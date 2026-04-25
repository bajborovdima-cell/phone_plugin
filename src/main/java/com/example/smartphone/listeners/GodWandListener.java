package com.example.smartphone.listeners;

import com.example.smartphone.SmartPhonePlugin;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

/**
 * Слушатель палочки бога
 */
public class GodWandListener implements Listener {

    private final SmartPhonePlugin plugin;

    public GodWandListener(SmartPhonePlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Обработка клика палочкой
     */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        // Проверка предмета в руке
        if (event.getItem() == null) return;
        if (event.getItem().getType() != Material.BLAZE_ROD) return;

        // Проверка названия предмета
        if (!event.getItem().hasItemMeta()) return;
        if (!event.getItem().getItemMeta().hasDisplayName()) return;

        String displayName = ChatColor.stripColor(event.getItem().getItemMeta().getDisplayName());
        if (!displayName.equals("⚡ Палочка Бога")) return;

        // Проверка действия (ПКМ)
        if (event.getAction() != Action.RIGHT_CLICK_AIR && 
            event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        event.setCancelled(true);

        // Получение цели (игрок или моб)
        Entity target = player.getTargetEntity(50); // 50 блоков

        if (target == null) {
            player.sendMessage(ChatColor.RED + "Нет цели в поле зрения!");
            return;
        }

        if (!(target instanceof LivingEntity)) {
            player.sendMessage(ChatColor.RED + "Цель должна быть живым существом!");
            return;
        }

        LivingEntity livingTarget = (LivingEntity) target;

        // [ОТКИДЫВАНИЕ]
        Vector direction = player.getLocation().getDirection();
        Vector knockback = direction.multiply(5.0); // Сильное откидывание
        knockback.setY(2.0); // Подбрасывание вверх

        livingTarget.setVelocity(knockback);

        // [ЭФФЕКТЫ]
        livingTarget.damage(1.0, player); // Небольшой урон для активации эффектов
        livingTarget.setFireTicks(20); // Поджигание на 1 секунду

        // Частицы
        livingTarget.getWorld().spawnParticle(
            org.bukkit.Particle.EXPLOSION_EMITTER,
            livingTarget.getLocation(),
            1,
            0.5,
            0.5,
            0.5,
            0.1
        );

        // Звук
        livingTarget.getWorld().playSound(
            livingTarget.getLocation(),
            org.bukkit.Sound.ENTITY_GENERIC_EXPLODE,
            1.0f,
            1.0f
        );

        player.sendMessage(ChatColor.GREEN + "✓ Цель откинута!");
    }
}
