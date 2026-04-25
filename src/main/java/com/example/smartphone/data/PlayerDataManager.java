package com.example.smartphone.data;

import com.example.smartphone.SmartPhonePlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class PlayerDataManager {

    private final File dataFolder;
    private final Map<UUID, PlayerData> playerData;
    private final SmartPhonePlugin plugin;
    private final int saveIntervalTicks;

    public PlayerDataManager(SmartPhonePlugin plugin) {
        this.plugin = plugin;
        this.dataFolder = new File(plugin.getDataFolder(), "player-data");
        this.playerData = new HashMap<>();
        this.saveIntervalTicks = plugin.getConfig().getInt("player-data.saveInterval", 600); // 30 секунд

        if (!this.dataFolder.exists()) {
            this.dataFolder.mkdirs();
        }

        loadAll();
        startAutoSave();
    }

    /**
     * Автоматическое сохранение всех данных каждые N тиков
     */
    private void startAutoSave() {
        new BukkitRunnable() {
            @Override
            public void run() {
                saveAll();
            }
        }.runTaskTimer(plugin, saveIntervalTicks, saveIntervalTicks);
    }

    public PlayerData getPlayerData(UUID uuid) {
        return playerData.computeIfAbsent(uuid, this::loadPlayer);
    }

    private PlayerData loadPlayer(UUID uuid) {
        File file = new File(dataFolder, uuid.toString() + ".yml");
        if (!file.exists()) {
            return new PlayerData(uuid);
        }
        
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        // [ЗАГРУЗКА ДОМОВ]
        Map<String, Home> homes = new HashMap<>();
        if (config.isConfigurationSection("homes")) {
            for (String homeName : config.getConfigurationSection("homes").getKeys(false)) {
                String path = "homes." + homeName;
                Home home = new Home(
                    config.getString(path + ".world"),
                    config.getDouble(path + ".x"),
                    config.getDouble(path + ".y"),
                    config.getDouble(path + ".z")
                );
                homes.put(homeName, home);
            }
        }

        PlayerData data = new PlayerData(
            uuid,
            config.getDouble("balance", 100),
            homes,
            config.getLong("homeCooldown", 0)
        );
        
        // [ЗАГРУЗКА ПЕРКОВ]
        data.damageLevel = config.getInt("perks.damage", 0);
        data.staminaLevel = config.getInt("perks.stamina", 0);
        data.bhopLevel = config.getInt("perks.bhop", 0);
        data.doubleJumpLevel = config.getInt("perks.doubleJump", 0);
        data.perkPoints = config.getInt("perks.points", 0);
        data.canDoubleJump = config.getBoolean("perks.canDoubleJump", false);
        data.lastJumpTime = config.getLong("perks.lastJumpTime", 0);
        data.doubleJumpEnabled = config.getBoolean("perks.doubleJumpEnabled", true);
        data.damageEnabled = config.getBoolean("perks.damageEnabled", true);
        
        // [ЗАГРУЗКА НОВЫХ ПЕРКОВ]
        data.phoenixLevel = config.getInt("perks.phoenix", 0);
        data.shieldLevel = config.getInt("perks.shield", 0);
        data.regenLevel = config.getInt("perks.regen", 0);
        data.dashLevel = config.getInt("perks.dash", 0);
        data.vampireLevel = config.getInt("perks.vampire", 0);
        data.fireLevel = config.getInt("perks.fire", 0);
        data.phoenixCharges = config.getInt("perks.phoenixCharges", 0);
        data.lastDashTime = config.getLong("perks.lastDashTime", 0);
        data.phoenixLastChargeTime = config.getLong("perks.phoenixLastChargeTime", 0);
        
        // Флаги включения перков
        data.phoenixEnabled = config.getBoolean("perks.phoenixEnabled", true);
        data.shieldEnabled = config.getBoolean("perks.shieldEnabled", true);
        data.regenEnabled = config.getBoolean("perks.regenEnabled", true);
        data.dashEnabled = config.getBoolean("perks.dashEnabled", true);
        data.vampireEnabled = config.getBoolean("perks.vampireEnabled", true);
        data.fireEnabled = config.getBoolean("perks.fireEnabled", true);

        // [ЗАГРУЗКА ШАХТИНГА]
        data.miningLevel = config.getInt("mining.level", 0);
        data.miningXp = config.getInt("mining.xp", 0);
        data.miningPerkProgress = config.getInt("mining.perkProgress", 0);
        data.miningReward10Claimed = config.getBoolean("mining.reward10Claimed", false);
        data.miningReward25Claimed = config.getBoolean("mining.reward25Claimed", false);
        data.miningReward50Claimed = config.getBoolean("mining.reward50Claimed", false);
        data.miningReward100Claimed = config.getBoolean("mining.reward100Claimed", false);
        
        return data;
    }

    public void savePlayer(PlayerData data) {
        data.dirty = true; // Помечаем как изменённое
    }
    
    public void saveAll() {
        int saved = 0;
        for (PlayerData data : playerData.values()) {
            if (data.dirty) {
                savePlayerToFile(data);
                data.dirty = false;
                saved++;
            }
        }
        if (saved > 0) {
            plugin.getLogger().info("Сохранено " + saved + " игроков");
        }
    }
    
    private void savePlayerToFile(PlayerData data) {
        File file = new File(dataFolder, data.uuid.toString() + ".yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        config.set("balance", data.balance);
        config.set("homeCooldown", data.homeCooldown);

        // [СОХРАНЕНИЕ ДОМОВ]
        config.set("homes", null);
        for (Map.Entry<String, Home> entry : data.homes.entrySet()) {
            String path = "homes." + entry.getKey();
            Home home = entry.getValue();
            config.set(path + ".world", home.world);
            config.set(path + ".x", home.x);
            config.set(path + ".y", home.y);
            config.set(path + ".z", home.z);
        }
        
        // [СОХРАНЕНИЕ ПЕРКОВ]
        config.set("perks.damage", data.damageLevel);
        config.set("perks.stamina", data.staminaLevel);
        config.set("perks.bhop", data.bhopLevel);
        config.set("perks.doubleJump", data.doubleJumpLevel);
        config.set("perks.points", data.perkPoints);
        config.set("perks.canDoubleJump", data.canDoubleJump);
        config.set("perks.lastJumpTime", data.lastJumpTime);
        config.set("perks.doubleJumpEnabled", data.doubleJumpEnabled);
        config.set("perks.damageEnabled", data.damageEnabled);
        
        // [СОХРАНЕНИЕ НОВЫХ ПЕРКОВ]
        config.set("perks.phoenix", data.phoenixLevel);
        config.set("perks.shield", data.shieldLevel);
        config.set("perks.regen", data.regenLevel);
        config.set("perks.dash", data.dashLevel);
        config.set("perks.vampire", data.vampireLevel);
        config.set("perks.fire", data.fireLevel);
        config.set("perks.phoenixCharges", data.phoenixCharges);
        config.set("perks.lastDashTime", data.lastDashTime);
        config.set("perks.phoenixLastChargeTime", data.phoenixLastChargeTime);
        
        // Флаги включения перков
        config.set("perks.phoenixEnabled", data.phoenixEnabled);
        config.set("perks.shieldEnabled", data.shieldEnabled);
        config.set("perks.regenEnabled", data.regenEnabled);
        config.set("perks.dashEnabled", data.dashEnabled);
        config.set("perks.vampireEnabled", data.vampireEnabled);
        config.set("perks.fireEnabled", data.fireEnabled);

        // [СОХРАНЕНИЕ ШАХТИНГА]
        config.set("mining.level", data.miningLevel);
        config.set("mining.xp", data.miningXp);
        config.set("mining.perkProgress", data.miningPerkProgress);
        config.set("mining.reward10Claimed", data.miningReward10Claimed);
        config.set("mining.reward25Claimed", data.miningReward25Claimed);
        config.set("mining.reward50Claimed", data.miningReward50Claimed);
        config.set("mining.reward100Claimed", data.miningReward100Claimed);

        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadAll() {
        if (!dataFolder.exists()) return;
        
        File[] files = dataFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files != null) {
            for (File file : files) {
                try {
                    UUID uuid = UUID.fromString(file.getName().replace(".yml", ""));
                    playerData.put(uuid, loadPlayer(uuid));
                } catch (IllegalArgumentException e) {
                    // Игнорируем неверные файлы
                }
            }
        }
    }

    public static class PlayerData {
        public final UUID uuid;
        public double balance;
        public Map<String, Home> homes;
        public long homeCooldown;
        
        // Перки
        public int damageLevel;
        public int staminaLevel;
        public int bhopLevel;
        public int doubleJumpLevel;
        public int perkPoints;
        public boolean damageEnabled; // Включён ли перк урона
        
        // Новые перки
        public int phoenixLevel;      // Уровни воскрешения
        public int shieldLevel;       // Шанс блока (5%/10%/15%)
        public int regenLevel;        // Скорость регена
        public int dashLevel;         // Дальность рывка
        public int vampireLevel;      // Шанс вампиризма
        public int fireLevel;         // Шанс поджога
        public int phoenixCharges;    // Заряды феникса
        public long lastDashTime;     // Кулдаун рывка
        public long phoenixLastChargeTime; // Время последнего заряда
        
        // Флаги включения перков
        public boolean phoenixEnabled;
        public boolean shieldEnabled;
        public boolean regenEnabled;
        public boolean dashEnabled;
        public boolean vampireEnabled;
        public boolean fireEnabled;
        
        // Шахтинг
        public int miningLevel;
        public int miningXp;
        public int miningPerkProgress; // Прогресс для очков перков
        public boolean miningReward10Claimed;
        public boolean miningReward25Claimed;
        public boolean miningReward50Claimed;
        public boolean miningReward100Claimed;
        
        // Для двойного прыжка
        public boolean canDoubleJump;
        public long lastJumpTime;
        public boolean doubleJumpEnabled; // Включён ли перк
        
        // Флаг изменений для оптимизации
        public boolean dirty = false;

        public PlayerData(UUID uuid) {
            this(uuid, 100, new HashMap<>(), 0);
        }

        public PlayerData(UUID uuid, double balance, Map<String, Home> homes, long homeCooldown) {
            this.uuid = uuid;
            this.balance = balance;
            this.homes = homes;
            this.homeCooldown = homeCooldown;
            this.damageLevel = 0;
            this.staminaLevel = 0;
            this.bhopLevel = 0;
            this.doubleJumpLevel = 0;
            this.doubleJumpEnabled = true; // По умолчанию включён
            this.perkPoints = 0;
            this.damageEnabled = true; // По умолчанию включён
            
            // Новые перки
            this.phoenixLevel = 0;
            this.shieldLevel = 0;
            this.regenLevel = 0;
            this.dashLevel = 0;
            this.vampireLevel = 0;
            this.fireLevel = 0;
            this.phoenixCharges = 0;
            this.lastDashTime = 0;
            this.phoenixLastChargeTime = 0;
            
            // Флаги включения перков (по умолчанию включены)
            this.phoenixEnabled = true;
            this.shieldEnabled = true;
            this.regenEnabled = true;
            this.dashEnabled = true;
            this.vampireEnabled = true;
            this.fireEnabled = true;
            
            this.miningLevel = 0;
            this.miningXp = 0;
            this.miningPerkProgress = 0;
            this.miningReward10Claimed = false;
            this.miningReward25Claimed = false;
            this.miningReward50Claimed = false;
            this.miningReward100Claimed = false;
            this.canDoubleJump = false;
            this.lastJumpTime = 0;
        }

        public boolean hasHome() {
            return !homes.isEmpty();
        }

        public Home getHome(String name) {
            return homes.get(name);
        }

        public void addHome(String name, Home home) {
            homes.put(name, home);
        }

        public void removeHome(String name) {
            homes.remove(name);
        }

        public Set<String> getHomeNames() {
            return homes.keySet();
        }

        public boolean isOnCooldown() {
            return System.currentTimeMillis() < homeCooldown;
        }

        public long getRemainingCooldown() {
            return (homeCooldown - System.currentTimeMillis()) / 1000;
        }
    }

    public static class Home {
        public String world;
        public double x, y, z;

        public Home(String world, double x, double y, double z) {
            this.world = world;
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }
}
