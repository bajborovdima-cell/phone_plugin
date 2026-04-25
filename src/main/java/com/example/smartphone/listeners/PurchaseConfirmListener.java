package com.example.smartphone.listeners;

import com.example.smartphone.SmartPhonePlugin;
import com.example.smartphone.data.PlayerDataManager;
import com.example.smartphone.gui.PurchaseConfirmMenu;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * Слушатель меню подтверждения покупки
 */
public class PurchaseConfirmListener implements Listener {

    private final SmartPhonePlugin plugin;

    public PurchaseConfirmListener(SmartPhonePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        Inventory inventory = event.getInventory();

        // Проверка меню подтверждения покупки
        if (!(inventory.getHolder() instanceof PurchaseConfirmMenu)) {
            return;
        }

        event.setCancelled(true);

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) {
            return;
        }

        // Получаем данные о предмете из меню
        PurchaseConfirmMenu menu = (PurchaseConfirmMenu) inventory.getHolder();
        String itemName = menu != null ? menu.getItemName() : null;
        boolean isStackable = menu != null && menu.isStackable();
        if (itemName == null) {
            return;
        }

        // Определяем количество по слоту
        int amount = 1; // По умолчанию 1
        if (isStackable) {
            // Для стакаемых предметов
            switch (event.getSlot()) {
                case 10: amount = 1; break;
                case 11: amount = 16; break;
                case 15: amount = 32; break;
                case 16: amount = 64; break;
                default: return;
            }
        } else {
            // Для не-стакаемых предметов только 1 слот
            if (event.getSlot() != 11) {
                return;
            }
        }

        // Проверяем тип клика - покупка или продажа
        if (event.isLeftClick()) {
            // Покупка
            purchaseItem(player, itemName, amount);
        } else if (event.isRightClick()) {
            // Продажа
            sellItem(player, itemName, amount);
        }
    }

    private void purchaseItem(Player player, String itemName, int amount) {
        PlayerDataManager.PlayerData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
        
        // Находим цену предмета
        int pricePerUnit = getPriceForItem(itemName);
        if (pricePerUnit == 0) {
            player.sendMessage(ChatColor.RED + "Предмет не найден!");
            return;
        }

        int totalCost = pricePerUnit * amount;

        if (data.balance < totalCost) {
            player.sendMessage(ChatColor.RED + "Недостаточно денег! Нужно: " + totalCost + " монет");
            return;
        }

        // Списываем деньги
        data.balance -= totalCost;
        plugin.getPlayerDataManager().savePlayer(data);

        // Даём предмет
        Material material = getMaterialForItem(itemName);
        if (material == null) {
            player.sendMessage(ChatColor.RED + "Материал не найден!");
            return;
        }

        ItemStack item = new ItemStack(material, Math.min(amount, material.getMaxStackSize()));
        player.getInventory().addItem(item);

        player.sendMessage(ChatColor.GREEN + "✓ Куплено: " + itemName + " x" + amount + " за " + totalCost + " монет");

        // Закрываем меню
        player.closeInventory();
    }
    
    private void sellItem(Player player, String itemName, int amount) {
        PlayerDataManager.PlayerData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
        
        // Находим цену продажи предмета
        int sellPricePerUnit = getSellPriceForItem(itemName);
        if (sellPricePerUnit == 0) {
            player.sendMessage(ChatColor.RED + "Предмет не найден!");
            return;
        }

        int totalEarnings = sellPricePerUnit * amount;

        // Проверяем что у игрока есть предмет
        Material material = getMaterialForItem(itemName);
        if (material == null) {
            player.sendMessage(ChatColor.RED + "Материал не найден!");
            return;
        }

        int hasAmount = 0;
        for (ItemStack stack : player.getInventory().getContents()) {
            if (stack != null && stack.getType() == material) {
                hasAmount += stack.getAmount();
            }
        }

        if (hasAmount < amount) {
            player.sendMessage(ChatColor.RED + "У вас нет " + itemName + " в нужном количестве! Нужно: " + amount + ", у вас: " + hasAmount);
            return;
        }

        // Удаляем предметы
        int removed = 0;
        for (ItemStack stack : player.getInventory().getContents()) {
            if (stack != null && stack.getType() == material) {
                int toRemove = Math.min(amount - removed, stack.getAmount());
                stack.setAmount(stack.getAmount() - toRemove);
                removed += toRemove;
                if (removed >= amount) break;
            }
        }

        // Зачисляем деньги
        data.balance += totalEarnings;
        plugin.getPlayerDataManager().savePlayer(data);

        player.sendMessage(ChatColor.GREEN + "✓ Продано: " + itemName + " x" + amount + " за " + totalEarnings + " монет");

        // Закрываем меню
        player.closeInventory();
    }

    private int getPriceForItem(String itemName) {
        // Цены из магазина
        switch (itemName) {
            // Еда
            case "Яблоко": return 10;
            case "Хлеб": return 15;
            case "Жареная говядина": return 20;
            case "Жареная свинина": return 20;
            case "Золотое яблоко": return 100;
            // Инструменты
            case "Деревянная кирка": return 15;
            case "Каменная кирка": return 30;
            case "Железная кирка": return 100;
            case "Деревянный топор": return 15;
            case "Каменный топор": return 30;
            case "Железный топор": return 100;
            case "Деревянная лопата": return 15;
            case "Каменная лопата": return 30;
            case "Железная лопата": return 100;
            case "Деревянная мотыга": return 10;
            case "Каменная мотыга": return 20;
            case "Железная мотыга": return 80;
            case "Верстак": return 20;
            case "Печь": return 30;
            case "Сундук": return 15;
            case "Факел": return 5;
            case "Лестница": return 10;
            case "Ведро": return 50;
            case "Ножницы": return 40;
            case "Удочка": return 60;
            case "Огниво": return 40;
            case "Компас": return 100;
            case "Часы": return 100;
            // Броня
            case "Кожаный шлем": return 15;
            case "Кольчужный шлем": return 25;
            case "Железный шлем": return 50;
            case "Медный шлем": return 35;
            case "Кожаный нагрудник": return 30;
            case "Кольчужный нагрудник": return 50;
            case "Железный нагрудник": return 100;
            case "Медный нагрудник": return 55;
            case "Кожаные поножи": return 25;
            case "Кольчужные поножи": return 40;
            case "Железные поножи": return 80;
            case "Медные поножи": return 45;
            case "Кожаные ботинки": return 10;
            case "Кольчужные ботинки": return 20;
            case "Железные ботинки": return 40;
            case "Медные ботинки": return 30;
            // Оружие
            case "Деревянный меч": return 15;
            case "Каменный меч": return 30;
            case "Железный меч": return 80;
            case "Лук": return 60;
            case "Стрела": return 5;
            case "Деревянное копьё": return 15;
            case "Каменное копьё": return 30;
            case "Медное копьё": return 50;
            case "Железное копьё": return 80;
            case "Трезубец": return 300;
            // Блоки
            case "Булыжник": return 3;
            case "Камень": return 3;
            case "Гранит": return 5;
            case "Диорит": return 5;
            case "Андезит": return 5;
            case "Дубовые доски": return 5;
            case "Еловые доски": return 5;
            case "Берёзовые доски": return 5;
            case "Джунглевые доски": return 5;
            case "Акациевые доски": return 5;
            case "Тёмно-дубовые доски": return 5;
            case "Песчаник": return 8;
            case "Красный песчаник": return 8;
            case "Белый бетон": return 10;
            case "Чёрный бетон": return 10;
            case "Красный бетон": return 10;
            case "Синий бетон": return 10;
            case "Зелёный бетон": return 10;
            case "Стекло": return 10;
            case "Белое стекло": return 12;
            case "Белая шерсть": return 8;
            case "Красная шерсть": return 8;
            case "Синяя шерсть": return 8;
            case "Терракота": return 12;
            case "Кирпичи": return 20;
            case "Книжные полки": return 30;
            case "Светокамень": return 40;
            case "Морской фонарь": return 50;
            case "Железные прутья": return 15;
            case "Дубовый забор": return 10;
            case "Дубовая дверь": return 15;
            case "Каменные кирпичи": return 15;
            case "Замшелый булыжник": return 10;
            case "Обсидиан": return 100;
            default: return 0;
        }
    }

    private int getSellPriceForItem(String itemName) {
        switch (itemName) {
            // Еда
            case "Яблоко": return 3;
            case "Хлеб": return 5;
            case "Жареная говядина": return 8;
            case "Жареная свинина": return 8;
            case "Золотое яблоко": return 30;
            // Инструменты
            case "Деревянная кирка": return 5;
            case "Каменная кирка": return 10;
            case "Железная кирка": return 30;
            case "Деревянный топор": return 5;
            case "Каменный топор": return 10;
            case "Железный топор": return 30;
            case "Деревянная лопата": return 5;
            case "Каменная лопата": return 10;
            case "Железная лопата": return 30;
            case "Деревянная мотыга": return 3;
            case "Каменная мотыга": return 6;
            case "Железная мотыга": return 25;
            case "Верстак": return 5;
            case "Печь": return 10;
            case "Сундук": return 5;
            case "Факел": return 1;
            case "Лестница": return 3;
            case "Ведро": return 15;
            case "Ножницы": return 12;
            case "Удочка": return 20;
            case "Огниво": return 12;
            case "Компас": return 30;
            case "Часы": return 30;
            // Броня
            case "Кожаный шлем": return 5;
            case "Кольчужный шлем": return 8;
            case "Железный шлем": return 15;
            case "Кожаный нагрудник": return 10;
            case "Кольчужный нагрудник": return 15;
            case "Железный нагрудник": return 30;
            case "Медный нагрудник": return 18;
            case "Кожаные поножи": return 8;
            case "Кольчужные поножи": return 12;
            case "Железные поножи": return 25;
            case "Медные поножи": return 15;
            case "Кожаные ботинки": return 3;
            case "Кольчужные ботинки": return 6;
            case "Железные ботинки": return 12;
            case "Медные ботинки": return 10;
            // Оружие
            case "Деревянный меч": return 5;
            case "Каменный меч": return 10;
            case "Железный меч": return 25;
            case "Лук": return 20;
            case "Стрела": return 1;
            case "Трезубец": return 100;
            // Блоки
            case "Булыжник": return 1;
            case "Камень": return 1;
            case "Гранит": return 2;
            case "Диорит": return 2;
            case "Андезит": return 2;
            case "Дубовые доски": return 2;
            case "Еловые доски": return 2;
            case "Берёзовые доски": return 2;
            case "Джунглевые доски": return 2;
            case "Акациевые доски": return 2;
            case "Тёмно-дубовые доски": return 2;
            case "Песчаник": return 3;
            case "Красный песчаник": return 3;
            case "Белый бетон": return 3;
            case "Чёрный бетон": return 3;
            case "Красный бетон": return 3;
            case "Синий бетон": return 3;
            case "Зелёный бетон": return 3;
            case "Стекло": return 3;
            case "Белое стекло": return 4;
            case "Белая шерсть": return 2;
            case "Красная шерсть": return 2;
            case "Синяя шерсть": return 2;
            case "Терракота": return 4;
            case "Кирпичи": return 5;
            case "Книжные полки": return 10;
            case "Светокамень": return 12;
            case "Морской фонарь": return 15;
            case "Железные прутья": return 5;
            case "Дубовый забор": return 3;
            case "Дубовая дверь": return 5;
            case "Каменные кирпичи": return 5;
            case "Замшелый булыжник": return 3;
            case "Обсидиан": return 30;
            // Копья (fallback)
            case "Деревянное копьё": return 5;
            case "Каменное копьё": return 10;
            case "Медное копьё": return 15;
            case "Железное копьё": return 25;
            default: return 0;
        }
    }

    private Material getMaterialForItem(String itemName) {
        switch (itemName) {
            // Еда
            case "Яблоко": return Material.APPLE;
            case "Хлеб": return Material.BREAD;
            case "Жареная говядина": return Material.COOKED_BEEF;
            case "Жареная свинина": return Material.COOKED_PORKCHOP;
            case "Золотое яблоко": return Material.GOLDEN_APPLE;
            // Инструменты
            case "Деревянная кирка": return Material.WOODEN_PICKAXE;
            case "Каменная кирка": return Material.STONE_PICKAXE;
            case "Железная кирка": return Material.IRON_PICKAXE;
            case "Деревянный топор": return Material.WOODEN_AXE;
            case "Каменный топор": return Material.STONE_AXE;
            case "Железный топор": return Material.IRON_AXE;
            case "Деревянная лопата": return Material.WOODEN_SHOVEL;
            case "Каменная лопата": return Material.STONE_SHOVEL;
            case "Железная лопата": return Material.IRON_SHOVEL;
            case "Деревянная мотыга": return Material.WOODEN_HOE;
            case "Каменная мотыга": return Material.STONE_HOE;
            case "Железная мотыга": return Material.IRON_HOE;
            case "Верстак": return Material.CRAFTING_TABLE;
            case "Печь": return Material.FURNACE;
            case "Сундук": return Material.CHEST;
            case "Факел": return Material.TORCH;
            case "Лестница": return Material.LADDER;
            case "Ведро": return Material.BUCKET;
            case "Ножницы": return Material.SHEARS;
            case "Удочка": return Material.FISHING_ROD;
            case "Огниво": return Material.FLINT_AND_STEEL;
            case "Компас": return Material.COMPASS;
            case "Часы": return Material.CLOCK;
            // Броня
            case "Кожаный шлем": return Material.LEATHER_HELMET;
            case "Кольчужный шлем": return Material.CHAINMAIL_HELMET;
            case "Железный шлем": return Material.IRON_HELMET;
            case "Медный шлем": return Material.COPPER_HELMET;
            case "Кожаный нагрудник": return Material.LEATHER_CHESTPLATE;
            case "Кольчужный нагрудник": return Material.CHAINMAIL_CHESTPLATE;
            case "Железный нагрудник": return Material.IRON_CHESTPLATE;
            case "Медный нагрудник": return Material.COPPER_CHESTPLATE;
            case "Кожаные поножи": return Material.LEATHER_LEGGINGS;
            case "Кольчужные поножи": return Material.CHAINMAIL_LEGGINGS;
            case "Железные поножи": return Material.IRON_LEGGINGS;
            case "Медные поножи": return Material.COPPER_LEGGINGS;
            case "Кожаные ботинки": return Material.LEATHER_BOOTS;
            case "Кольчужные ботинки": return Material.CHAINMAIL_BOOTS;
            case "Железные ботинки": return Material.IRON_BOOTS;
            case "Медные ботинки": return Material.COPPER_BOOTS;
            // Оружие
            case "Деревянный меч": return Material.WOODEN_SWORD;
            case "Каменный меч": return Material.STONE_SWORD;
            case "Железный меч": return Material.IRON_SWORD;
            case "Лук": return Material.BOW;
            case "Стрела": return Material.ARROW;
            case "Трезубец": return Material.TRIDENT;
            // Блоки
            case "Булыжник": return Material.COBBLESTONE;
            case "Камень": return Material.STONE;
            case "Гранит": return Material.GRANITE;
            case "Диорит": return Material.DIORITE;
            case "Андезит": return Material.ANDESITE;
            case "Дубовые доски": return Material.OAK_PLANKS;
            case "Еловые доски": return Material.SPRUCE_PLANKS;
            case "Берёзовые доски": return Material.BIRCH_PLANKS;
            case "Джунглевые доски": return Material.JUNGLE_PLANKS;
            case "Акациевые доски": return Material.ACACIA_PLANKS;
            case "Тёмно-дубовые доски": return Material.DARK_OAK_PLANKS;
            case "Песчаник": return Material.SANDSTONE;
            case "Красный песчаник": return Material.RED_SANDSTONE;
            case "Белый бетон": return Material.WHITE_CONCRETE;
            case "Чёрный бетон": return Material.BLACK_CONCRETE;
            case "Красный бетон": return Material.RED_CONCRETE;
            case "Синий бетон": return Material.BLUE_CONCRETE;
            case "Зелёный бетон": return Material.GREEN_CONCRETE;
            case "Стекло": return Material.GLASS;
            case "Белое стекло": return Material.WHITE_STAINED_GLASS;
            case "Белая шерсть": return Material.WHITE_WOOL;
            case "Красная шерсть": return Material.RED_WOOL;
            case "Синяя шерсть": return Material.BLUE_WOOL;
            case "Терракота": return Material.TERRACOTTA;
            case "Кирпичи": return Material.BRICKS;
            case "Книжные полки": return Material.BOOKSHELF;
            case "Светокамень": return Material.GLOWSTONE;
            case "Морской фонарь": return Material.SEA_LANTERN;
            case "Железные прутья": return Material.IRON_BARS;
            case "Дубовый забор": return Material.OAK_FENCE;
            case "Дубовая дверь": return Material.OAK_DOOR;
            case "Каменные кирпичи": return Material.STONE_BRICKS;
            case "Замшелый булыжник": return Material.MOSSY_COBBLESTONE;
            case "Обсидиан": return Material.OBSIDIAN;
            // Копья (fallback)
            case "Деревянное копьё": return Material.STICK;
            case "Каменное копьё": return Material.COBBLESTONE;
            case "Медное копьё": return Material.COPPER_INGOT;
            case "Железное копьё": return Material.IRON_INGOT;
            default: return null;
        }
    }
}
