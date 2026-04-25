package com.example.smartphone.commands;

import com.example.smartphone.SmartPhonePlugin;
import com.example.smartphone.data.PlayerDataManager;
import com.example.smartphone.data.PlayerDataManager.Home;
import com.example.smartphone.gui.HomeMenu;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class HomeCommand implements CommandExecutor {

    private final SmartPhonePlugin plugin;

    public HomeCommand(SmartPhonePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Только игроки могут использовать эту команду!");
            return true;
        }

        Player player = (Player) sender;
        PlayerDataManager.PlayerData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());

        if (command.getName().equals("sethome")) {
            // [УСТАНОВКА ДОМА]
            if (args.length < 1) {
                player.sendMessage(ChatColor.RED + "Использование: /sethome <название>");
                return true;
            }

            // [ПРОВЕРКА ЛИМИТА] Максимум 3 дома
            int maxHomes = plugin.getConfig().getInt("homes.maxHomes", 3);
            if (data.homes.size() >= maxHomes) {
                player.sendMessage(ChatColor.RED + "Лимит домов! Максимум: " + maxHomes + " (у вас: " + data.homes.size() + ")");
                player.sendMessage(ChatColor.YELLOW + "Удалите дом: /delhome <название>");
                return true;
            }

            String homeName = args[0].toLowerCase();
            Location loc = player.getLocation();

            Home home = new Home(loc.getWorld().getName(), loc.getX(), loc.getY(), loc.getZ());
            data.addHome(homeName, home);
            plugin.getPlayerDataManager().savePlayer(data);

            player.sendMessage(ChatColor.GREEN + "✓ Дом '" + homeName + "' установлен! (" + data.homes.size() + "/" + maxHomes + ")");
            
        } else if (command.getName().equals("delhome")) {
            // [УДАЛЕНИЕ ДОМА]
            if (args.length < 1) {
                player.sendMessage(ChatColor.RED + "Использование: /delhome <название>");
                return true;
            }
            
            String homeName = args[0].toLowerCase();
            if (!data.homes.containsKey(homeName)) {
                player.sendMessage(ChatColor.RED + "Дом '" + homeName + "' не найден!");
                return true;
            }
            
            data.removeHome(homeName);
            plugin.getPlayerDataManager().savePlayer(data);
            
            player.sendMessage(ChatColor.YELLOW + "Дом '" + homeName + "' удалён!");
            
        } else if (command.getName().equals("home")) {
            // [ОТКРЫТИЕ GUI]
            if (!data.hasHome()) {
                player.sendMessage(ChatColor.RED + "У вас нет домов! Используйте /sethome <название>");
                return true;
            }

            if (data.isOnCooldown()) {
                long remaining = data.getRemainingCooldown();
                player.sendMessage(ChatColor.RED + "Перезарядка! Осталось: " + remaining + " сек.");
                return true;
            }
            
            // [ОТКРЫТИЕ МЕНЮ]
            HomeMenu menu = new HomeMenu(plugin);
            menu.open(player);
        }

        return true;
    }
}
