package com.example.smartphone.commands;

import com.example.smartphone.SmartPhonePlugin;
import com.example.smartphone.data.PlayerDataManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Команда для выдачи очков перков
 */
public class PerkPointsCommand implements CommandExecutor {

    private final SmartPhonePlugin plugin;

    public PerkPointsCommand(SmartPhonePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("smartphone.perkpoints")) {
            sender.sendMessage(ChatColor.RED + "У вас нет прав на эту команду!");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Использование: /perkpoints <игрок> <количество>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Игрок не найден!");
            return true;
        }

        int points;
        try {
            points = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Неверное количество очков!");
            return true;
        }

        PlayerDataManager.PlayerData data = plugin.getPlayerDataManager().getPlayerData(target.getUniqueId());
        data.perkPoints += points;
        plugin.getPlayerDataManager().savePlayer(data);

        sender.sendMessage(ChatColor.GREEN + "✓ Выдано " + points + " очков перков игроку " + target.getName());
        sender.sendMessage(ChatColor.GOLD + "Текущие очки: " + data.perkPoints);
        target.sendMessage(ChatColor.GREEN + "Вы получили " + points + " очков перков!");

        return true;
    }
}
