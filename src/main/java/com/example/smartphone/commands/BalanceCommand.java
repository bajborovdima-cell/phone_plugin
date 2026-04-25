package com.example.smartphone.commands;

import com.example.smartphone.SmartPhonePlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

public class BalanceCommand implements CommandExecutor {

    private final SmartPhonePlugin plugin;

    public BalanceCommand(SmartPhonePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equals("balance")) {
            // [БАЛАНС]
            if (!(sender instanceof Player)) {
                sender.sendMessage("Только игроки могут использовать эту команду!");
                return true;
            }

            Player player = (Player) sender;
            double balance = plugin.getEconomyManager().getBalance(player.getUniqueId());
            
            player.sendMessage(ChatColor.GOLD + "=== Ваш баланс ===");
            player.sendMessage(ChatColor.YELLOW + "У вас есть: " + ChatColor.GREEN + balance + " монет");
            
        } else if (command.getName().equals("top")) {
            // [ТОП]
            Map<UUID, Double> balances = new HashMap<>();
            
            for (OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {
                double balance = plugin.getEconomyManager().getBalance(offlinePlayer.getUniqueId());
                if (balance > 0) {
                    balances.put(offlinePlayer.getUniqueId(), balance);
                }
            }
            
            // [СОРТИРОВКА]
            List<Map.Entry<UUID, Double>> sorted = new ArrayList<>(balances.entrySet());
            sorted.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));
            
            sender.sendMessage(ChatColor.GOLD + "=== Топ богатых игроков ===");
            int limit = Math.min(10, sorted.size());
            
            for (int i = 0; i < limit; i++) {
                Map.Entry<UUID, Double> entry = sorted.get(i);
                OfflinePlayer player = Bukkit.getOfflinePlayer(entry.getKey());
                String position = ChatColor.GREEN + "#" + (i + 1);
                String name = player.getName() != null ? player.getName() : "Неизвестно";
                String balance = ChatColor.YELLOW + "" + entry.getValue() + " монет";
                
                sender.sendMessage(position + " " + name + " - " + balance);
            }
        }

        return true;
    }
}
