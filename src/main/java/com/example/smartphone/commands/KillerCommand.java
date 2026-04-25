package com.example.smartphone.commands;

import com.example.smartphone.SmartPhonePlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class KillerCommand implements CommandExecutor {

    private final SmartPhonePlugin plugin;

    public KillerCommand(SmartPhonePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Только игроки могут использовать эту команду!");
            return true;
        }

        Player player = (Player) sender;
        double cost = plugin.getConfig().getDouble("killer.cost", 500);

        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + "Использование: /killer <игрок>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null || !target.isOnline()) {
            player.sendMessage(ChatColor.RED + "Игрок не найден или не онлайн!");
            return true;
        }

        if (!plugin.getEconomyManager().hasBalance(player.getUniqueId(), cost)) {
            player.sendMessage(ChatColor.RED + "Недостаточно денег! Нужно: " + cost + " монет");
            return true;
        }

        // [СПАВН КИЛЛЕРА]
        plugin.getKillerManager().spawnKiller(player, target);
        plugin.getEconomyManager().withdraw(player.getUniqueId(), cost);

        player.sendMessage(ChatColor.GREEN + "✓ Киллер нанят! У вас 2 минуты на устранение цели.");
        player.sendMessage(ChatColor.RED + "Цель: " + target.getName());

        return true;
    }
}
