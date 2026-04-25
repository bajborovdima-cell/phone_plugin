package com.example.smartphone.commands;

import com.example.smartphone.SmartPhonePlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PayCommand implements CommandExecutor {

    private final SmartPhonePlugin plugin;

    public PayCommand(SmartPhonePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Только игроки могут использовать эту команду!");
            return true;
        }

        Player player = (Player) sender;

        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Использование: /pay <игрок> <сумма>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null || !target.isOnline()) {
            player.sendMessage(ChatColor.RED + "Игрок не найден или не онлайн!");
            return true;
        }

        double amount;
        try {
            amount = Double.parseDouble(args[1]);
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Неверная сумма!");
            return true;
        }

        if (amount <= 0) {
            player.sendMessage(ChatColor.RED + "Сумма должна быть больше 0!");
            return true;
        }

        if (!plugin.getEconomyManager().hasBalance(player.getUniqueId(), amount)) {
            player.sendMessage(ChatColor.RED + "Недостаточно денег!");
            return true;
        }

        // [ПЕРЕВОД]
        if (plugin.getEconomyManager().transfer(player.getUniqueId(), target.getUniqueId(), amount)) {
            player.sendMessage(ChatColor.GREEN + "✓ Вы перевели " + amount + " монет игроку " + target.getName());
            target.sendMessage(ChatColor.GREEN + "✓ Вы получили " + amount + " монет от " + player.getName());
        } else {
            player.sendMessage(ChatColor.RED + "Ошибка при переводе!");
        }

        return true;
    }
}
