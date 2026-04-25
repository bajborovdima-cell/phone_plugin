package com.example.smartphone.listeners;

import com.example.smartphone.SmartPhonePlugin;
import com.example.smartphone.managers.OneBlockManager;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * Слушатель режима "Один блок"
 */
public class OneBlockListener implements Listener {

    private final SmartPhonePlugin plugin;
    private final OneBlockManager oneBlockManager;

    public OneBlockListener(SmartPhonePlugin plugin) {
        this.plugin = plugin;
        this.oneBlockManager = plugin.getOneBlockManager();
    }

    /**
     * Обработка разрушения блока
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();

        // Проверка: игрок на острове
        if (!oneBlockManager.isPlayerOnIsland(player)) {
            return;
        }

        // Обработка разрушения
        if (oneBlockManager.handleBlockBreak(player, block)) {
            event.setCancelled(true); // Отменяем стандартное разрушение
            
            // [ДОБАВЛЕНИЕ В ИНВЕНТАРЬ] Записываем добытый блок
            oneBlockManager.addMinedBlock(player, block.getType());
        }
    }

    /**
     * Запрет установки блоков (кроме креатива)
     */
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();

        // Проверка: игрок на острове
        if (!oneBlockManager.isPlayerOnIsland(player)) {
            return;
        }

        // В креативе можно строить
        if (player.getGameMode() == GameMode.CREATIVE) {
            return;
        }

        // Запрет установки блоков за пределами платформы
        Location loc = event.getBlock().getLocation();
        Location island = oneBlockManager.getIslandLocation();

        if (island == null) return;

        int dx = Math.abs(loc.getBlockX() - island.getBlockX());
        int dz = Math.abs(loc.getBlockZ() - island.getBlockZ());

        // Разрешаем установку только на платформе 5x5
        if (dx > 2 || dz > 2) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "Здесь нельзя строить!");
        }
    }

    /**
     * Защита от падения с острова
     */
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        // Проверка: игрок на острове
        if (!oneBlockManager.isPlayerOnIsland(player)) {
            return;
        }

        Location island = oneBlockManager.getIslandLocation();
        if (island == null) return;

        Location to = event.getTo();
        if (to == null) return;

        // Проверка падения
        if (to.getY() < island.getY() - 5) {
            Location safeLoc = island.clone().add(0, 1, 0);
            player.teleport(safeLoc);
            player.setFallDistance(0);
            player.sendMessage(ChatColor.RED + "Осторожно! Не падайте с острова!");
        }
    }
}
