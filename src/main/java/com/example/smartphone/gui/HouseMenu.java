package com.example.smartphone.gui;

import com.example.smartphone.SmartPhonePlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Меню недвижимости - покупка готовых схем домов
 */
public class HouseMenu implements InventoryHolder {

    private final SmartPhonePlugin plugin;
    private static final String MENU_TITLE = ChatColor.DARK_GREEN + "Недвижимость";

    // Схемы домов
    private static final Map<Integer, HouseSchema> SCHEMAS = new HashMap<>();

    static {
        SCHEMAS.put(9, new HouseSchema(
            "small",
            "Маленький дом",
            500,
            7, 5, 7,
            Material.OAK_PLANKS
        ));
        SCHEMAS.put(10, new HouseSchema(
            "medium",
            "Средний дом",
            1500,
            9, 6, 9,
            Material.SPRUCE_PLANKS
        ));
        SCHEMAS.put(11, new HouseSchema(
            "large",
            "Большой особняк",
            3000,
            13, 7, 13,
            Material.DARK_OAK_PLANKS
        ));
    }

    public HouseMenu(SmartPhonePlugin plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        Inventory inventory = Bukkit.createInventory(this, 27, MENU_TITLE);
        fillMenu(inventory, player);
        player.openInventory(inventory);
    }

    private void fillMenu(Inventory inventory, Player player) {
        // [РЯД 1: Заголовок]
        inventory.setItem(0, createMenuItem(
            Material.OAK_DOOR,
            ChatColor.GREEN + "Автопостройка домов",
            ChatColor.GRAY + "Выберите дом для покупки",
            ChatColor.GRAY + "Дом будет построен вокруг вас"
        ));

        // [РЯД 2: Схемы домов]
        for (Map.Entry<Integer, HouseSchema> entry : SCHEMAS.entrySet()) {
            HouseSchema schema = entry.getValue();
            inventory.setItem(entry.getKey(), createSchemaItem(schema));
        }

        // [РЯД 3: Информация]
        int playerHomes = plugin.getPlayerDataManager()
            .getPlayerData(player.getUniqueId())
            .getHomeNames().size();
        inventory.setItem(19, createInfoItem(
            Material.NAME_TAG,
            ChatColor.WHITE + "Ваши дома",
            ChatColor.GOLD + String.valueOf(playerHomes)
        ));

        double balance = plugin.getEconomyManager().getBalance(player.getUniqueId());
        inventory.setItem(20, createInfoItem(
            Material.GOLD_INGOT,
            ChatColor.WHITE + "Ваш баланс",
            ChatColor.GOLD + String.valueOf((int)balance) + " монет"
        ));

        // [ЗАПОЛНИТЕЛИ]
        for (int i : new int[]{1, 2, 3, 4, 5, 6, 7, 8, 12, 13, 14, 15, 16, 17, 18, 21, 22, 23, 24, 25, 26}) {
            inventory.setItem(i, createGlassPane());
        }
    }

    private ItemStack createMenuItem(Material material, String name, String lore1, String lore2) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(Arrays.asList(lore1, lore2));
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createSchemaItem(HouseSchema schema) {
        ItemStack item = new ItemStack(schema.material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.GREEN + schema.name);
            List<String> lore = Arrays.asList(
                ChatColor.GRAY + "Размер: " + schema.width + "x" + schema.height + "x" + schema.depth,
                ChatColor.GRAY + "С окнами, дверью, мебелью",
                "",
                ChatColor.GOLD + "Цена: " + schema.price + " монет",
                "",
                ChatColor.GREEN + "→ Клик для покупки"
            );
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createInfoItem(Material material, String name, String value) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(Arrays.asList(ChatColor.GRAY + value));
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createGlassPane() {
        ItemStack item = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(" ");
            item.setItemMeta(meta);
        }
        return item;
    }

    /**
     * Обработка клика по меню
     */
    public boolean handleItemClick(Player player, int slot) {
        HouseSchema schema = SCHEMAS.get(slot);
        if (schema == null) {
            return false;
        }

        // [ПРОВЕРКА БАЛАНСА]
        if (!plugin.getEconomyManager().hasBalance(player.getUniqueId(), schema.price)) {
            player.sendMessage(ChatColor.RED + "Недостаточно денег! Нужно: " + schema.price + " монет");
            return true;
        }

        // [ПОСТРОЙКА ДОМА]
        Location loc = player.getLocation();
        int x = loc.getBlockX() - schema.width / 2;
        int y = loc.getBlockY();
        int z = loc.getBlockZ() - schema.depth / 2;

        buildHouse(player.getWorld(), x, y, z, schema);

        // [ОПЛАТА]
        plugin.getEconomyManager().withdraw(player.getUniqueId(), schema.price);
        player.sendMessage(ChatColor.GREEN + "✓ Дом построен! С вас: " + schema.price + " монет");

        // [СОХРАНЕНИЕ ДОМА]
        var data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
        String homeName = schema.name + "_" + System.currentTimeMillis();
        var home = new com.example.smartphone.data.PlayerDataManager.Home(
            player.getWorld().getName(),
            x + schema.width / 2,
            loc.getBlockY() + 1,  // Сохраняем координату пола
            z + schema.depth / 2
        );
        data.addHome(homeName, home);
        plugin.getPlayerDataManager().savePlayer(data);
        player.sendMessage(ChatColor.YELLOW + "Дом сохранён как: " + homeName);

        return true;
    }

    /**
     * Постройка дома по схеме
     */
    private void buildHouse(org.bukkit.World world, int startX, int startY, int startZ, HouseSchema schema) {
        int width = schema.width;
        int height = schema.height;
        int depth = schema.depth;

        // Очистка области (начинаем с пола)
        clearArea(world, startX, startY, startZ, width, height + 2, depth);

        // === ФУНДАМЕНТ (полный квадрат) - на уровне земли ===
        for (int x = 0; x < width; x++) {
            for (int z = 0; z < depth; z++) {
                setBlock(world, startX + x, startY, startZ + z, Material.COBBLESTONE);
            }
        }

        // === ПОЛ (на уровне земли) ===
        for (int x = 0; x < width; x++) {
            for (int z = 0; z < depth; z++) {
                setBlock(world, startX + x, startY, startZ + z, Material.OAK_PLANKS);
            }
        }

        // === СТЕНЫ (начинаем с y+1) ===
        buildWalls(world, startX, startY, startZ, width, height, depth, schema);

        // === КРЫША ===
        buildRoof(world, startX, startY, startZ, width, depth);

        // === ДВЕРЬ ===
        placeDoor(world, startX, startY, startZ, width, depth);

        // === ОКНА ===
        placeWindows(world, startX, startY, startZ, width, height, depth);

        // === МЕБЕЛЬ ===
        placeFurniture(world, startX, startY, startZ, width, depth);
    }

    /**
     * Очистка области
     */
    private void clearArea(org.bukkit.World world, int x, int y, int z, int width, int height, int depth) {
        for (int dx = 0; dx < width; dx++) {
            for (int dy = 0; dy < height; dy++) {
                for (int dz = 0; dz < depth; dz++) {
                    world.getBlockAt(x + dx, y + dy, z + dz).setType(Material.AIR);
                }
            }
        }
    }

    /**
     * Постройка стен
     */
    private void buildWalls(org.bukkit.World world, int x, int y, int z, int width, int height, int depth, HouseSchema schema) {
        Material wallMaterial = schema.material;
        Material logMaterial = getLogMaterial(schema.material);

        for (int h = 1; h < height; h++) {
            // Передняя и задняя стены
            for (int w = 0; w < width; w++) {
                // Передняя стена
                setBlock(world, x + w, y + h, z, wallMaterial);
                // Задняя стена
                setBlock(world, x + w, y + h, z + depth - 1, wallMaterial);
            }
            // Левая и правая стены
            for (int d = 0; d < depth; d++) {
                // Левая стена
                setBlock(world, x, y + h, z + d, wallMaterial);
                // Правая стена
                setBlock(world, x + width - 1, y + h, z + d, wallMaterial);
            }

            // Углы - брёвна
            setBlock(world, x, y + h, z, logMaterial);
            setBlock(world, x + width - 1, y + h, z, logMaterial);
            setBlock(world, x, y + h, z + depth - 1, logMaterial);
            setBlock(world, x + width - 1, y + h, z + depth - 1, logMaterial);
        }
    }

    /**
     * Постройка крыши
     */
    private void buildRoof(org.bukkit.World world, int x, int y, int z, int width, int depth) {
        int roofY = y + 5; // Крыша на высоте 5 блоков от пола
        
        // Плоская крыша
        for (int wx = 0; wx < width; wx++) {
            for (int wz = 0; wz < depth; wz++) {
                setBlock(world, x + wx, roofY, z + wz, Material.OAK_PLANKS);
            }
        }
        
        // Края крыши - ступени
        for (int wx = 0; wx < width; wx++) {
            setBlock(world, x + wx, roofY + 1, z, Material.OAK_STAIRS);
            setBlock(world, x + wx, roofY + 1, z + depth - 1, Material.OAK_STAIRS);
        }
        for (int wz = 0; wz < depth; wz++) {
            setBlock(world, x, roofY + 1, z + wz, Material.OAK_STAIRS);
            setBlock(world, x + width - 1, roofY + 1, z + wz, Material.OAK_STAIRS);
        }
    }

    /**
     * Размещение двери
     */
    private void placeDoor(org.bukkit.World world, int x, int y, int z, int width, int depth) {
        int doorX = x + width / 2;
        int doorZ = z;
        
        // Очищаем проём (дверь начинается с y+1)
        setBlock(world, doorX, y + 1, doorZ, Material.AIR);
        setBlock(world, doorX, y + 2, doorZ, Material.AIR);
        
        // Ставим дверь через setBlockData
        Block doorBottom = world.getBlockAt(doorX, y + 1, doorZ);
        Block doorTop = world.getBlockAt(doorX, y + 2, doorZ);
        
        doorBottom.setType(Material.OAK_DOOR);
        doorTop.setType(Material.OAK_DOOR);
        
        // Настраиваем дверь через createBlockData
        org.bukkit.block.data.type.Door doorData = (org.bukkit.block.data.type.Door) 
            Material.OAK_DOOR.createBlockData();
        doorData.setHalf(org.bukkit.block.data.type.Door.Half.BOTTOM);
        doorData.setOpen(false);
        doorBottom.setBlockData(doorData);
        
        org.bukkit.block.data.type.Door doorDataTop = (org.bukkit.block.data.type.Door) 
            Material.OAK_DOOR.createBlockData();
        doorDataTop.setHalf(org.bukkit.block.data.type.Door.Half.TOP);
        doorDataTop.setOpen(false);
        doorTop.setBlockData(doorDataTop);
    }

    /**
     * Размещение окон
     */
    private void placeWindows(org.bukkit.World world, int x, int y, int z, int width, int height, int depth) {
        int windowY = y + 3; // Окна на высоте 3 блока от пола
        
        // Окна на передней и задней стене
        for (int wx = 2; wx < width - 2; wx += 3) {
            // Передняя стена
            setBlock(world, x + wx, windowY, z, Material.GLASS);
            // Задняя стена
            setBlock(world, x + wx, windowY, z + depth - 1, Material.GLASS);
        }
        
        // Окна на боковых стенах
        for (int wz = 2; wz < depth - 2; wz += 3) {
            // Левая стена
            setBlock(world, x, windowY, z + wz, Material.GLASS);
            // Правая стена
            setBlock(world, x + width - 1, windowY, z + wz, Material.GLASS);
        }
    }

    /**
     * Размещение мебели
     */
    private void placeFurniture(org.bukkit.World world, int x, int y, int z, int width, int depth) {
        int floorY = y + 1; // Мебель ставится на пол (y+1)

        // === ВЕРСТАК (угол) ===
        setBlock(world, x + 1, floorY, z + 1, Material.CRAFTING_TABLE);

        // === ПЕЧЬ ===
        setBlock(world, x + 2, floorY, z + 1, Material.FURNACE);

        // === СУНДУК (рядом с верстаком) ===
        setBlock(world, x + 1, floorY, z + 2, Material.CHEST);

        // === СТОЛ (забор + плита) ===
        int tableX = x + width / 2 + 2;
        int tableZ = z + depth / 2 - 2;
        setBlock(world, tableX, floorY, tableZ, Material.OAK_FENCE);
        setBlock(world, tableX, floorY + 1, tableZ, Material.OAK_PRESSURE_PLATE);

        // === СТУЛ (повёрнут к столу) ===
        Block stairBlock = world.getBlockAt(tableX, floorY, tableZ + 1);
        stairBlock.setType(Material.OAK_STAIRS);
        // Поворот лестницы на 180 градусов (facing north)
        org.bukkit.block.data.type.Stairs stairData = (org.bukkit.block.data.type.Stairs) 
            Material.OAK_STAIRS.createBlockData();
        stairData.setFacing(org.bukkit.block.BlockFace.NORTH);
        stairBlock.setBlockData(stairData);

        // === ФАКЕЛЫ (освещение) ===
        int centerX = x + width / 2;
        setBlock(world, centerX - 2, floorY + 2, z + 1, Material.TORCH);
        setBlock(world, centerX + 2, floorY + 2, z + 1, Material.TORCH);
        setBlock(world, centerX - 2, floorY + 2, z + depth - 2, Material.TORCH);
        setBlock(world, centerX + 2, floorY + 2, z + depth - 2, Material.TORCH);
    }

    /**
     * Установка блока
     */
    private void setBlock(org.bukkit.World world, int x, int y, int z, Material material) {
        Block block = world.getBlockAt(x, y, z);
        block.setType(material);
    }

    /**
     * Получить бревно по материалу
     */
    private Material getLogMaterial(Material material) {
        switch (material) {
            case SPRUCE_PLANKS:
            case SPRUCE_WOOD:
            case SPRUCE_LOG:
                return Material.SPRUCE_LOG;
            case DARK_OAK_PLANKS:
            case DARK_OAK_WOOD:
            case DARK_OAK_LOG:
                return Material.DARK_OAK_LOG;
            default:
                return Material.OAK_LOG;
        }
    }

    @Override
    public Inventory getInventory() {
        return null;
    }

    /**
     * Схема дома
     */
    private static class HouseSchema {
        public final String id;
        public final String name;
        public final int price;
        public final int width, height, depth;
        public final Material material;

        public HouseSchema(String id, String name, int price, int width, int height, int depth, Material material) {
            this.id = id;
            this.name = name;
            this.price = price;
            this.width = width;
            this.height = height;
            this.depth = depth;
            this.material = material;
        }
    }
}
