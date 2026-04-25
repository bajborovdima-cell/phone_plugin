package com.example.smartphone.commands;

import com.example.smartphone.SmartPhonePlugin;
import com.example.smartphone.gui.PurchaseConfirmMenu;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
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
 * Команда магазина с категориями
 */
public class ShopCommand implements CommandExecutor {

    private final SmartPhonePlugin plugin;

    public ShopCommand(SmartPhonePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Только игроки могут использовать эту команду!");
            return true;
        }

        Player player = (Player) sender;
        ShopMenu menu = new ShopMenu(plugin);
        menu.open(player);

        return true;
    }

    /**
     * Меню магазина
     */
    public static class ShopMenu implements InventoryHolder {
        private final SmartPhonePlugin plugin;
        private static final String MENU_TITLE = ChatColor.GOLD + "Магазин";

        // Категории: слот → материал
        private static final Map<Integer, ShopCategory> CATEGORIES = new HashMap<>();

        static {
            CATEGORIES.put(10, new ShopCategory(Material.APPLE, "Еда"));
            CATEGORIES.put(11, new ShopCategory(Material.WOODEN_PICKAXE, "Инструменты"));
            CATEGORIES.put(12, new ShopCategory(Material.IRON_CHESTPLATE, "Броня"));
            CATEGORIES.put(13, new ShopCategory(Material.IRON_SWORD, "Оружие"));
            CATEGORIES.put(14, new ShopCategory(Material.OAK_PLANKS, "Блоки"));
        }

        public ShopMenu(SmartPhonePlugin plugin) {
            this.plugin = plugin;
        }

        public void open(Player player) {
            Inventory inventory = Bukkit.createInventory(this, 27, MENU_TITLE);
            fillMenu(inventory, player);
            player.openInventory(inventory);
        }

        private void fillMenu(Inventory inventory, Player player) {
            // Заголовок
            inventory.setItem(4, createMenuItem(
                Material.EMERALD,
                ChatColor.GREEN + "Магазин предметов",
                ChatColor.GRAY + "Выберите категорию",
                ChatColor.GRAY + "Ваш баланс: " + plugin.getEconomyManager().getBalance(player.getUniqueId())
            ));

            // Категории
            for (Map.Entry<Integer, ShopCategory> entry : CATEGORIES.entrySet()) {
                ShopCategory category = entry.getValue();
                inventory.setItem(entry.getKey(), createCategoryItem(category));
            }

            // Кнопка закрытия
            inventory.setItem(16, createMenuItem(
                Material.BARRIER,
                ChatColor.RED + "Закрыть",
                ChatColor.GRAY + "Нажми чтобы выйти",
                ""
            ));

            // Заполнители
            for (int i : new int[]{0, 1, 2, 3, 5, 6, 7, 8, 9, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26}) {
                inventory.setItem(i, createGlassPane());
            }
        }

        private ItemStack createCategoryItem(ShopCategory category) {
            ItemStack item = new ItemStack(category.material);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.AQUA + category.name);
                meta.setLore(Arrays.asList(
                    ChatColor.GRAY + "Нажми для просмотра",
                    ChatColor.GREEN + "→ Клик"
                ));
                item.setItemMeta(meta);
            }
            return item;
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

        private ItemStack createGlassPane() {
            ItemStack item = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(" ");
                item.setItemMeta(meta);
            }
            return item;
        }

        @Override
        public Inventory getInventory() {
            return null;
        }

        /**
         * Обработка клика
         */
        public boolean handleItemClick(Player player, int slot) {
            ShopCategory category = CATEGORIES.get(slot);
            if (category != null) {
                // Открываем подменю категории
                CategoryItemsMenu itemsMenu = new CategoryItemsMenu(plugin, category.name);
                itemsMenu.open(player);
                return true;
            }

            if (slot == 16) {
                player.closeInventory();
                return true;
            }

            return false;
        }

        private static class ShopCategory {
            public final Material material;
            public final String name;

            public ShopCategory(Material material, String name) {
                this.material = material;
                this.name = name;
            }
        }
    }

    /**
     * Меню предметов категории
     */
    public static class CategoryItemsMenu implements InventoryHolder {
        private final SmartPhonePlugin plugin;
        private final String categoryName;

        public CategoryItemsMenu(SmartPhonePlugin plugin, String categoryName) {
            this.plugin = plugin;
            this.categoryName = categoryName;
        }

        public void open(Player player) {
            Inventory inventory = Bukkit.createInventory(this, 54, ChatColor.GOLD + categoryName);
            fillMenu(inventory, player);
            player.openInventory(inventory);
        }

        private void fillMenu(Inventory inventory, Player player) {
            // Получаем предметы для категории
            List<ShopItem> items = getItemsForCategory(categoryName);

            // Заголовок
            inventory.setItem(49, createMenuItem(
                Material.EMERALD,
                ChatColor.GREEN + "Ваш баланс: " + (int)plugin.getEconomyManager().getBalance(player.getUniqueId()),
                ChatColor.GRAY + "ЛКМ - купить, ПКМ - продать"
            ));

            // Кнопка назад
            inventory.setItem(48, createMenuItem(
                Material.ARROW,
                ChatColor.YELLOW + "← Назад",
                ChatColor.GRAY + "Вернуться к категориям"
            ));

            // Предметы
            int slot = 0;
            for (ShopItem item : items) {
                if (slot >= 45) break; // Максимум 45 предметов

                inventory.setItem(slot, createShopItem(item));
                slot++;
            }

            // Заполнители
            for (int i = 0; i < 54; i++) {
                if (inventory.getItem(i) == null) {
                    inventory.setItem(i, createGlassPane());
                }
            }
        }

        private List<ShopItem> getItemsForCategory(String category) {
            List<ShopItem> items = new java.util.ArrayList<>();

            switch (category) {
                case "Еда":
                    items.add(new ShopItem(Material.APPLE, 10, 3, "Яблоко", true));
                    items.add(new ShopItem(Material.BREAD, 15, 5, "Хлеб", true));
                    items.add(new ShopItem(Material.COOKED_BEEF, 20, 8, "Жареная говядина", true));
                    items.add(new ShopItem(Material.COOKED_PORKCHOP, 20, 8, "Жареная свинина", true));
                    items.add(new ShopItem(Material.GOLDEN_APPLE, 100, 30, "Золотое яблоко", true));
                    break;

                case "Инструменты":
                    // Кирки
                    items.add(new ShopItem(Material.WOODEN_PICKAXE, 15, 5, "Деревянная кирка", false));
                    items.add(new ShopItem(Material.STONE_PICKAXE, 30, 10, "Каменная кирка", false));
                    items.add(new ShopItem(Material.IRON_PICKAXE, 100, 30, "Железная кирка", false));
                    // Топоры
                    items.add(new ShopItem(Material.WOODEN_AXE, 15, 5, "Деревянный топор", false));
                    items.add(new ShopItem(Material.STONE_AXE, 30, 10, "Каменный топор", false));
                    items.add(new ShopItem(Material.IRON_AXE, 100, 30, "Железный топор", false));
                    // Лопаты
                    items.add(new ShopItem(Material.WOODEN_SHOVEL, 15, 5, "Деревянная лопата", false));
                    items.add(new ShopItem(Material.STONE_SHOVEL, 30, 10, "Каменная лопата", false));
                    items.add(new ShopItem(Material.IRON_SHOVEL, 100, 30, "Железная лопата", false));
                    // Мотыги
                    items.add(new ShopItem(Material.WOODEN_HOE, 10, 3, "Деревянная мотыга", false));
                    items.add(new ShopItem(Material.STONE_HOE, 20, 6, "Каменная мотыга", false));
                    items.add(new ShopItem(Material.IRON_HOE, 80, 25, "Железная мотыга", false));
                    // Прочее
                    items.add(new ShopItem(Material.CRAFTING_TABLE, 20, 5, "Верстак", false));
                    items.add(new ShopItem(Material.FURNACE, 30, 10, "Печь", false));
                    items.add(new ShopItem(Material.CHEST, 15, 5, "Сундук", false));
                    items.add(new ShopItem(Material.TORCH, 5, 1, "Факел", false));
                    items.add(new ShopItem(Material.LADDER, 10, 3, "Лестница", false));
                    items.add(new ShopItem(Material.BUCKET, 50, 15, "Ведро", false));
                    items.add(new ShopItem(Material.SHEARS, 40, 12, "Ножницы", false));
                    items.add(new ShopItem(Material.FISHING_ROD, 60, 20, "Удочка", false));
                    items.add(new ShopItem(Material.FLINT_AND_STEEL, 40, 12, "Огниво", false));
                    items.add(new ShopItem(Material.COMPASS, 100, 30, "Компас", false));
                    items.add(new ShopItem(Material.CLOCK, 100, 30, "Часы", false));
                    break;

                case "Броня":
                    // Шлемы
                    items.add(new ShopItem(Material.LEATHER_HELMET, 15, 5, "Кожаный шлем", false));
                    items.add(new ShopItem(Material.CHAINMAIL_HELMET, 25, 8, "Кольчужный шлем", false));
                    items.add(new ShopItem(Material.IRON_HELMET, 50, 15, "Железный шлем", false));
                    items.add(new ShopItem(Material.COPPER_HELMET, 35, 12, "Медный шлем", false));
                    // Нагрудники
                    items.add(new ShopItem(Material.LEATHER_CHESTPLATE, 30, 10, "Кожаный нагрудник", false));
                    items.add(new ShopItem(Material.CHAINMAIL_CHESTPLATE, 50, 15, "Кольчужный нагрудник", false));
                    items.add(new ShopItem(Material.IRON_CHESTPLATE, 100, 30, "Железный нагрудник", false));
                    items.add(new ShopItem(Material.COPPER_CHESTPLATE, 55, 18, "Медный нагрудник", false));
                    // Поножи
                    items.add(new ShopItem(Material.LEATHER_LEGGINGS, 25, 8, "Кожаные поножи", false));
                    items.add(new ShopItem(Material.CHAINMAIL_LEGGINGS, 40, 12, "Кольчужные поножи", false));
                    items.add(new ShopItem(Material.IRON_LEGGINGS, 80, 25, "Железные поножи", false));
                    items.add(new ShopItem(Material.COPPER_LEGGINGS, 45, 15, "Медные поножи", false));
                    // Ботинки
                    items.add(new ShopItem(Material.LEATHER_BOOTS, 10, 3, "Кожаные ботинки", false));
                    items.add(new ShopItem(Material.CHAINMAIL_BOOTS, 20, 6, "Кольчужные ботинки", false));
                    items.add(new ShopItem(Material.IRON_BOOTS, 40, 12, "Железные ботинки", false));
                    items.add(new ShopItem(Material.COPPER_BOOTS, 30, 10, "Медные ботинки", false));
                    break;

                case "Оружие":
                    // Мечи
                    items.add(new ShopItem(Material.WOODEN_SWORD, 15, 5, "Деревянный меч", false));
                    items.add(new ShopItem(Material.STONE_SWORD, 30, 10, "Каменный меч", false));
                    items.add(new ShopItem(Material.IRON_SWORD, 80, 25, "Железный меч", false));
                    items.add(new ShopItem(Material.BOW, 60, 20, "Лук", false));
                    items.add(new ShopItem(Material.ARROW, 5, 1, "Стрела", true));
                    // Копья (правильные идентификаторы)
                    items.add(new ShopItem(getMaterial("WOODEN_SPEAR", Material.STICK), 15, 5, "Деревянное копьё", false));
                    items.add(new ShopItem(getMaterial("STONE_SPEAR", Material.COBBLESTONE), 30, 10, "Каменное копьё", false));
                    items.add(new ShopItem(getMaterial("COPPER_SPEAR", Material.COPPER_INGOT), 50, 15, "Медное копьё", false));
                    items.add(new ShopItem(getMaterial("IRON_SPEAR", Material.IRON_INGOT), 80, 25, "Железное копьё", false));
                    items.add(new ShopItem(Material.TRIDENT, 300, 100, "Трезубец", false));
                    break;

                case "Блоки":
                    // Каменные блоки
                    items.add(new ShopItem(Material.COBBLESTONE, 3, 1, "Булыжник", true));
                    items.add(new ShopItem(Material.STONE, 3, 1, "Камень", true));
                    items.add(new ShopItem(Material.GRANITE, 5, 2, "Гранит", true));
                    items.add(new ShopItem(Material.DIORITE, 5, 2, "Диорит", true));
                    items.add(new ShopItem(Material.ANDESITE, 5, 2, "Андезит", true));
                    // Деревянные блоки
                    items.add(new ShopItem(Material.OAK_PLANKS, 5, 2, "Дубовые доски", true));
                    items.add(new ShopItem(Material.SPRUCE_PLANKS, 5, 2, "Еловые доски", true));
                    items.add(new ShopItem(Material.BIRCH_PLANKS, 5, 2, "Берёзовые доски", true));
                    items.add(new ShopItem(Material.JUNGLE_PLANKS, 5, 2, "Джунглевые доски", true));
                    items.add(new ShopItem(Material.ACACIA_PLANKS, 5, 2, "Акациевые доски", true));
                    items.add(new ShopItem(Material.DARK_OAK_PLANKS, 5, 2, "Тёмно-дубовые доски", true));
                    // Песчаник
                    items.add(new ShopItem(Material.SANDSTONE, 8, 3, "Песчаник", true));
                    items.add(new ShopItem(Material.RED_SANDSTONE, 8, 3, "Красный песчаник", true));
                    // Бетон
                    items.add(new ShopItem(Material.WHITE_CONCRETE, 10, 3, "Белый бетон", true));
                    items.add(new ShopItem(Material.BLACK_CONCRETE, 10, 3, "Чёрный бетон", true));
                    items.add(new ShopItem(Material.RED_CONCRETE, 10, 3, "Красный бетон", true));
                    items.add(new ShopItem(Material.BLUE_CONCRETE, 10, 3, "Синий бетон", true));
                    items.add(new ShopItem(Material.GREEN_CONCRETE, 10, 3, "Зелёный бетон", true));
                    // Стекло
                    items.add(new ShopItem(Material.GLASS, 10, 3, "Стекло", true));
                    items.add(new ShopItem(Material.WHITE_STAINED_GLASS, 12, 4, "Белое стекло", true));
                    // Шерсть
                    items.add(new ShopItem(Material.WHITE_WOOL, 8, 2, "Белая шерсть", true));
                    items.add(new ShopItem(Material.RED_WOOL, 8, 2, "Красная шерсть", true));
                    items.add(new ShopItem(Material.BLUE_WOOL, 8, 2, "Синяя шерсть", true));
                    // Терракота
                    items.add(new ShopItem(Material.TERRACOTTA, 12, 4, "Терракота", true));
                    // Прочее
                    items.add(new ShopItem(Material.BRICKS, 20, 5, "Кирпичи", true));
                    items.add(new ShopItem(Material.BOOKSHELF, 30, 10, "Книжные полки", true));
                    items.add(new ShopItem(Material.GLOWSTONE, 40, 12, "Светокамень", true));
                    items.add(new ShopItem(Material.SEA_LANTERN, 50, 15, "Морской фонарь", true));
                    items.add(new ShopItem(Material.IRON_BARS, 15, 5, "Железные прутья", true));
                    items.add(new ShopItem(Material.OAK_FENCE, 10, 3, "Дубовый забор", true));
                    items.add(new ShopItem(Material.OAK_DOOR, 15, 5, "Дубовая дверь", true));
                    items.add(new ShopItem(Material.LADDER, 10, 3, "Лестница", true));
                    items.add(new ShopItem(Material.STONE_BRICKS, 15, 5, "Каменные кирпичи", true));
                    items.add(new ShopItem(Material.MOSSY_COBBLESTONE, 10, 3, "Замшелый булыжник", true));
                    items.add(new ShopItem(Material.OBSIDIAN, 100, 30, "Обсидиан", true));
                    break;
            }

            return items;
        }

        private ItemStack createShopItem(ShopItem item) {
            ItemStack stack = new ItemStack(item.material, 1);

            ItemMeta meta = stack.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.WHITE + item.displayName);
                meta.setLore(Arrays.asList(
                    ChatColor.GRAY + "Цена за 1 шт: " + ChatColor.GREEN + item.buyPrice + " монет",
                    ChatColor.GRAY + "Цена продажи 1 шт: " + ChatColor.GOLD + item.sellPrice + " монет",
                    "",
                    ChatColor.GREEN + "ЛКМ - Купить 1 шт",
                    ChatColor.GREEN + "ПКМ - Купить стак (64)",
                    ChatColor.YELLOW + "Shift+ЛКМ - Купить 16",
                    ChatColor.YELLOW + "Shift+ПКМ - Купить 32"
                ));
                stack.setItemMeta(meta);
            }
            return stack;
        }

        private ItemStack createMenuItem(Material material, String name, String lore) {
            ItemStack item = new ItemStack(material);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(name);
                meta.setLore(Arrays.asList(lore));
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
         * Получить материал по названию с fallback
         */
        private Material getMaterial(String name, Material fallback) {
            try {
                return Material.valueOf(name);
            } catch (IllegalArgumentException e) {
                return fallback;
            }
        }

        @Override
        public Inventory getInventory() {
            return null;
        }

        /**
         * Обработка клика
         */
        public boolean handleItemClick(Player player, int slot, org.bukkit.event.inventory.ClickType click) {
            Inventory inventory = player.getOpenInventory().getTopInventory();
            ItemStack clickedItem = inventory.getItem(slot);

            if (clickedItem == null || clickedItem.getType() == Material.AIR) {
                return false;
            }

            // Кнопка назад
            if (slot == 48) {
                ShopMenu menu = new ShopMenu(plugin);
                menu.open(player);
                return true;
            }

            // Предметы
            if (slot < 45 && clickedItem.hasItemMeta()) {
                String displayName = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName());
                List<ShopItem> items = getItemsForCategory(categoryName);

                for (ShopItem item : items) {
                    if (item.displayName.equals(displayName)) {
                        // Открываем меню покупки
                        PurchaseConfirmMenu purchaseMenu = new PurchaseConfirmMenu(
                            plugin, item.displayName, item.material, item.buyPrice, item.sellPrice, item.isStackable
                        );
                        purchaseMenu.open(player);
                        return true;
                    }
                }
            }

            return false;
        }

        private void buyItem(Player player, ShopItem item, int amount) {
            double balance = plugin.getEconomyManager().getBalance(player.getUniqueId());
            double totalCost = item.buyPrice * amount;

            if (balance < totalCost) {
                player.sendMessage(ChatColor.RED + "Недостаточно денег! Нужно: " + (int)totalCost + " монет");
                return;
            }

            ItemStack newItem = new ItemStack(item.material);
            int maxStack = item.material.getMaxStackSize();
            int actualAmount = Math.min(amount, maxStack);
            newItem.setAmount(actualAmount);

            player.getInventory().addItem(newItem);
            plugin.getEconomyManager().withdraw(player.getUniqueId(), totalCost);
            player.sendMessage(ChatColor.GREEN + "✓ Куплено: " + item.displayName + " x" + actualAmount + " за " + (int)totalCost + " монет");

            // Обновляем баланс в GUI
            updateBalance(player);
        }

        private void updateBalance(Player player) {
            Inventory inventory = player.getOpenInventory().getTopInventory();
            if (inventory.getHolder() instanceof CategoryItemsMenu) {
                ItemStack balanceItem = inventory.getItem(49);
                if (balanceItem != null && balanceItem.hasItemMeta()) {
                    double balance = plugin.getEconomyManager().getBalance(player.getUniqueId());
                    ItemMeta meta = balanceItem.getItemMeta();
                    meta.setDisplayName(ChatColor.GREEN + "Ваш баланс: " + (int)balance);
                    balanceItem.setItemMeta(meta);
                    inventory.setItem(49, balanceItem);
                }
            }
        }

        private void sellItem(Player player, ShopItem item) {
            // Определяем количество для продажи
            int amountToSell = 64;
            if (item.material == Material.BLAZE_ROD ||
                item.material == Material.ENDER_PEARL ||
                item.material == Material.DIAMOND ||
                item.material == Material.EMERALD ||
                item.material == Material.NETHERITE_INGOT ||
                item.material == Material.APPLE ||
                item.material == Material.BREAD ||
                item.material == Material.COOKED_BEEF ||
                item.material == Material.COOKED_PORKCHOP ||
                item.material == Material.GOLDEN_APPLE ||
                item.material == Material.WOODEN_PICKAXE ||
                item.material == Material.STONE_PICKAXE ||
                item.material == Material.IRON_PICKAXE ||
                item.material == Material.DIAMOND_PICKAXE ||
                item.material == Material.WOODEN_AXE ||
                item.material == Material.STONE_AXE ||
                item.material == Material.IRON_AXE ||
                item.material == Material.DIAMOND_AXE ||
                item.material == Material.WOODEN_SHOVEL ||
                item.material == Material.STONE_SHOVEL ||
                item.material == Material.IRON_SHOVEL ||
                item.material == Material.DIAMOND_SHOVEL ||
                item.material == Material.WOODEN_HOE ||
                item.material == Material.STONE_HOE ||
                item.material == Material.IRON_HOE ||
                item.material == Material.DIAMOND_HOE ||
                item.material == Material.WOODEN_SWORD ||
                item.material == Material.STONE_SWORD ||
                item.material == Material.IRON_SWORD ||
                item.material == Material.DIAMOND_SWORD ||
                item.material == Material.BOW ||
                item.material == Material.LEATHER_HELMET ||
                item.material == Material.CHAINMAIL_HELMET ||
                item.material == Material.IRON_HELMET ||
                item.material == Material.DIAMOND_HELMET ||
                item.material == Material.LEATHER_CHESTPLATE ||
                item.material == Material.CHAINMAIL_CHESTPLATE ||
                item.material == Material.IRON_CHESTPLATE ||
                item.material == Material.DIAMOND_CHESTPLATE ||
                item.material == Material.LEATHER_LEGGINGS ||
                item.material == Material.CHAINMAIL_LEGGINGS ||
                item.material == Material.IRON_LEGGINGS ||
                item.material == Material.DIAMOND_LEGGINGS ||
                item.material == Material.LEATHER_BOOTS ||
                item.material == Material.CHAINMAIL_BOOTS ||
                item.material == Material.IRON_BOOTS ||
                item.material == Material.DIAMOND_BOOTS ||
                item.material == Material.CRAFTING_TABLE ||
                item.material == Material.FURNACE ||
                item.material == Material.CHEST ||
                item.material == Material.BUCKET ||
                item.material == Material.SHEARS ||
                item.material == Material.FISHING_ROD ||
                item.material == Material.FLINT_AND_STEEL ||
                item.material == Material.COMPASS ||
                item.material == Material.CLOCK) {
                amountToSell = 1;
            } else if (item.material == Material.TORCH || item.material == Material.LADDER) {
                amountToSell = 16;
            }

            // Считаем сколько есть у игрока
            int hasAmount = 0;
            for (ItemStack stack : player.getInventory().getContents()) {
                if (stack != null && stack.getType() == item.material) {
                    hasAmount += stack.getAmount();
                }
            }

            if (hasAmount < amountToSell) {
                player.sendMessage(ChatColor.RED + "У вас нет достаточно предмета! Нужно: " + amountToSell + ", у вас: " + hasAmount);
                return;
            }

            // Удаляем предметы
            int removed = 0;
            for (ItemStack stack : player.getInventory().getContents()) {
                if (stack != null && stack.getType() == item.material) {
                    int toRemove = Math.min(amountToSell - removed, stack.getAmount());
                    stack.setAmount(stack.getAmount() - toRemove);
                    removed += toRemove;
                    if (removed >= amountToSell) break;
                }
            }

            plugin.getEconomyManager().deposit(player.getUniqueId(), item.sellPrice);
            player.sendMessage(ChatColor.GREEN + "✓ Продано: " + item.displayName + " (" + amountToSell + " шт.) за " + item.sellPrice + " монет");
            
            // Обновляем баланс в GUI
            updateBalance(player);
        }

        private static class ShopItem {
            public final Material material;
            public final int buyPrice;
            public final int sellPrice;
            public final String displayName;
            public final boolean isStackable; // Можно ли покупать стаками (1, 16, 32, 64)

            public ShopItem(Material material, int buyPrice, int sellPrice, String displayName, boolean isStackable) {
                this.material = material;
                this.buyPrice = buyPrice;
                this.sellPrice = sellPrice;
                this.displayName = displayName;
                this.isStackable = isStackable;
            }
        }
    }
}
