package com.example.smartphone.listeners;

import com.example.smartphone.SmartPhonePlugin;
import com.example.smartphone.data.PlayerDataManager;
import com.example.smartphone.gui.MiningMenu;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * Слушатель шахтинга: опыт, уровни, награды
 */
public class MiningListener implements Listener {

    private final SmartPhonePlugin plugin;

    public MiningListener(SmartPhonePlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Начисление опыта за добычу блоков
     */
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        PlayerDataManager.PlayerData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());

        // Получаем настройки из конфига
        if (!plugin.getConfig().getBoolean("mining.enabled", true)) {
            return;
        }

        int xpPerBlock = plugin.getConfig().getInt("mining.xpPerBlock", 1);
        int maxLevel = plugin.getConfig().getInt("mining.maxLevel", 100);
        int perkPointsRate = plugin.getConfig().getInt("mining.perkPointsRate", 10); // 1 очко за N блоков

        // [НАЧИСЛЕНИЕ ОПЫТА]
        data.miningXp += xpPerBlock;

        // [НАЧИСЛЕНИЕ ОЧКОВ ПЕРКОВ] 1 очко за каждые 10 блоков
        data.miningPerkProgress++;
        if (data.miningPerkProgress >= perkPointsRate) {
            data.perkPoints++;
            data.miningPerkProgress = 0;
            player.sendMessage(ChatColor.LIGHT_PURPLE + "✨ +1 очко перков за майнинг!");
        }

        // [ПРОВЕРКА НА ПОВЫШЕНИЕ УРОВНЯ]
        int xpNeededForNextLevel = data.miningLevel * 10; // 10 XP за уровень
        if (data.miningXp >= xpNeededForNextLevel && data.miningLevel < maxLevel) {
            data.miningLevel++;
            data.miningXp -= xpNeededForNextLevel;
            player.sendMessage(ChatColor.GREEN + "✓ Уровень шахтинга повышен: " + data.miningLevel);

            // [ПРОВЕРКА НАГРАД]
            checkReward(player, data);
        }

        plugin.getPlayerDataManager().savePlayer(data);
    }

    /**
     * Проверка и выдача наград
     */
    private void checkReward(Player player, PlayerDataManager.PlayerData data) {
        int level = data.miningLevel;

        if (level >= 10 && !data.miningReward10Claimed) {
            int reward = plugin.getConfig().getInt("mining.rewards.level10", 100);
            plugin.getEconomyManager().deposit(player.getUniqueId(), reward);
            data.miningReward10Claimed = true;
            plugin.getPlayerDataManager().savePlayer(data);
            player.sendMessage(ChatColor.GOLD + "🎁 Награда за 10 уровень: " + reward + " монет!");
        }
        if (level >= 25 && !data.miningReward25Claimed) {
            int reward = plugin.getConfig().getInt("mining.rewards.level25", 500);
            plugin.getEconomyManager().deposit(player.getUniqueId(), reward);
            data.miningReward25Claimed = true;
            plugin.getPlayerDataManager().savePlayer(data);
            player.sendMessage(ChatColor.GOLD + "🎁 Награда за 25 уровень: " + reward + " монет!");
        }
        if (level >= 50 && !data.miningReward50Claimed) {
            int reward = plugin.getConfig().getInt("mining.rewards.level50", 1000);
            plugin.getEconomyManager().deposit(player.getUniqueId(), reward);
            data.miningReward50Claimed = true;
            plugin.getPlayerDataManager().savePlayer(data);
            player.sendMessage(ChatColor.GOLD + "🎁 Награда за 50 уровень: " + reward + " монет!");
        }
        if (level >= 100 && !data.miningReward100Claimed) {
            int reward = plugin.getConfig().getInt("mining.rewards.level100", 5000);
            plugin.getEconomyManager().deposit(player.getUniqueId(), reward);
            data.miningReward100Claimed = true;
            plugin.getPlayerDataManager().savePlayer(data);
            player.sendMessage(ChatColor.GOLD + "🎁 Награда за 100 уровень: " + reward + " монет!");
            player.sendMessage(ChatColor.GOLD + "🏆 МАКСИМАЛЬНЫЙ УРОВЕНЬ ШАХТИНГА!");
        }
    }

    /**
     * Обработка кликов в меню шахтинга
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        Inventory inventory = event.getInventory();

        if (!(inventory.getHolder() instanceof MiningMenu)) {
            return;
        }

        event.setCancelled(true);

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) {
            return;
        }

        if (!clickedItem.hasItemMeta() || !clickedItem.getItemMeta().hasDisplayName()) {
            return;
        }

        PlayerDataManager.PlayerData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
        MiningMenu miningMenu = new MiningMenu(plugin);

        // [ОБРАБОТКА КЛИКОВ ПО НАГРАДАМ]
        switch (event.getSlot()) {
            case 9: // Награда за 10 уровень
                claimReward(player, data, 10, 100, miningMenu);
                break;

            case 10: // Награда за 25 уровень
                claimReward(player, data, 25, 500, miningMenu);
                break;

            case 11: // Награда за 50 уровень
                claimReward(player, data, 50, 1000, miningMenu);
                break;

            case 12: // Награда за 100 уровень
                claimReward(player, data, 100, 5000, miningMenu);
                break;
        }
    }

    private void claimReward(Player player, PlayerDataManager.PlayerData data,
                            int requiredLevel, int reward, MiningMenu menu) {
        if (data.miningLevel < requiredLevel) {
            player.sendMessage(ChatColor.RED + "Требуется уровень " + requiredLevel + "!");
            return;
        }

        // Проверяем, получена ли уже награда
        boolean claimed = false;
        switch (requiredLevel) {
            case 10: claimed = data.miningReward10Claimed; break;
            case 25: claimed = data.miningReward25Claimed; break;
            case 50: claimed = data.miningReward50Claimed; break;
            case 100: claimed = data.miningReward100Claimed; break;
        }

        if (claimed) {
            player.sendMessage(ChatColor.RED + "Вы уже получили эту награду!");
            return;
        }

        plugin.getEconomyManager().deposit(player.getUniqueId(), reward);
        player.sendMessage(ChatColor.GREEN + "✓ Получено " + reward + " монет!");
        
        // Отмечаем награду как полученную
        switch (requiredLevel) {
            case 10: data.miningReward10Claimed = true; break;
            case 25: data.miningReward25Claimed = true; break;
            case 50: data.miningReward50Claimed = true; break;
            case 100: data.miningReward100Claimed = true; break;
        }
        plugin.getPlayerDataManager().savePlayer(data);
    }
}
