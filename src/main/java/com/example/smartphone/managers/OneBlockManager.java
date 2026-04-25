package com.example.smartphone.managers;

import com.example.smartphone.SmartPhonePlugin;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

/**
 * Менеджер режима "Один блок"
 * Общий остров для всех игроков в отдельном мире
 */
public class OneBlockManager {

    private final SmartPhonePlugin plugin;
    private final Set<UUID> playersOnIsland;
    private final List<Material> blockPool;
    private Location islandLocation;
    private World oneBlockWorld;
    
    // [ХРАНЕНИЕ ДОБЫТЫХ БЛОКОВ] Map<UUID, Map<Material, Integer>>
    private final Map<UUID, Map<Material, Integer>> minedBlocks;

    public OneBlockManager(SmartPhonePlugin plugin) {
        this.plugin = plugin;
        this.playersOnIsland = new HashSet<>();
        this.blockPool = loadBlockPool();
        this.minedBlocks = new HashMap<>();

        // Загрузка или создание мира
        new BukkitRunnable() {
            @Override
            public void run() {
                createOrLoadWorld();
            }
        }.runTaskLater(plugin, 1L);
    }

    /**
     * Загрузка пула блоков из конфига
     */
    private List<Material> loadBlockPool() {
        List<Material> blocks = new ArrayList<>();
        List<String> blockNames = plugin.getConfig().getStringList("oneblock.blocks");
        
        if (blockNames.isEmpty()) {
            // Блоки по умолчанию
            blocks.addAll(Arrays.asList(
                Material.DIRT,
                Material.STONE,
                Material.COAL_ORE,
                Material.IRON_ORE,
                Material.GOLD_ORE,
                Material.DIAMOND_ORE,
                Material.WHEAT,
                Material.OAK_LOG,
                Material.SAND,
                Material.GRAVEL,
                Material.CLAY,
                Material.REDSTONE_ORE,
                Material.LAPIS_ORE,
                Material.EMERALD_ORE,
                Material.OBSIDIAN,
                Material.NETHERRACK,
                Material.SOUL_SAND,
                Material.GLOWSTONE,
                Material.BOOKSHELF,
                Material.CRAFTING_TABLE
            ));
        } else {
            for (String name : blockNames) {
                try {
                    Material material = Material.valueOf(name.toUpperCase());
                    blocks.add(material);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Неверный блок в конфиге: " + name);
                }
            }
        }
        
        return blocks;
    }

    /**
     * Создание или загрузка мира
     */
    private void createOrLoadWorld() {
        String worldName = plugin.getConfig().getString("oneblock.world", "oneblock_world");
        oneBlockWorld = Bukkit.getWorld(worldName);
        
        if (oneBlockWorld == null) {
            // Создание нового мира
            WorldCreator creator = new WorldCreator(worldName);
            creator.environment(World.Environment.NORMAL);
            creator.generateStructures(false);
            creator.type(WorldType.NORMAL);
            creator.generator(new OneBlockGenerator());
            
            oneBlockWorld = creator.createWorld();
            
            if (oneBlockWorld != null) {
                // Настройка мира
                oneBlockWorld.setGameRule(org.bukkit.GameRule.DO_DAYLIGHT_CYCLE, false);
                oneBlockWorld.setGameRule(org.bukkit.GameRule.DO_WEATHER_CYCLE, false);
                oneBlockWorld.setTime(6000); // Утро
                
                // Создание острова
                createIsland();
                
                plugin.getLogger().info("Мир OneBlock создан: " + worldName);
            }
        } else {
            plugin.getLogger().info("Мир OneBlock загружен: " + worldName);
            // Поиск острова
            findIsland();
        }
    }

    /**
     * Создание острова
     */
    private void createIsland() {
        if (oneBlockWorld == null) return;
        
        int x = plugin.getConfig().getInt("oneblock.island.x", 0);
        int y = plugin.getConfig().getInt("oneblock.island.y", 64);
        int z = plugin.getConfig().getInt("oneblock.island.z", 0);
        
        islandLocation = new Location(oneBlockWorld, x, y, z);
        
        // Платформа 5x5
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                Block block = oneBlockWorld.getBlockAt(x + dx, y - 1, z + dz);
                block.setType(Material.BEDROCK);
            }
        }
        
        // Центральный блок
        Block centerBlock = oneBlockWorld.getBlockAt(x, y, z);
        centerBlock.setType(Material.GRASS_BLOCK);
        
        // Сохранение координат в конфиг
        plugin.getConfig().set("oneblock.island.x", x);
        plugin.getConfig().set("oneblock.island.y", y);
        plugin.getConfig().set("oneblock.island.z", z);
        plugin.saveConfig();
        
        plugin.getLogger().info("Остров создан на координатах: " + x + ", " + y + ", " + z);
    }

    /**
     * Поиск существующего острова
     */
    private void findIsland() {
        int x = plugin.getConfig().getInt("oneblock.island.x", 0);
        int y = plugin.getConfig().getInt("oneblock.island.y", 64);
        int z = plugin.getConfig().getInt("oneblock.island.z", 0);
        
        islandLocation = new Location(oneBlockWorld, x, y, z);
    }

    /**
     * Телепортация игрока на остров
     */
    public void teleportToIsland(Player player) {
        if (islandLocation == null) {
            player.sendMessage(ChatColor.RED + "Остров ещё не создан! Подождите...");
            return;
        }
        
        Location teleportLoc = islandLocation.clone().add(0, 1, 0);
        player.teleport(teleportLoc);
        playersOnIsland.add(player.getUniqueId());
        
        player.sendMessage(ChatColor.GREEN + "✓ Вы телепортированы на остров Один блок!");
        player.sendMessage(ChatColor.YELLOW + "Ломайте блоки и получайте ресурсы!");
        
        // Эффект телепортации
        player.spawnParticle(Particle.PORTAL, teleportLoc, 50, 0.5, 0.5, 0.5, 0.5);
        player.playSound(teleportLoc, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
    }

    /**
     * Телепортация игрока на спавн
     */
    public void teleportToSpawn(Player player) {
        playersOnIsland.remove(player.getUniqueId());
        
        // Телепортация на главный мир
        World mainWorld = Bukkit.getWorlds().get(0);
        Location spawnLoc = mainWorld.getSpawnLocation();
        
        player.teleport(spawnLoc);
        player.sendMessage(ChatColor.GREEN + "✓ Вы вернулись на спавн!");
        
        // Эффект
        player.spawnParticle(Particle.PORTAL, player.getLocation(), 50, 0.5, 0.5, 0.5, 0.5);
        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
    }

    /**
     * Обработка разрушения блока
     * Заменяет только ОДИН блок который сломал игрок
     */
    public boolean handleBlockBreak(Player player, Block block) {
        if (islandLocation == null) return false;

        // Проверка что блок в мире OneBlock
        if (!block.getWorld().equals(oneBlockWorld)) return false;

        // Проверка что блок на острове (в радиусе 2 блоков от центра)
        int dx = Math.abs(block.getX() - islandLocation.getBlockX());
        int dz = Math.abs(block.getZ() - islandLocation.getBlockZ());
        int dy = Math.abs(block.getY() - islandLocation.getBlockY());

        if (dx > 2 || dz > 2 || dy > 2) return false;

        // Выпадение блока (дроп)
        block.breakNaturally();

        // Получаем новый случайный блок
        Material newBlock = getRandomBlock();

        // Мгновенная замена блока (без задержки - оптимизация)
        block.setType(newBlock);
        
        // Частицы (только одному игроку - оптимизация)
        player.spawnParticle(Particle.HAPPY_VILLAGER,
            block.getLocation().add(0.5, 0.5, 0.5), 5, 0.3, 0.3, 0.3, 0.1);
        player.playSound(block.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 1.5f);

        return true;
    }

    /**
     * Случайный блок из пула
     */
    private Material getRandomBlock() {
        if (blockPool.isEmpty()) return Material.DIRT;
        return blockPool.get(new Random().nextInt(blockPool.size()));
    }

    /**
     * Проверка: игрок на острове
     */
    public boolean isPlayerOnIsland(Player player) {
        return playersOnIsland.contains(player.getUniqueId());
    }

    /**
     * Получить местоположение острова
     */
    public Location getIslandLocation() {
        return islandLocation;
    }

    /**
     * Получить мир OneBlock
     */
    public World getOneBlockWorld() {
        return oneBlockWorld;
    }

    /**
     * Выход всех игроков при отключении сервера
     */
    public void removeAllPlayers() {
        for (UUID uuid : new HashSet<>(playersOnIsland)) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                teleportToSpawn(player);
            }
        }
        playersOnIsland.clear();
    }
    
    /**
     * Добавить добытый блок
     */
    public void addMinedBlock(Player player, Material material) {
        UUID uuid = player.getUniqueId();
        minedBlocks.computeIfAbsent(uuid, k -> new HashMap<>())
            .merge(material, 1, Integer::sum);
    }
    
    /**
     * Продать все добытые блоки
     */
    public double sellAllBlocks(Player player) {
        UUID uuid = player.getUniqueId();
        Map<Material, Integer> blocks = minedBlocks.get(uuid);
        
        if (blocks == null || blocks.isEmpty()) {
            return 0.0;
        }
        
        double totalEarnings = 0.0;
        
        for (Map.Entry<Material, Integer> entry : blocks.entrySet()) {
            Material material = entry.getKey();
            int count = entry.getValue();
            
            // Получаем цену из конфига
            double price = plugin.getConfig().getDouble("jobs.mining.blocks." + material.name(), 1.0);
            double earnings = price * count;
            totalEarnings += earnings;
        }
        
        // Зачисляем деньги
        plugin.getEconomyManager().deposit(uuid, totalEarnings);
        
        // Очищаем добытые блоки
        blocks.clear();
        
        return totalEarnings;
    }
    
    /**
     * Получить статистику добычи
     */
    public Map<Material, Integer> getMinedBlocks(Player player) {
        return new HashMap<>(minedBlocks.getOrDefault(player.getUniqueId(), new HashMap<>()));
    }
    
    /**
     * Очистить данные игрока
     */
    public void clearPlayerData(UUID uuid) {
        minedBlocks.remove(uuid);
    }
}
