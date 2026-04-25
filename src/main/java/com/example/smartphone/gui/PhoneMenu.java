package com.example.smartphone.gui;

import com.example.smartphone.SmartPhonePlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class PhoneMenu implements InventoryHolder {

    private final SmartPhonePlugin plugin;
    private static final int MENU_SIZE = 54; // Увеличено до 54 слотов
    private static final String MENU_TITLE = ChatColor.BLUE + "Телефон";

    public PhoneMenu(SmartPhonePlugin plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        Inventory inventory = Bukkit.createInventory(this, MENU_SIZE, MENU_TITLE);
        fillMenu(inventory, player);
        player.openInventory(inventory);
    }

    private void fillMenu(Inventory inventory, Player player) {
        // [РЯД 1: Функции]
        inventory.setItem(0, createMenuItem(
            Material.CARROT_ON_A_STICK,
            ChatColor.YELLOW + "Такси",
            ChatColor.GRAY + "Вызвать такси",
            ChatColor.GRAY + "Цена: §e50 монет"
        ));

        inventory.setItem(1, createMenuItem(
            Material.RED_BED,
            ChatColor.GREEN + "Дом",
            ChatColor.GRAY + "Телепортироваться домой",
            ChatColor.GRAY + "Команда: §e/home"
        ));

        inventory.setItem(2, createMenuItem(
            Material.BOW,
            ChatColor.RED + "Киллер",
            ChatColor.GRAY + "Нанять киллера",
            ChatColor.GRAY + "Цена: §e500+ монет"
        ));

        inventory.setItem(3, createMenuItem(
            Material.IRON_CHESTPLATE,
            ChatColor.BLUE + "Охранник",
            ChatColor.GRAY + "Вызвать охранника",
            ChatColor.GRAY + "Цена: §e100 монет"
        ));

        inventory.setItem(4, createMenuItem(
            Material.EMERALD,
            ChatColor.GOLD + "Магазин",
            ChatColor.GRAY + "Купить/Продать",
            ChatColor.GRAY + "Команда: §e/shop"
        ));

        inventory.setItem(5, createMenuItem(
            Material.OAK_DOOR,
            ChatColor.DARK_GREEN + "Недвижимость",
            ChatColor.GRAY + "Купить готовый дом",
            ChatColor.GRAY + "Маленький, средний, большой"
        ));

        inventory.setItem(6, createMenuItem(
            Material.NETHER_STAR,
            ChatColor.LIGHT_PURPLE + "Перки",
            ChatColor.GRAY + "Урон, выносливость, bhop",
            ChatColor.GRAY + "Двойной прыжок"
        ));

        inventory.setItem(7, createMenuItem(
            Material.BLAZE_ROD,
            ChatColor.RED + "🔥 Палка",
            ChatColor.GRAY + "Купить огненный стержень",
            ChatColor.GRAY + "Цена: §e650 монет"
        ));

        inventory.setItem(8, createMenuItem(
            Material.DIAMOND_PICKAXE,
            ChatColor.AQUA + "Шахтинг",
            ChatColor.GRAY + "Уровень и награды",
            ChatColor.GRAY + "Добыча блоков = XP"
        ));

        inventory.setItem(12, createMenuItem(
            Material.GRASS_BLOCK,
            ChatColor.DARK_GREEN + "🌍 Один блок",
            ChatColor.GRAY + "Режим выживания на острове",
            ChatColor.GRAY + "Общий остров для всех"
        ));

        inventory.setItem(13, createMenuItem(
            Material.DIAMOND_SWORD,
            ChatColor.RED + "⚔️ Дуэль",
            ChatColor.GRAY + "Вызвать игрока на дуэль",
            ChatColor.GRAY + "1v1 на арене"
        ));

        // [РЯД 2: Информация]
        double balance = plugin.getEconomyManager().getBalance(player.getUniqueId());
        inventory.setItem(18, createMenuItem(
            Material.GOLD_INGOT,
            ChatColor.GOLD + "Баланс",
            ChatColor.GRAY + "Ваш счёт: §e" + balance + " монет",
            ChatColor.GRAY + "Команда: §e/balance"
        ));

        inventory.setItem(10, createMenuItem(
            Material.PAPER,
            ChatColor.AQUA + "Перевод",
            ChatColor.GRAY + "Перевести деньги",
            ChatColor.GRAY + "Команда: §e/pay <игрок> <сумма>"
        ));

        inventory.setItem(11, createMenuItem(
            Material.COMPASS,
            ChatColor.DARK_PURPLE + "Топ игроков",
            ChatColor.GRAY + "Богатейшие игроки",
            ChatColor.GRAY + "Команда: §e/top"
        ));

        // [РЯД 3: Заполнитель]
        for (int i = 27; i < 54; i++) {
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
}
