package com.example.smartphone.listeners;

import com.example.smartphone.SmartPhonePlugin;
import com.example.smartphone.data.PlayerDataManager;
import com.example.smartphone.data.PlayerDataManager.Home;
import com.example.smartphone.entities.GuardManager;
import com.example.smartphone.entities.KillerManager.KillerTier;
import com.example.smartphone.gui.*;
import com.example.smartphone.managers.TaxiManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class GUIMenuListener implements Listener {

    private final SmartPhonePlugin plugin;

    public GUIMenuListener(SmartPhonePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        Inventory inventory = event.getInventory();

        // [МЕНЮ ДОМОВ]
        if (inventory.getHolder() instanceof HomeMenu) {
            handleHomeClick(event, player, inventory);
            return;
        }

        // [МЕНЮ КИЛЛЕРА - ВЫБОР ЦЕЛИ]
        if (inventory.getHolder() instanceof KillerMenu) {
            handleKillerTargetClick(event, player, inventory);
            return;
        }

        // [МЕНЮ КИЛЛЕРА - ВЫБОР УРОВНЯ]
        if (inventory.getHolder() instanceof KillerTierMenu) {
            handleKillerTierClick(event, player, inventory);
            return;
        }

        // [МЕНЮ ОХРАНЫ - ВЫБОР ЦЕЛИ]
        if (inventory.getHolder() instanceof com.example.smartphone.gui.GuardMenu) {
            handleGuardTargetClick(event, player, inventory);
            return;
        }

        // [МЕНЮ ОХРАНЫ - ВЫБОР УРОВНЯ]
        if (inventory.getHolder() instanceof com.example.smartphone.gui.GuardTierMenu) {
            handleGuardTierClick(event, player, inventory);
            return;
        }

        // [ТАКСИ - ВЫБОР НАЗНАЧЕНИЯ]
        if (inventory.getHolder() instanceof TaxiDestinationMenu) {
            handleTaxiDestinationClick(event, player, inventory);
            return;
        }
    }

    private void handleHomeClick(InventoryClickEvent event, Player player, Inventory inventory) {
        event.setCancelled(true);
        
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) {
            return;
        }

        if (!clickedItem.hasItemMeta() || !clickedItem.getItemMeta().hasDisplayName()) {
            return;
        }

        String displayName = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName());
        if (!displayName.startsWith("🏠 ")) {
            return;
        }

        // [ПОЛУЧЕНИЕ НАЗВАНИЯ ДОМА]
        String homeName = displayName.replace("🏠 ", "").trim();
        
        PlayerDataManager.PlayerData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
        Home home = data.getHome(homeName);
        
        if (home == null) {
            player.sendMessage(ChatColor.RED + "Дом не найден!");
            return;
        }

        // [ТЕЛЕПОРТАЦИЯ]
        Location homeLoc = new Location(
            player.getServer().getWorld(home.world),
            home.x, home.y, home.z
        );
        
        player.teleport(homeLoc);
        player.sendMessage(ChatColor.GREEN + "✓ Вы телепортировались в дом '" + homeName + "'!");
        
        // [КУЛДАУН]
        int cooldown = plugin.getConfig().getInt("home.cooldown", 30);
        data.homeCooldown = System.currentTimeMillis() + (cooldown * 1000L);
        plugin.getPlayerDataManager().savePlayer(data);
    }

    private void handleKillerTargetClick(InventoryClickEvent event, Player player, Inventory inventory) {
        event.setCancelled(true);
        
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) {
            return;
        }

        if (!clickedItem.hasItemMeta() || !clickedItem.getItemMeta().hasDisplayName()) {
            return;
        }

        String displayName = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName());
        if (!displayName.startsWith("⚔️ ")) {
            return;
        }

        // [ПОЛУЧЕНИЕ ЦЕЛИ]
        String targetName = displayName.replace("⚔️ ", "").trim();
        Player target = player.getServer().getPlayer(targetName);
        
        if (target == null || !target.isOnline()) {
            player.sendMessage(ChatColor.RED + "Игрок больше не онлайн!");
            player.closeInventory();
            return;
        }

        // [ОТКРЫТИЕ МЕНЮ УРОВНЯ]
        player.closeInventory();
        KillerTierMenu tierMenu = new KillerTierMenu(target);
        tierMenu.open(player);
    }

    private void handleKillerTierClick(InventoryClickEvent event, Player player, Inventory inventory) {
        event.setCancelled(true);
        
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) {
            return;
        }

        if (!clickedItem.hasItemMeta() || !clickedItem.getItemMeta().hasDisplayName()) {
            return;
        }

        String displayName = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName());
        
        KillerTier tier;
        if (displayName.contains("Обычный")) {
            tier = KillerTier.BASIC;
        } else if (displayName.contains("Профессионал")) {
            tier = KillerTier.PROFESSIONAL;
        } else if (displayName.contains("Легенда")) {
            tier = KillerTier.LEGENDARY;
        } else {
            return;
        }

        // [НАЙМ КИЛЛЕРА]
        KillerTierMenu currentMenu = (KillerTierMenu) inventory.getHolder();
        Player target = currentMenu.getTarget();
        
        if (target == null || !target.isOnline()) {
            player.sendMessage(ChatColor.RED + "Цель больше не онлайн!");
            player.closeInventory();
            return;
        }

        double cost = tier.getCost();
        if (!plugin.getEconomyManager().hasBalance(player.getUniqueId(), cost)) {
            player.sendMessage(ChatColor.RED + "Недостаточно денег! Нужно: " + cost + " монет");
            player.closeInventory();
            return;
        }

        // [СПАВН КИЛЛЕРА]
        plugin.getKillerManager().spawnKiller(player, target, tier);
        plugin.getEconomyManager().withdraw(player.getUniqueId(), cost);

        player.sendMessage(ChatColor.GREEN + "✓ Киллер уровня '" + tier.getName() + "' нанят!");
        player.closeInventory();
    }

    private void handleGuardTargetClick(InventoryClickEvent event, Player player, Inventory inventory) {
        event.setCancelled(true);
        
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) {
            return;
        }

        if (!clickedItem.hasItemMeta() || !clickedItem.getItemMeta().hasDisplayName()) {
            return;
        }

        String displayName = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName());
        
        Player target;
        
        // [СЕБЯ]
        if (displayName.contains("✓ Себя")) {
            target = player;
        } else if (displayName.startsWith("🛡️ ")) {
            // [ИГРОК]
            String targetName = displayName.replace("🛡️ ", "").trim();
            target = player.getServer().getPlayer(targetName);
            
            if (target == null || !target.isOnline()) {
                player.sendMessage(ChatColor.RED + "Игрок больше не онлайн!");
                player.closeInventory();
                return;
            }
        } else {
            return;
        }

        // [ОТКРЫТИЕ МЕНЮ УРОВНЯ]
        player.closeInventory();
        com.example.smartphone.gui.GuardTierMenu tierMenu = new com.example.smartphone.gui.GuardTierMenu(target);
        tierMenu.open(player);
    }

    private void handleGuardTierClick(InventoryClickEvent event, Player player, Inventory inventory) {
        event.setCancelled(true);
        
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) {
            return;
        }

        if (!clickedItem.hasItemMeta() || !clickedItem.getItemMeta().hasDisplayName()) {
            return;
        }

        String displayName = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName());

        com.example.smartphone.entities.GuardManager.GuardTier tier;
        if (displayName.contains("Обычная")) {
            tier = com.example.smartphone.entities.GuardManager.GuardTier.BASIC;
        } else if (displayName.contains("Профессионал")) {
            tier = com.example.smartphone.entities.GuardManager.GuardTier.PROFESSIONAL;
        } else if (displayName.contains("ЭЛИТА") || displayName.contains("Элита")) {
            tier = com.example.smartphone.entities.GuardManager.GuardTier.ELITE;
        } else if (displayName.contains("ВЕЧНАЯ") || displayName.contains("Вечная")) {
            tier = com.example.smartphone.entities.GuardManager.GuardTier.ETERNAL;
        } else {
            return;
        }

        // [НАЙМ ОХРАНЫ]
        com.example.smartphone.gui.GuardTierMenu currentMenu = (com.example.smartphone.gui.GuardTierMenu) inventory.getHolder();
        Player target = currentMenu.getTarget();
        
        if (target == null || !target.isOnline()) {
            player.sendMessage(ChatColor.RED + "Цель больше не онлайн!");
            player.closeInventory();
            return;
        }

        double cost = tier.getCost();
        if (!plugin.getEconomyManager().hasBalance(player.getUniqueId(), cost)) {
            player.sendMessage(ChatColor.RED + "Недостаточно денег! Нужно: " + cost + " монет");
            player.closeInventory();
            return;
        }

        // [СПАВН ОХРАНЫ]
        plugin.getGuardManager().spawnGuard(player, target, tier);
        plugin.getEconomyManager().withdraw(player.getUniqueId(), cost);

        player.sendMessage(ChatColor.GREEN + "✓ Охрана уровня '" + tier.getName() + "' нанята для " + target.getName() + "!");
        player.closeInventory();
    }

    private void handleTaxiDestinationClick(InventoryClickEvent event, Player player, Inventory inventory) {
        event.setCancelled(true);
        
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) {
            return;
        }

        if (!clickedItem.hasItemMeta() || !clickedItem.getItemMeta().hasDisplayName()) {
            return;
        }

        String displayName = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName());
        double cost = 50;
        Location destination = null;
        
        // [СПАВН]
        if (displayName.contains("Спавн")) {
            destination = player.getServer().getWorlds().get(0).getSpawnLocation();
        }
        // [ДОМ]
        else if (displayName.startsWith("🏠 Дом:")) {
            String homeName = displayName.replace("🏠 Дом:", "").trim();
            PlayerDataManager.PlayerData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
            Home home = data.getHome(homeName);
            if (home != null) {
                destination = new Location(player.getServer().getWorld(home.world), home.x, home.y, home.z);
            }
        }
        // [ДРУГ]
        else if (displayName.startsWith("👤 Друг:")) {
            String friendName = displayName.replace("👤 Друг:", "").trim();
            Player friend = player.getServer().getPlayer(friendName);
            if (friend != null && friend.isOnline()) {
                destination = friend.getLocation();
            }
        }
        
        if (destination != null) {
            player.closeInventory();
            TaxiManager taxiManager = new TaxiManager(plugin);
            taxiManager.startTaxiRide(player, destination, cost);
        }
    }
}
