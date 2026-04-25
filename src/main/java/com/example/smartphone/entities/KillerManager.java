package com.example.smartphone.entities;

import com.example.smartphone.SmartPhonePlugin;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Skeleton;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.entity.Player;

import java.util.*;

public class KillerManager implements Listener {

    private final SmartPhonePlugin plugin;
    private final Map<UUID, KillerData> activeKillers;

    public KillerManager(SmartPhonePlugin plugin) {
        this.plugin = plugin;
        this.activeKillers = new HashMap<>();
    }

    public void spawnKiller(Player owner, Player target) {
        spawnKiller(owner, target, KillerTier.BASIC);
    }

    public void spawnKiller(Player owner, Player target, KillerTier tier) {
        // [СПАВН РЯДОМ С ЦЕЛЬЮ]
        Location loc = target.getLocation().add(3, 0, 0);

        // [СОЗДАНИЕ СКЕЛЕТА]
        Skeleton killer = (Skeleton) target.getWorld().spawnEntity(loc, EntityType.SKELETON);

        // [НАСТРОЙКИ]
        killer.setCustomName(tier.getDisplayName());
        killer.setCustomNameVisible(true);
        killer.setAI(true);

        // [ЗДОРОВЬЕ]
        double health = tier.getHealth();
        killer.getAttribute(Attribute.MAX_HEALTH).setBaseValue(health);
        killer.setHealth(health);

        // [БРОНЯ]
        EntityEquipment equipment = killer.getEquipment();
        if (equipment != null) {
            equipment.setHelmet(createColoredArmor(org.bukkit.Material.LEATHER_HELMET, tier.getColor()));
            equipment.setChestplate(createColoredArmor(org.bukkit.Material.LEATHER_CHESTPLATE, tier.getColor()));
            equipment.setLeggings(createColoredArmor(org.bukkit.Material.LEATHER_LEGGINGS, tier.getColor()));
            equipment.setBoots(createColoredArmor(org.bukkit.Material.LEATHER_BOOTS, tier.getColor()));

            // [ОРУЖИЕ - МЕЧ]
            ItemStack sword = new ItemStack(org.bukkit.Material.DIAMOND_SWORD);
            ItemMeta meta = sword.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(tier.getDisplayName() + " - Меч");
                sword.setItemMeta(meta);
            }
            equipment.setItemInMainHand(sword);
        }
        
        // [ДАННЫЕ]
        UUID killerUUID = killer.getUniqueId();
        int lifetime = tier.getLifetime();
        
        activeKillers.put(killerUUID, new KillerData(killerUUID, owner.getUniqueId(), target.getUniqueId(), tier));
        
        // [АТАКА ЦЕЛИ]
        new BukkitRunnable() {
            @Override
            public void run() {
                if (killer.isValid() && target.isOnline()) {
                    ((Mob) killer).setTarget(target);
                }
            }
        }.runTaskLater(plugin, 20L);
        
        // [УДАЛЕНИЕ ПО ИСТЕЧЕНИИ]
        new BukkitRunnable() {
            @Override
            public void run() {
                if (killer.isValid()) {
                    killer.remove();
                    activeKillers.remove(killerUUID);
                    owner.sendMessage(ChatColor.YELLOW + "Время киллера истекло!");
                }
            }
        }.runTaskLater(plugin, lifetime * 20L);
    }

    private ItemStack createColoredArmor(Material material, org.bukkit.Color color) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null && meta instanceof org.bukkit.inventory.meta.LeatherArmorMeta) {
            ((org.bukkit.inventory.meta.LeatherArmorMeta) meta).setColor(color);
            item.setItemMeta(meta);
        }
        return item;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onTarget(EntityTargetEvent event) {
        if (!activeKillers.containsKey(event.getEntity().getUniqueId())) {
            return;
        }

        KillerData data = activeKillers.get(event.getEntity().getUniqueId());
        
        // [ПРИОРИТЕТ ЦЕЛИ]
        if (event.getTarget() != null && event.getTarget().getUniqueId().equals(data.targetUUID)) {
            event.setCancelled(false);
        }
    }

    @EventHandler
    public void onDeath(EntityDeathEvent event) {
        Entity entity = event.getEntity();
        if (!activeKillers.containsKey(entity.getUniqueId())) {
            return;
        }

        KillerData data = activeKillers.remove(entity.getUniqueId());
        Player owner = plugin.getServer().getPlayer(data.ownerUUID);
        
        if (owner != null) {
            owner.sendMessage(ChatColor.RED + "Ваш киллер (" + data.tier.getName() + ") погиб!");
        }
    }

    public void removeAllKillers() {
        for (KillerData data : activeKillers.values()) {
            Entity entity = plugin.getServer().getEntity(data.killerUUID);
            if (entity != null) {
                entity.remove();
            }
        }
        activeKillers.clear();
    }

    private static class KillerData {
        public final UUID killerUUID;
        public final UUID ownerUUID;
        public final UUID targetUUID;
        public final KillerTier tier;

        public KillerData(UUID killerUUID, UUID ownerUUID, UUID targetUUID, KillerTier tier) {
            this.killerUUID = killerUUID;
            this.ownerUUID = ownerUUID;
            this.targetUUID = targetUUID;
            this.tier = tier;
        }
    }

    public enum KillerTier {
        BASIC(
            "§4§l🔪 Киллер",
            "Обычный",
            500,
            20.0,
            5.0,
            120,
            org.bukkit.Color.RED
        ),
        PROFESSIONAL(
            "§6§l🔪 Профессионал",
            "Профессионал",
            1000,
            30.0,
            8.0,
            180,
            org.bukkit.Color.ORANGE
        ),
        LEGENDARY(
            "§5§l🔪 ЛЕГЕНДА",
            "Легенда",
            2500,
            50.0,
            12.0,
            300,
            org.bukkit.Color.PURPLE
        );

        private final String displayName;
        private final String name;
        private final double cost;
        private final double health;
        private final double damage;
        private final int lifetime;
        private final org.bukkit.Color color;

        KillerTier(String displayName, String name, double cost, double health, double damage, int lifetime, org.bukkit.Color color) {
            this.displayName = displayName;
            this.name = name;
            this.cost = cost;
            this.health = health;
            this.damage = damage;
            this.lifetime = lifetime;
            this.color = color;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getName() {
            return name;
        }

        public double getCost() {
            return cost;
        }

        public double getHealth() {
            return health;
        }

        public double getDamage() {
            return damage;
        }

        public int getLifetime() {
            return lifetime;
        }

        public org.bukkit.Color getColor() {
            return color;
        }
    }
}
