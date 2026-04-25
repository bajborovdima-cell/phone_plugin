package com.example.smartphone.listeners;

import com.example.smartphone.SmartPhonePlugin;
import com.example.smartphone.data.PlayerDataManager;
import com.example.smartphone.data.PlayerDataManager.Home;
import com.example.smartphone.gui.HomeMenu;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class HomeMenuListener implements Listener {

    private final SmartPhonePlugin plugin;

    public HomeMenuListener(SmartPhonePlugin plugin) {
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
        if (!(inventory.getHolder() instanceof HomeMenu)) {
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

        // [ПРОВЕРКА ТИПА КЛИКА]
        ClickType click = event.getClick();

        if (click.isLeftClick()) {
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

        } else if (click.isRightClick()) {
            // [УДАЛЕНИЕ ДОМА]
            player.closeInventory();
            data.removeHome(homeName);
            plugin.getPlayerDataManager().savePlayer(data);
            player.sendMessage(ChatColor.YELLOW + "✗ Дом '" + homeName + "' удалён!");
        }
    }
}
