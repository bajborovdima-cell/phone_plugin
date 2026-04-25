package com.example.smartphone.listeners;

import com.example.smartphone.SmartPhonePlugin;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class JobListener implements Listener {

    private final SmartPhonePlugin plugin;
    private final Map<UUID, Long> lastMiningReward;
    
    // [АНТИ-АБЬЮЗ] Отслеживание поставленных блоков
    private final Map<UUID, Set<BlockLocation>> placedBlocks;

    // [БАЗОВАЯ СТАВКА] 1 рубль за блок
    private static final double BASE_REWARD = 1.0;

    // [БЛОКИ БЕЗ НАГРАДЫ] За которые деньги не начисляются
    private static final Set<Material> NO_REWARD_BLOCKS = Set.of(
        Material.BEDROCK,
        Material.BARRIER,
        Material.COMMAND_BLOCK,
        Material.STRUCTURE_BLOCK,
        Material.JIGSAW,
        Material.END_PORTAL,
        Material.END_PORTAL_FRAME,
        Material.NETHER_PORTAL,
        Material.CAVE_AIR,
        Material.VOID_AIR,
        Material.WATER,
        Material.LAVA,
        Material.AIR
    );

    public JobListener(SmartPhonePlugin plugin) {
        this.plugin = plugin;
        this.lastMiningReward = new HashMap<>();
        this.placedBlocks = new HashMap<>();
    }
    
    /**
     * Отслеживание установки блоков (анти-абьюз)
     */
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        
        UUID uuid = player.getUniqueId();
        placedBlocks.computeIfAbsent(uuid, k -> new HashSet<>())
            .add(new BlockLocation(block.getWorld().getName(), block.getX(), block.getY(), block.getZ()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled()) {
            return;
        }

        Player player = event.getPlayer();
        Block block = event.getBlock();
        Material blockType = block.getType();
        
        // [АНТИ-АБЬЮЗ] Проверка что блок не был поставлен игроком
        UUID uuid = player.getUniqueId();
        BlockLocation blockLoc = new BlockLocation(block.getWorld().getName(), block.getX(), block.getY(), block.getZ());
        
        if (placedBlocks.containsKey(uuid) && placedBlocks.get(uuid).contains(blockLoc)) {
            // Блок был поставлен игроком - не даём награду
            placedBlocks.get(uuid).remove(blockLoc);
            return;
        }

        // [ПРОВЕРКА ВКЛЮЧЕНИЯ]
        if (!plugin.getConfig().getBoolean("jobs.mining.enabled", true)) {
            return;
        }

        // [ПРОВЕРКА НА ЗАПРЕЩЁННЫЕ БЛОКИ]
        if (NO_REWARD_BLOCKS.contains(blockType)) {
            return;
        }

        // [ПОЛУЧЕНИЕ ЦЕНЫ]
        double reward = getBlockReward(blockType);

        // [БАЗОВАЯ НАГРАДА] Если блок не указан в конфиге - даём базовую ставку
        if (reward <= 0) {
            reward = BASE_REWARD;
        }

        // [ПРОВЕРКА КУЛДАУНА]
        int cooldownSeconds = plugin.getConfig().getInt("jobs.mining.cooldown", 0);
        if (cooldownSeconds > 0) {
            UUID playerUUID = player.getUniqueId();
            long currentTime = System.currentTimeMillis();
            Long lastReward = lastMiningReward.get(playerUUID);

            if (lastReward != null) {
                long elapsed = (currentTime - lastReward) / 1000;
                if (elapsed < cooldownSeconds) {
                    return;
                }
            }

            lastMiningReward.put(playerUUID, currentTime);
        }

        // [НАЧИСЛЕНИЕ]
        plugin.getEconomyManager().deposit(player.getUniqueId(), reward);

        // [СООБЩЕНИЕ]
        if (plugin.getConfig().getBoolean("jobs.mining.messages", true)) {
            String blockName = getBlockName(blockType);
            // Форматирование награды: целое число если нет дроби, иначе с 1 знаком после запятой
            String rewardText = reward % 1 == 0 ? String.valueOf((int)reward) : String.format("%.1f", reward);
            String currency = reward == 1.0 ? "рубль" : "рублей";
            player.sendMessage(ChatColor.GREEN + "+§e" + rewardText + " §a" + currency + " за " + blockName);
        }
    }

    private double getBlockReward(Material material) {
        String path = "jobs.mining.blocks." + material.name();
        return plugin.getConfig().getDouble(path, 0);
    }

    private String getBlockName(Material material) {
        String name = material.name().replace("_", " ").toLowerCase();

        // [КРАСИВЫЕ ИМЕНА]
        switch (material) {
            // [РУДЫ]
            case COAL_ORE: return "Угольную руду";
            case DEEPSLATE_COAL_ORE: return "Глубинную угольную руду";
            case IRON_ORE: return "Железную руду";
            case DEEPSLATE_IRON_ORE: return "Глубинную железную руду";
            case GOLD_ORE: return "Золотую руду";
            case DEEPSLATE_GOLD_ORE: return "Глубинную золотую руду";
            case DIAMOND_ORE: return "Алмазную руду";
            case DEEPSLATE_DIAMOND_ORE: return "Глубинную алмазную руду";
            case EMERALD_ORE: return "Изумрудную руду";
            case DEEPSLATE_EMERALD_ORE: return "Глубинную изумрудную руду";
            case LAPIS_ORE: return "Лазуритовую руду";
            case REDSTONE_ORE: return "Редстоун руду";
            case COPPER_ORE: return "Медную руду";
            case NETHER_GOLD_ORE: return "Золотую руду Незера";
            case NETHER_QUARTZ_ORE: return "Кварцевую руду";
            case ANCIENT_DEBRIS: return "Древние обломки";
            
            // [КАМНИ]
            case STONE: return "Камень";
            case COBBLESTONE: return "Булыжник";
            case COBBLED_DEEPSLATE: return "Глубинный булыжник";
            case ANDESITE: return "Андезит";
            case GRANITE: return "Гранит";
            case DIORITE: return "Диорит";
            case CALCITE: return "Кальцит";
            case TUFF: return "Туф";
            case DRIPSTONE_BLOCK: return "Капельниковый блок";
            case POINTED_DRIPSTONE: return "Остриё капельника";
            
            // [ЗЕМЛЯ]
            case DIRT: return "Землю";
            case GRASS_BLOCK: return "Дёрн";
            case PODZOL: return "Подзол";
            case MYCELIUM: return "Мицелий";
            case ROOTED_DIRT: return "Укоренённую землю";
            case MOSS_BLOCK: return "Блок мха";
            case CLAY: return "Глину";
            
            // [ПЕСОК]
            case SAND: return "Песок";
            case RED_SAND: return "Красный песок";
            case GRAVEL: return "Гравий";
            case SANDSTONE: return "Песчаник";
            
            // [ДЕРЕВО]
            case OAK_WOOD: return "Дубовую древесину";
            case OAK_LOG: return "Дубовое бревно";
            case STRIPPED_OAK_LOG: return "Очищенное дубовое бревно";
            case BIRCH_WOOD: return "Берёзовую древесину";
            case BIRCH_LOG: return "Берёзовое бревно";
            case SPRUCE_WOOD: return "Еловую древесину";
            case SPRUCE_LOG: return "Еловое бревно";
            case JUNGLE_WOOD: return "Джунглевую древесину";
            case JUNGLE_LOG: return "Джунглевое бревно";
            case ACACIA_WOOD: return "Акациевую древесину";
            case ACACIA_LOG: return "Акациевое бревно";
            case DARK_OAK_WOOD: return "Тёмную дубовую древесину";
            case DARK_OAK_LOG: return "Тёмное дубовое бревно";
            case MANGROVE_LOG: return "Мангровое бревно";
            case CHERRY_LOG: return "Вишнёвое бревно";
            
            // [ПРОЧЕЕ]
            case OBSIDIAN: return "Обсидиан";
            case CRYING_OBSIDIAN: return "Плачущий обсидиан";
            case NETHERRACK: return "Незеррак";
            case END_STONE: return "Камень Края";
            case SOUL_SAND: return "Песок душ";
            case SOUL_SOIL: return "Почву душ";
            case BASALT: return "Базальт";
            case BLACKSTONE: return "Чернит";
            case GLOWSTONE: return "Светокамень";
            case SEA_LANTERN: return "Морской фонарь";
            case PRISMARINE: return "Призмарин";
            case ICE: return "Лёд";
            case PACKED_ICE: return "Плотный лёд";
            case BLUE_ICE: return "Синий лёд";
            case SNOW_BLOCK: return "Блок снега";
            case SNOW: return "Снег";
            case MOSSY_COBBLESTONE: return "Замшелый булыжник";
            case INFESTED_STONE: return "Заражённый камень";
            case ENDER_CHEST: return "Эндер-сундук";
            
            // [СТРОИТЕЛЬНЫЕ]
            case BRICK: return "Кирпич";
            case BRICKS: return "Кирпичи";
            case NETHER_BRICK: return "Незер-кирпич";
            case QUARTZ_BLOCK: return "Кварцевый блок";
            case SMOOTH_QUARTZ: return "Гладкий кварц";
            case WHITE_CONCRETE: return "Белый бетон";
            case BLACK_CONCRETE: return "Чёрный бетон";
            
            default:
                // [ДРУГИЕ БЛОКИ] Возвращаем обычное название
                return name;
        }
    }
    
    /**
     * Класс для хранения координат блока
     */
    private static class BlockLocation {
        private final String world;
        private final int x, y, z;
        
        public BlockLocation(String world, int x, int y, int z) {
            this.world = world;
            this.x = x;
            this.y = y;
            this.z = z;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof BlockLocation)) return false;
            BlockLocation other = (BlockLocation) obj;
            return world.equals(other.world) && x == other.x && y == other.y && z == other.z;
        }
        
        @Override
        public int hashCode() {
            int result = world.hashCode();
            result = 31 * result + x;
            result = 31 * result + y;
            result = 31 * result + z;
            return result;
        }
    }
}
