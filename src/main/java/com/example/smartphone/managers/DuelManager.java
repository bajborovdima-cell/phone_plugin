package com.example.smartphone.managers;

import com.example.smartphone.SmartPhonePlugin;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

/**
 * Менеджер режима дуэли
 */
public class DuelManager {

    private final SmartPhonePlugin plugin;
    private final Map<UUID, UUID> pendingInvites; // Кто кого вызвал
    private final Map<UUID, Arena> activeDuels; // Активные дуэли
    private final List<Arena> arenas;

    public DuelManager(SmartPhonePlugin plugin) {
        this.plugin = plugin;
        this.pendingInvites = new HashMap<>();
        this.activeDuels = new HashMap<>();
        this.arenas = new ArrayList<>();
        
        // Создаём тестовую арену
        createDefaultArena();
    }

    /**
     * Создать арену по умолчанию
     */
    private void createDefaultArena() {
        // Арена будет создаваться динамически при запросе дуэли
    }

    /**
     * Вызвать игрока на дуэль
     */
    public boolean challenge(Player challenger, Player target) {
        if (challenger.equals(target)) {
            challenger.sendMessage(ChatColor.RED + "Нельзя вызвать себя на дуэль!");
            return false;
        }

        if (activeDuels.containsKey(challenger.getUniqueId()) || 
            activeDuels.containsKey(target.getUniqueId())) {
            challenger.sendMessage(ChatColor.RED + "Один из игроков уже в дуэли!");
            return false;
        }

        pendingInvites.put(target.getUniqueId(), challenger.getUniqueId());
        
        challenger.sendMessage(ChatColor.GREEN + "✓ Вы вызвали " + target.getName() + " на дуэль!");
        target.sendMessage(ChatColor.YELLOW + "⚔️ " + challenger.getName() + " вызывает вас на дуэль!");
        target.sendMessage(ChatColor.YELLOW + "Используйте: /duel accept или /duel deny");
        
        // Авто-отмена через 30 секунд
        new BukkitRunnable() {
            @Override
            public void run() {
                if (pendingInvites.containsKey(target.getUniqueId())) {
                    pendingInvites.remove(target.getUniqueId());
                    challenger.sendMessage(ChatColor.RED + "Время ожидания истекло!");
                }
            }
        }.runTaskLater(plugin, 600L);
        
        return true;
    }

    /**
     * Принять вызов
     */
    public boolean accept(Player target) {
        UUID challengerUUID = pendingInvites.remove(target.getUniqueId());
        
        if (challengerUUID == null) {
            target.sendMessage(ChatColor.RED + "У вас нет вызовов на дуэль!");
            return false;
        }

        Player challenger = plugin.getServer().getPlayer(challengerUUID);
        
        if (challenger == null || !challenger.isOnline()) {
            target.sendMessage(ChatColor.RED + "Игрок вышел из игры!");
            return false;
        }

        // Начинаем дуэль
        startDuel(challenger, target);
        return true;
    }

    /**
     * Отклонить вызов
     */
    public boolean deny(Player target) {
        UUID challengerUUID = pendingInvites.remove(target.getUniqueId());
        
        if (challengerUUID == null) {
            target.sendMessage(ChatColor.RED + "У вас нет вызовов на дуэль!");
            return false;
        }

        Player challenger = plugin.getServer().getPlayer(challengerUUID);
        if (challenger != null && challenger.isOnline()) {
            challenger.sendMessage(ChatColor.RED + target.getName() + " отклонил ваш вызов!");
        }
        
        return true;
    }

    /**
     * Начать дуэль
     */
    private void startDuel(Player challenger, Player target) {
        // Создаём арену
        Location spawnLoc = challenger.getLocation();
        Arena arena = createArena(spawnLoc);
        
        // Телепортируем игроков
        challenger.teleport(arena.getSpawn1());
        target.teleport(arena.getSpawn2());
        
        // Сохраняем инвентари
        arena.saveInventories(challenger, target);
        
        // Даём экипировку
        giveDuelKit(challenger);
        giveDuelKit(target);
        
        // Регистрируем дуэль
        activeDuels.put(challenger.getUniqueId(), arena);
        activeDuels.put(target.getUniqueId(), arena);
        
        // Объявление
        challenger.sendMessage(ChatColor.GOLD + "⚔️ ДУЭЛЬ НАЧАЛАСЬ!");
        target.sendMessage(ChatColor.GOLD + "⚔️ ДУЭЛЬ НАЧАЛАСЬ!");
        
        Bukkit.broadcastMessage(ChatColor.GOLD + "⚔️ " + challenger.getName() + " vs " + target.getName() + " — Дуэль началась!");
    }

    /**
     * Создать арену
     */
    private Arena createArena(Location center) {
        World world = center.getWorld();
        
        // Создаём платформу 10x10
        int x = center.getBlockX();
        int y = center.getBlockY() - 1;
        int z = center.getBlockZ();
        
        for (int dx = -5; dx < 5; dx++) {
            for (int dz = -5; dz < 5; dz++) {
                world.getBlockAt(x + dx, y, z + dz).setType(Material.OAK_PLANKS);
            }
        }
        
        // Стены
        for (int dy = 1; dy <= 3; dy++) {
            for (int dx = -5; dx < 5; dx++) {
                world.getBlockAt(x + dx, y + dy, z - 5).setType(Material.GLASS);
                world.getBlockAt(x + dx, y + dy, z + 4).setType(Material.GLASS);
            }
            for (int dz = -5; dz < 5; dz++) {
                world.getBlockAt(x - 5, y + dy, z + dz).setType(Material.GLASS);
                world.getBlockAt(x + 4, y + dy, z + dz).setType(Material.GLASS);
            }
        }
        
        Location spawn1 = new Location(world, x - 3, y + 1, z);
        Location spawn2 = new Location(world, x + 2, y + 1, z);
        
        return new Arena(spawn1, spawn2, world, x, y, z);
    }

    /**
     * Выдать комплект для дуэли
     */
    private void giveDuelKit(Player player) {
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
        
        // Алмазный меч
        ItemStack sword = new ItemStack(Material.DIAMOND_SWORD);
        player.getInventory().addItem(sword);
        
        // Золотое яблоко
        ItemStack apple = new ItemStack(Material.GOLDEN_APPLE, 3);
        player.getInventory().addItem(apple);
        
        player.updateInventory();
    }

    /**
     * Обработка смерти в дуэли
     */
    public void onPlayerDeath(Player player) {
        UUID uuid = player.getUniqueId();
        Arena arena = activeDuels.get(uuid);
        
        if (arena == null) return;
        
        // Определяем победителя
        Player winner = null;
        Player loser = player;
        
        for (UUID participant : activeDuels.keySet()) {
            if (activeDuels.get(participant).equals(arena) && !participant.equals(uuid)) {
                winner = plugin.getServer().getPlayer(participant);
                break;
            }
        }
        
        if (winner != null) {
            Bukkit.broadcastMessage(ChatColor.GOLD + "🏆 " + winner.getName() + " побеждает в дуэли!");
            winner.sendMessage(ChatColor.GREEN + "✓ Вы победили в дуэли!");
        }
        
        // Завершаем дуэль
        endDuel(arena, winner, loser);
    }

    /**
     * Завершить дуэль
     */
    private void endDuel(Arena arena, Player winner, Player loser) {
        // Возвращаем инвентари
        arena.restoreInventories(winner, loser);
        
        // Телепортируем на спавн
        if (winner != null && winner.isOnline()) {
            winner.teleport(plugin.getServer().getWorlds().get(0).getSpawnLocation());
        }
        if (loser != null && loser.isOnline()) {
            loser.teleport(plugin.getServer().getWorlds().get(0).getSpawnLocation());
        }
        
        // Удаляем из активных
        for (UUID uuid : new HashSet<>(activeDuels.keySet())) {
            if (activeDuels.get(uuid).equals(arena)) {
                activeDuels.remove(uuid);
            }
        }
        
        // Разрушаем арену
        arena.destroy();
    }

    /**
     * Покинуть дуэль
     */
    public boolean leave(Player player) {
        UUID uuid = player.getUniqueId();
        Arena arena = activeDuels.get(uuid);
        
        if (arena == null) {
            player.sendMessage(ChatColor.RED + "Вы не в дуэли!");
            return false;
        }
        
        // Считаем проигрышем
        Player opponent = null;
        for (UUID participant : activeDuels.keySet()) {
            if (activeDuels.get(participant).equals(arena) && !participant.equals(uuid)) {
                opponent = plugin.getServer().getPlayer(participant);
                break;
            }
        }
        
        endDuel(arena, opponent, player);
        player.sendMessage(ChatColor.YELLOW + "Вы покинули дуэль (считается поражением)");
        
        return true;
    }

    /**
     * Проверка: игрок в дуэли
     */
    public boolean isInDuel(Player player) {
        return activeDuels.containsKey(player.getUniqueId());
    }

    /**
     * Класс арены
     */
    public static class Arena {
        private final Location spawn1;
        private final Location spawn2;
        private final World world;
        private final int centerX, centerY, centerZ;
        private final Map<UUID, ItemStack[]> savedInventories;
        private final Map<UUID, ItemStack[]> savedArmor;

        public Arena(Location spawn1, Location spawn2, World world, int centerX, int centerY, int centerZ) {
            this.spawn1 = spawn1;
            this.spawn2 = spawn2;
            this.world = world;
            this.centerX = centerX;
            this.centerY = centerY;
            this.centerZ = centerZ;
            this.savedInventories = new HashMap<>();
            this.savedArmor = new HashMap<>();
        }

        public Location getSpawn1() { return spawn1; }
        public Location getSpawn2() { return spawn2; }

        public void saveInventories(Player p1, Player p2) {
            savedInventories.put(p1.getUniqueId(), p1.getInventory().getContents());
            savedInventories.put(p2.getUniqueId(), p2.getInventory().getContents());
            savedArmor.put(p1.getUniqueId(), p1.getInventory().getArmorContents());
            savedArmor.put(p2.getUniqueId(), p2.getInventory().getArmorContents());
        }

        public void restoreInventories(Player p1, Player p2) {
            if (p1 != null && p1.isOnline()) {
                p1.getInventory().setContents(savedInventories.get(p1.getUniqueId()));
                p1.getInventory().setArmorContents(savedArmor.get(p1.getUniqueId()));
                p1.updateInventory();
            }
            if (p2 != null && p2.isOnline()) {
                p2.getInventory().setContents(savedInventories.get(p2.getUniqueId()));
                p2.getInventory().setArmorContents(savedArmor.get(p2.getUniqueId()));
                p2.updateInventory();
            }
        }

        public void destroy() {
            // Разрушаем платформу
            for (int dx = -5; dx < 5; dx++) {
                for (int dz = -5; dz < 5; dz++) {
                    world.getBlockAt(centerX + dx, centerY, centerZ + dz).setType(Material.AIR);
                }
            }
            // Разрушаем стены
            for (int dy = 1; dy <= 3; dy++) {
                for (int dx = -5; dx < 5; dx++) {
                    world.getBlockAt(centerX + dx, centerY + dy, centerZ - 5).setType(Material.AIR);
                    world.getBlockAt(centerX + dx, centerY + dy, centerZ + 4).setType(Material.AIR);
                }
                for (int dz = -5; dz < 5; dz++) {
                    world.getBlockAt(centerX - 5, centerY + dy, centerZ + dz).setType(Material.AIR);
                    world.getBlockAt(centerX + 4, centerY + dy, centerZ + dz).setType(Material.AIR);
                }
            }
        }
    }
}
