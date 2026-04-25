package com.example.smartphone.entities;

import com.example.smartphone.SmartPhonePlugin;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class GuardManager implements Listener {

    private final SmartPhonePlugin plugin;
    private final Map<UUID, GuardData> activeGuards;
    private static final double PROTECTION_RADIUS = 16.0;

    public GuardManager(SmartPhonePlugin plugin) {
        this.plugin = plugin;
        this.activeGuards = new HashMap<>();
    }

    public void spawnGuard(Player owner, Player protectTarget, GuardTier tier) {
        // [ПОЗИЦИЯ СПАВНА] Сзади игрока на расстоянии 1.5 блока
        Location loc = protectTarget.getLocation().subtract(protectTarget.getLocation().getDirection().multiply(1.5)).add(0, 0.5, 0);

        // [СОЗДАНИЕ СКЕЛЕТА]
        Skeleton guard = (Skeleton) protectTarget.getWorld().spawnEntity(loc, EntityType.SKELETON);

        // [НАСТРОЙКИ]
        guard.setCustomName(tier.getDisplayName());
        guard.setCustomNameVisible(true);
        guard.setAI(true);
        guard.setCanPickupItems(false);
        guard.setSilent(false); // Звуки стрельбы

        // [ЗДОРОВЬЕ]
        double health = tier.getHealth();
        guard.getAttribute(Attribute.MAX_HEALTH).setBaseValue(health);
        guard.setHealth(health);

        // [БРОНЯ И ОРУЖИЕ - УЛУЧШЕННАЯ ВЕРСИЯ С МЕЧАМИ]
        EntityEquipment equipment = guard.getEquipment();
        if (equipment != null) {
            if (tier == GuardTier.ELITE) {
                // [ЭЛИТА] Полная кожаная броня + алмазный меч
                equipment.setHelmet(createColoredArmor(Material.LEATHER_HELMET, org.bukkit.Color.PURPLE));
                equipment.setChestplate(createColoredArmor(Material.LEATHER_CHESTPLATE, org.bukkit.Color.PURPLE));
                equipment.setLeggings(createColoredArmor(Material.LEATHER_LEGGINGS, org.bukkit.Color.PURPLE));
                equipment.setBoots(createColoredArmor(Material.LEATHER_BOOTS, org.bukkit.Color.PURPLE));
                
                ItemStack sword = new ItemStack(Material.DIAMOND_SWORD);
                ItemMeta swordMeta = sword.getItemMeta();
                if (swordMeta != null) {
                    swordMeta.setDisplayName(ChatColor.DARK_PURPLE + "Меч Элиты");
                    swordMeta.addEnchant(org.bukkit.enchantments.Enchantment.SHARPNESS, 5, true);
                    swordMeta.addEnchant(org.bukkit.enchantments.Enchantment.FIRE_ASPECT, 2, true);
                    swordMeta.addEnchant(org.bukkit.enchantments.Enchantment.KNOCKBACK, 2, true);
                    sword.setItemMeta(swordMeta);
                }
                equipment.setItemInMainHand(sword);

                // [УСИЛЕНИЯ]
                guard.addPotionEffect(new PotionEffect(PotionEffectType.getByName("SPEED"), Integer.MAX_VALUE, 1));
                guard.addPotionEffect(new PotionEffect(PotionEffectType.getByName("RESISTANCE"), Integer.MAX_VALUE, 1));
                
            } else if (tier == GuardTier.PROFESSIONAL) {
                // [ПРОФЕССИОНАЛ] Кожаная броня + железный меч
                equipment.setHelmet(createColoredArmor(Material.LEATHER_HELMET, org.bukkit.Color.ORANGE));
                equipment.setChestplate(createColoredArmor(Material.LEATHER_CHESTPLATE, org.bukkit.Color.ORANGE));
                equipment.setLeggings(createColoredArmor(Material.LEATHER_LEGGINGS, org.bukkit.Color.ORANGE));
                equipment.setBoots(createColoredArmor(Material.LEATHER_BOOTS, org.bukkit.Color.ORANGE));
                
                ItemStack sword = new ItemStack(Material.IRON_SWORD);
                ItemMeta swordMeta = sword.getItemMeta();
                if (swordMeta != null) {
                    swordMeta.setDisplayName(ChatColor.GOLD + "Меч Профессионала");
                    swordMeta.addEnchant(org.bukkit.enchantments.Enchantment.SHARPNESS, 2, true);
                    swordMeta.addEnchant(org.bukkit.enchantments.Enchantment.FIRE_ASPECT, 1, true);
                    sword.setItemMeta(swordMeta);
                }
                equipment.setItemInMainHand(sword);
                
            } else {
                // [ОБЫЧНЫЙ] Кожаная броня + каменный меч
                equipment.setHelmet(createColoredArmor(Material.LEATHER_HELMET, org.bukkit.Color.BLUE));
                equipment.setChestplate(createColoredArmor(Material.LEATHER_CHESTPLATE, org.bukkit.Color.BLUE));
                equipment.setLeggings(createColoredArmor(Material.LEATHER_LEGGINGS, org.bukkit.Color.BLUE));
                equipment.setBoots(createColoredArmor(Material.LEATHER_BOOTS, org.bukkit.Color.BLUE));
                
                ItemStack sword = new ItemStack(Material.STONE_SWORD);
                ItemMeta swordMeta = sword.getItemMeta();
                if (swordMeta != null) {
                    swordMeta.setDisplayName(ChatColor.BLUE + "Меч Охранника");
                    swordMeta.addEnchant(org.bukkit.enchantments.Enchantment.SHARPNESS, 1, true);
                    sword.setItemMeta(swordMeta);
                }
                equipment.setItemInMainHand(sword);
            }
        }

        // [ДАННЫЕ]
        UUID guardUUID = guard.getUniqueId();
        int lifetime = tier.getLifetime();

        activeGuards.put(guardUUID, new GuardData(guardUUID, owner.getUniqueId(), protectTarget.getUniqueId(), tier, lifetime));

        // [СЛЕДОВАНИЕ ЗА ЦЕЛЬЮ] Охранник следует за игроком (оптимизировано)
        new BukkitRunnable() {
            @Override
            public void run() {
                if (guard.isValid() && protectTarget.isOnline()) {
                    Location targetLoc = protectTarget.getLocation();
                    Location guardLoc = guard.getLocation();

                    // [ПРОВЕРКА МИРА] Если миры разные - телепортируем
                    if (!guardLoc.getWorld().equals(targetLoc.getWorld())) {
                        guard.teleport(targetLoc);
                        return;
                    }

                    // [РАССТОЯНИЕ] Используем distanceSquared для производительности
                    double distanceSquared = guardLoc.distanceSquared(targetLoc);

                    // [ТЕЛЕПОРТ ЕСЛИ ДАЛЕКО] Если больше 5 блоков (25 в квадрате)
                    if (distanceSquared > 25) {
                        // [ПОЗИЦИЯ] Сзади игрока на 1.5 блока
                        Location newLoc = targetLoc.subtract(targetLoc.getDirection().multiply(1.5)).add(0, 0.5, 0);
                        guard.teleport(newLoc);
                    }
                    // [ПОВОРОТ] Если близко - поворачиваем в сторону игрока
                    else if (distanceSquared > 2.25) { // 1.5 в квадрате
                        guard.getLocation().setDirection(targetLoc.toVector().subtract(guardLoc.toVector()));
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 20L); // Проверка каждые 1 секунду (было 0.5с)

        // [ЗАДАЧА ПОИСКА ВРАГОВ] Охранник сам ищет враждебных мобов и игроков (оптимизировано)
        final UUID guardUUIDFinal = guardUUID;
        new BukkitRunnable() {
            @Override
            public void run() {
                GuardData data = activeGuards.get(guardUUIDFinal);
                if (!guard.isValid() || !protectTarget.isOnline() || data == null) {
                    cancel();
                    return;
                }

                // [ПРОВЕРКА МИРА]
                if (!guard.getWorld().equals(protectTarget.getWorld())) {
                    return;
                }

                // [ПОИСК ВРАГОВ В РАДИУСЕ] Уменьшен радиус для производительности
                List<Entity> nearby = guard.getNearbyEntities(8, 4, 8);

                for (Entity entity : nearby) {
                    // [МОБЫ]
                    if (entity instanceof Monster) {
                        Monster monster = (Monster) entity;

                        // [НЕ АТАКОВАТЬ ЕСЛИ УЖЕ ЦЕЛЬ ОХРАННИКА]
                        if (guard.getTarget() == monster) {
                            continue;
                        }

                        // [АТАКА МОНСТРА]
                        guard.setTarget(monster);

                        // [СООБЩЕНИЕ]
                        Player owner = plugin.getServer().getPlayer(data.ownerUUID);
                        if (owner != null) {
                            owner.sendMessage(ChatColor.YELLOW + "Охранник атакует " + monster.getName() + "!");
                        }
                        break; // Атакуем только одного за тик
                    }
                    
                    // [ИГРОКИ] Атака игроков которые атакуют защищаемого
                    if (entity instanceof Player) {
                        Player player = (Player) entity;
                        
                        // [НЕ АТАКОВАТЬ ВЛАДЕЛЬЦА]
                        if (player.getUniqueId().equals(data.ownerUUID)) {
                            continue;
                        }
                        
                        // [НЕ АТАКОВАТЬ ЗАЩИЩАЕМОГО]
                        if (player.getUniqueId().equals(data.protectTargetUUID)) {
                            continue;
                        }

                        // [АТАКА ИГРОКА]
                        if (guard.getTarget() == null) {
                            guard.setTarget(player);
                            
                            // [СООБЩЕНИЕ]
                            Player owner = plugin.getServer().getPlayer(data.ownerUUID);
                            if (owner != null) {
                                owner.sendMessage(ChatColor.RED + "Охранник атакует игрока " + player.getName() + "!");
                            }
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 100L, 40L); // Проверка каждые 2 секунды (было 1с)

        // [УДАЛЕНИЕ ПО ИСТЕЧЕНИИ] Только если время > 0
        if (lifetime > 0) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (guard.isValid()) {
                        guard.remove();
                        activeGuards.remove(guardUUID);
                        owner.sendMessage(ChatColor.YELLOW + "Время охраны истекло!");

                        if (protectTarget.isOnline() && !protectTarget.equals(owner)) {
                            protectTarget.sendMessage(ChatColor.YELLOW + "Охранник, нанятый игроком " + owner.getName() + ", исчез!");
                        }
                    }
                }
            }.runTaskLater(plugin, lifetime * 20L);
        }

        // [СООБЩЕНИЯ]
        protectTarget.sendMessage(ChatColor.GREEN + "✓ " + owner.getName() + " нанял охрану для вас! (" + tier.getName() + ", HP: " + (int)health + ")");
        owner.sendMessage(ChatColor.GREEN + "✓ Охранник нанят! HP: " + (int)health + ", вооружён мечом!");
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
    public void onDamage(EntityDamageByEntityEvent event) {
        // [ЗАЩИТА ЦЕЛИ]
        if (event.getEntity() instanceof Player) {
            Player victim = (Player) event.getEntity();
            Entity damager = event.getDamager();

            for (GuardData data : activeGuards.values()) {
                if (data.protectTargetUUID.equals(victim.getUniqueId())) {
                    Entity guard = plugin.getServer().getEntity(data.guardUUID);

                    if (guard instanceof Skeleton) {
                        // [НЕ АТАКОВАТЬ ХОЗЯИНА]
                        if (damager.getUniqueId().equals(data.ownerUUID)) {
                            continue;
                        }

                        // [НЕ АТАКОВАТЬ СЕБЯ]
                        if (damager.getUniqueId().equals(guard.getUniqueId())) {
                            continue;
                        }

                        // [НЕ АТАКОВАТЬ ЗАЩИЩАЕМОГО]
                        if (damager.getUniqueId().equals(data.protectTargetUUID)) {
                            continue;
                        }

                        // [АТАКА ОБИДЧИКА]
                        if (damager instanceof LivingEntity) {
                            LivingEntity livingDamager = (LivingEntity) damager;
                            Skeleton skeleton = (Skeleton) guard;

                            // [ПРОВЕРКА ЧТО ЦЕЛЬ НЕ МЫ]
                            if (skeleton.getTarget() != livingDamager) {
                                skeleton.setTarget(livingDamager);

                                // [СООБЩЕНИЕ]
                                Player owner = plugin.getServer().getPlayer(data.ownerUUID);
                                if (owner != null) {
                                    owner.sendMessage(ChatColor.RED + "Ваш охранник атакует " + damager.getName() + "!");
                                }

                                // [ОЧИСТКА ЦЕЛИ ЧЕРЕЗ 10 СЕК]
                                new BukkitRunnable() {
                                    @Override
                                    public void run() {
                                        if (guard.isValid() && skeleton.getTarget() == livingDamager) {
                                            skeleton.setTarget(null);
                                        }
                                    }
                                }.runTaskLater(plugin, 200L);
                            }
                        }
                    }
                }
            }
        }

        // [УРОН ОХРАННИКУ]
        if (activeGuards.containsKey(event.getEntity().getUniqueId())) {
            Entity guard = event.getEntity();
            GuardData data = activeGuards.get(guard.getUniqueId());

            if (event.getDamager() instanceof LivingEntity) {
                // [ПОКАЗАТЬ ХП]
                double currentHealth = ((LivingEntity) guard).getHealth();
                double maxHealth = ((LivingEntity) guard).getAttribute(Attribute.MAX_HEALTH).getValue();

                Player owner = plugin.getServer().getPlayer(data.ownerUUID);
                if (owner != null) {
                    owner.sendMessage(ChatColor.YELLOW + "Охранник ранен! HP: " + (int)currentHealth + "/" + (int)maxHealth);
                }
            }
        }
    }

    @EventHandler
    public void onGuardAttack(EntityDamageByEntityEvent event) {
        // [ЕСЛИ ОХРАННИК НАНОСИТ УРОН]
        if (activeGuards.containsKey(event.getDamager().getUniqueId())) {
            UUID guardUUID = event.getDamager().getUniqueId();
            GuardData data = activeGuards.get(guardUUID);

            if (data == null) {
                return;
            }

            // [НЕ АТАКОВАТЬ ХОЗЯИНА]
            if (event.getEntity().getUniqueId().equals(data.ownerUUID)) {
                event.setCancelled(true);
                return;
            }

            // [НЕ АТАКОВАТЬ ЗАЩИЩАЕМОГО]
            if (event.getEntity().getUniqueId().equals(data.protectTargetUUID)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onTarget(EntityTargetEvent event) {
        // [ЕСЛИ ЦЕЛЬ МЕНЯЕТ ОХРАННИК]
        if (!activeGuards.containsKey(event.getEntity().getUniqueId())) {
            return;
        }

        GuardData data = activeGuards.get(event.getEntity().getUniqueId());

        if (event.getTarget() == null) {
            return;
        }

        // [НЕ АГРИТЬСЯ НА ХОЗЯИНА]
        if (event.getTarget().getUniqueId().equals(data.ownerUUID)) {
            event.setCancelled(true);
            return;
        }

        // [НЕ АГРИТЬСЯ НА ЗАЩИЩАЕМОГО]
        if (event.getTarget().getUniqueId().equals(data.protectTargetUUID)) {
            event.setCancelled(true);
            return;
        }
    }

    @EventHandler
    public void onDeath(EntityDeathEvent event) {
        Entity entity = event.getEntity();
        if (!activeGuards.containsKey(entity.getUniqueId())) {
            return;
        }

        GuardData data = activeGuards.remove(entity.getUniqueId());
        Player owner = plugin.getServer().getPlayer(data.ownerUUID);
        Player protectTarget = plugin.getServer().getPlayer(data.protectTargetUUID);

        if (owner != null) {
            owner.sendMessage(ChatColor.RED + "Ваша охрана (" + data.tier.getName() + ") погибла!");
        }

        if (protectTarget != null && !protectTarget.equals(owner)) {
            protectTarget.sendMessage(ChatColor.RED + "Ваш охранник погиб!");
        }
        
        // [ВЫПАДЕНИЕ ПРЕДМЕТОВ] Отключаем выпадение брони и оружия
        event.getDrops().clear();
        event.setDroppedExp(0);
    }

    public void removeGuard(Player owner) {
        for (Map.Entry<UUID, GuardData> entry : activeGuards.entrySet()) {
            if (entry.getValue().ownerUUID.equals(owner.getUniqueId())) {
                Entity guard = plugin.getServer().getEntity(entry.getKey());
                if (guard != null) {
                    guard.remove();
                }
                activeGuards.remove(entry.getKey());
                return;
            }
        }
    }

    public void removeAllGuards() {
        for (GuardData data : activeGuards.values()) {
            Entity entity = plugin.getServer().getEntity(data.guardUUID);
            if (entity != null) {
                entity.remove();
            }
        }
        activeGuards.clear();
    }

    private static class GuardData {
        public final UUID guardUUID;
        public final UUID ownerUUID;
        public final UUID protectTargetUUID;
        public final GuardTier tier;
        public final int remainingTime; // -1 = бесконечно

        public GuardData(UUID guardUUID, UUID ownerUUID, UUID protectTargetUUID, GuardTier tier, int remainingTime) {
            this.guardUUID = guardUUID;
            this.ownerUUID = ownerUUID;
            this.protectTargetUUID = protectTargetUUID;
            this.tier = tier;
            this.remainingTime = remainingTime;
        }
    }

    public enum GuardTier {
        BASIC(
            "§b§l🛡️ Охранник",
            "Обычная",
            100,
            30.0,
            300,
            org.bukkit.Color.BLUE
        ),
        PROFESSIONAL(
            "§e§l🛡️ Профессионал",
            "Профессионал",
            250,
            50.0,
            600,
            org.bukkit.Color.ORANGE
        ),
        ELITE(
            "§5§l🛡️ ЭЛИТА",
            "Элита",
            500,
            100.0,
            900,
            org.bukkit.Color.PURPLE
        ),
        ETERNAL(
            "§6§l🛡️ ВЕЧНЫЙ",
            "Вечная",
            1000,
            50.0,
            -1, // -1 = бесконечно
            org.bukkit.Color.ORANGE
        );

        private final String displayName;
        private final String name;
        private final double cost;
        private final double health;
        private final int lifetime;
        private final org.bukkit.Color color;

        GuardTier(String displayName, String name, double cost, double health, int lifetime, org.bukkit.Color color) {
            this.displayName = displayName;
            this.name = name;
            this.cost = cost;
            this.health = health;
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

        public int getLifetime() {
            return lifetime;
        }

        public org.bukkit.Color getColor() {
            return color;
        }
    }
}
