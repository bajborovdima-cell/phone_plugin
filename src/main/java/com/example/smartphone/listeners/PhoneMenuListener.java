package com.example.smartphone.listeners;

import com.example.smartphone.SmartPhonePlugin;
import com.example.smartphone.gui.GuardMenu;
import com.example.smartphone.gui.HomeMenu;
import com.example.smartphone.gui.HouseMenu;
import com.example.smartphone.gui.KillerMenu;
import com.example.smartphone.gui.MiningMenu;
import com.example.smartphone.gui.PerksMenu;
import com.example.smartphone.gui.PhoneMenu;
import com.example.smartphone.gui.TaxiDestinationMenu;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class PhoneMenuListener implements Listener {

    private final SmartPhonePlugin plugin;

    public PhoneMenuListener(SmartPhonePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        Inventory inventory = event.getInventory();

        // [ПРОВЕРКА МЕНЮ ТЕЛЕФОНА] - проверяем по держателю
        if (!(inventory.getHolder() instanceof PhoneMenu)) {
            return;
        }

        event.setCancelled(true); // [ОТМЕНА] Нельзя забирать предметы

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) {
            return;
        }

        int slot = event.getSlot();

        // [ОБРАБОТКА КНОПОК]
        switch (slot) {
            case 0: // Такси
                player.closeInventory();
                TaxiDestinationMenu taxiMenu = new TaxiDestinationMenu(plugin);
                taxiMenu.open(player);
                break;

            case 1: // Дом
                player.closeInventory();
                HomeMenu homeMenu = new HomeMenu(plugin);
                homeMenu.open(player);
                break;

            case 2: // Киллер
                player.closeInventory();
                KillerMenu killerMenu = new KillerMenu();
                killerMenu.open(player);
                break;

            case 3: // Охранник
                player.closeInventory();
                GuardMenu guardMenu = new GuardMenu();
                guardMenu.open(player);
                break;

            case 4: // Магазин
                player.closeInventory();
                player.performCommand("shop");
                break;

            case 5: // Недвижимость
                player.closeInventory();
                HouseMenu houseMenu = new HouseMenu(plugin);
                houseMenu.open(player);
                break;

            case 6: // Перки
                player.closeInventory();
                PerksMenu perksMenu = new PerksMenu(plugin);
                perksMenu.open(player);
                break;

            case 7: // Палка
                player.closeInventory();
                buyBlazeRod(player);
                break;

            case 8: // Шахтинг
                player.closeInventory();
                MiningMenu miningMenu = new MiningMenu(plugin);
                miningMenu.open(player);
                break;

            case 9: // Баланс
                player.closeInventory();
                double balance = plugin.getEconomyManager().getBalance(player.getUniqueId());
                player.sendMessage(ChatColor.GOLD + "Ваш баланс: " + ChatColor.GREEN + balance + " монет");
                break;

            case 10: // Перевод
                player.closeInventory();
                player.sendMessage(ChatColor.AQUA + "Перевод: /pay <игрок> <сумма>");
                break;

            case 11: // Топ
                player.closeInventory();
                player.performCommand("top");
                break;

            case 12: // Один блок
                player.closeInventory();
                if (plugin.getOneBlockManager().isPlayerOnIsland(player)) {
                    // Игрок на острове - открыть меню выхода
                    com.example.smartphone.gui.OneBlockMenu menu = new com.example.smartphone.gui.OneBlockMenu(plugin);
                    menu.open(player);
                } else {
                    // Игрок на спавне - телепортировать на остров
                    plugin.getOneBlockManager().teleportToIsland(player);
                }
                break;

            case 13: // Дуэль
                player.closeInventory();
                com.example.smartphone.gui.DuelMenu duelMenu = new com.example.smartphone.gui.DuelMenu(plugin);
                duelMenu.open(player);
                break;
        }
    }

    /**
     * Покупка огненного стержня (палки)
     */
    private void buyBlazeRod(Player player) {
        int price = 650;

        // [ПРОВЕРКА БАЛАНСА]
        if (!plugin.getEconomyManager().hasBalance(player.getUniqueId(), price)) {
            player.sendMessage(ChatColor.RED + "Недостаточно денег! Нужно: " + price + " монет");
            return;
        }

        // [ВЫДАЧА ПАЛКИ]
        ItemStack blazeRod = new ItemStack(Material.BLAZE_ROD);
        org.bukkit.inventory.meta.ItemMeta meta = blazeRod.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.RED + "🔥 Палка");
            meta.setLore(java.util.Arrays.asList(
                ChatColor.GRAY + "Откидывание 255 уровня",
                ChatColor.RED + "ПКМ по мобу — Откинуть"
            ));
            blazeRod.setItemMeta(meta);
        }
        player.getInventory().addItem(blazeRod);

        // [ОПЛАТА]
        plugin.getEconomyManager().withdraw(player.getUniqueId(), price);

        player.sendMessage(ChatColor.GREEN + "✓ Куплен огненный стержень за " + price + " монет!");
        player.sendMessage(ChatColor.RED + "🔥 Эта палка откидывает на 255 уровне!");
    }
}
