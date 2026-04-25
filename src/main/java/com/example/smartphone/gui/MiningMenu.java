package com.example.smartphone.gui;

import com.example.smartphone.SmartPhonePlugin;
import com.example.smartphone.data.PlayerDataManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

/**
 * Меню шахтинга - уровень, прогресс, награды
 */
public class MiningMenu implements InventoryHolder {

    private final SmartPhonePlugin plugin;
    private static final String MENU_TITLE = ChatColor.DARK_GRAY + "Шахтинг";

    public MiningMenu(SmartPhonePlugin plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        Inventory inventory = Bukkit.createInventory(this, 27, MENU_TITLE);
        fillMenu(inventory, player);
        player.openInventory(inventory);
    }

    private void fillMenu(Inventory inventory, Player player) {
        PlayerDataManager.PlayerData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
        
        // Получаем уровень шахтинга из данных игрока (нужно добавить в PlayerData)
        int miningLevel = data.miningLevel;
        int miningXp = data.miningXp;
        int maxLevel = plugin.getConfig().getInt("mining.maxLevel", 100);
        int xpPerBlock = plugin.getConfig().getInt("mining.xpPerBlock", 1);

        // [РЯД 1: Информация об уровне]
        inventory.setItem(0, createLevelItem(miningLevel, maxLevel));
        inventory.setItem(1, createXpItem(miningXp, xpPerBlock));
        inventory.setItem(2, createProgressBarItem(miningLevel, maxLevel));

        // [РЯД 2: Награды за уровни]
        inventory.setItem(9, createRewardItem(10, 100, miningLevel >= 10));
        inventory.setItem(10, createRewardItem(25, 500, miningLevel >= 25));
        inventory.setItem(11, createRewardItem(50, 1000, miningLevel >= 50));
        inventory.setItem(12, createRewardItem(100, 5000, miningLevel >= 100));

        // [РЯД 3: Настройки]
        inventory.setItem(18, createMaxLevelItem(maxLevel));
        
        // [ЗАПОЛНИТЕЛИ]
        for (int i : new int[]{3, 4, 5, 6, 7, 8, 13, 14, 15, 16, 17, 19, 20, 21, 22, 23, 24, 25, 26}) {
            inventory.setItem(i, createGlassPane());
        }
    }

    private ItemStack createLevelItem(int level, int maxLevel) {
        ItemStack item = new ItemStack(Material.EXPERIENCE_BOTTLE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.GOLD + "Уровень шахтинга");
            meta.setLore(Arrays.asList(
                ChatColor.AQUA + "Текущий уровень: " + ChatColor.GREEN + level,
                ChatColor.GRAY + "Максимальный уровень: " + maxLevel,
                "",
                ChatColor.YELLOW + "Добывайте блоки для получения опыта"
            ));
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createXpItem(int xp, int xpPerBlock) {
        ItemStack item = new ItemStack(Material.EXPERIENCE_BOTTLE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.LIGHT_PURPLE + "Опыт");
            meta.setLore(Arrays.asList(
                ChatColor.AQUA + "Текущий опыт: " + ChatColor.GREEN + xp,
                ChatColor.GRAY + "Опыт за блок: " + xpPerBlock,
                "",
                ChatColor.YELLOW + "1 блок = " + xpPerBlock + " XP"
            ));
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createProgressBarItem(int level, int maxLevel) {
        ItemStack item = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            double progress = (double) level / maxLevel * 100;
            meta.setDisplayName(ChatColor.WHITE + "Прогресс");
            meta.setLore(Arrays.asList(
                ChatColor.AQUA + "Прогресс: " + ChatColor.GREEN + String.format("%.1f", progress) + "%",
                ChatColor.GRAY + "Уровень " + level + " из " + maxLevel,
                "",
                ChatColor.YELLOW + "▮▮▮▮▮▮▮▮▮▮▮▮▮▮▮▮▮▮▮▮▮▮▮▮▮"
            ));
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createRewardItem(int requiredLevel, int reward, boolean unlocked) {
        ItemStack item = new ItemStack(unlocked ? Material.GOLD_INGOT : Material.IRON_BARS);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName((unlocked ? ChatColor.GREEN : ChatColor.RED) + "Награда за " + requiredLevel + " уровень");
            meta.setLore(Arrays.asList(
                ChatColor.GRAY + "Награда: " + ChatColor.GOLD + reward + " монет",
                "",
                unlocked ? 
                    ChatColor.GREEN + "✓ Получено" :
                    ChatColor.RED + "✗ Требуется " + requiredLevel + " уровень",
                "",
                ChatColor.YELLOW + "Клик для получения"
            ));
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createMaxLevelItem(int maxLevel) {
        ItemStack item = new ItemStack(Material.BOOK);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.DARK_PURPLE + "Максимальный уровень");
            meta.setLore(Arrays.asList(
                ChatColor.AQUA + "Макс. уровень: " + ChatColor.GREEN + maxLevel,
                "",
                ChatColor.GRAY + "Начальный уровень: 0",
                ChatColor.GRAY + "Опыт за блок: 1 XP",
                "",
                ChatColor.YELLOW + "Настройте в config.yml"
            ));
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
