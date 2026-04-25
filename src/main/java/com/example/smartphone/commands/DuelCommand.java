package com.example.smartphone.commands;

import com.example.smartphone.SmartPhonePlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Команда дуэли /duel
 */
public class DuelCommand implements CommandExecutor {

    private final SmartPhonePlugin plugin;

    public DuelCommand(SmartPhonePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Только игроки могут использовать эту команду!");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            player.sendMessage(ChatColor.GOLD + "=== Дуэль ===");
            player.sendMessage(ChatColor.YELLOW + "/duel <игрок> — Вызвать на дуэль");
            player.sendMessage(ChatColor.YELLOW + "/duel accept — Принять вызов");
            player.sendMessage(ChatColor.YELLOW + "/duel deny — Отклонить вызов");
            player.sendMessage(ChatColor.YELLOW + "/duel leave — Покинуть дуэль");
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "accept":
                plugin.getDuelManager().accept(player);
                break;

            case "deny":
                plugin.getDuelManager().deny(player);
                break;

            case "leave":
                plugin.getDuelManager().leave(player);
                break;

            default:
                // Вызов игрока
                Player target = Bukkit.getPlayer(args[0]);
                if (target == null || !target.isOnline()) {
                    player.sendMessage(ChatColor.RED + "Игрок не найден!");
                    return true;
                }
                plugin.getDuelManager().challenge(player, target);
                break;
        }

        return true;
    }
}
