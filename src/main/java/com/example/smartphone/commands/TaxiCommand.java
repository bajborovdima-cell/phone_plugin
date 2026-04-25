package com.example.smartphone.commands;

import com.example.smartphone.SmartPhonePlugin;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TaxiCommand implements CommandExecutor {

    private final SmartPhonePlugin plugin;

    public TaxiCommand(SmartPhonePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Только игроки могут использовать эту команду!");
            return true;
        }

        Player player = (Player) sender;
        double cost = plugin.getConfig().getDouble("taxi.cost", 50);

        if (args.length == 0) {
            // [ВЫЗВАТЬ ТАКСИ]
            if (!plugin.getEconomyManager().hasBalance(player.getUniqueId(), cost)) {
                player.sendMessage(ChatColor.RED + "Недостаточно денег! Нужно: " + cost + " монет");
                return true;
            }

            plugin.getEconomyManager().withdraw(player.getUniqueId(), cost);
            
            // [ЭФФЕКТЫ]
            Location loc = player.getLocation();
            loc.getWorld().playSound(loc, org.bukkit.Sound.ENTITY_HORSE_LAND, 1.0f, 1.0f);
            loc.getWorld().spawnParticle(org.bukkit.Particle.CRIT, loc, 20, 1, 0, 1, 0);
            
            player.sendMessage(ChatColor.GREEN + "✓ Такси вызвано! Стоимость: " + cost + " монет");
            player.sendMessage(ChatColor.YELLOW + "Куда едем? /taxi home | /taxi spawn");
            
        } else if (args[0].equalsIgnoreCase("home")) {
            // [ДОМОЙ]
            var data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
            if (!data.hasHome()) {
                player.sendMessage(ChatColor.RED + "У вас нет дома!");
                return true;
            }
            
            // Берём первый дом
            String firstHomeName = data.getHomeNames().stream().findFirst().orElse(null);
            if (firstHomeName == null) {
                player.sendMessage(ChatColor.RED + "У вас нет дома!");
                return true;
            }
            
            var home = data.getHome(firstHomeName);
            
            if (!plugin.getEconomyManager().withdraw(player.getUniqueId(), cost)) {
                player.sendMessage(ChatColor.RED + "Недостаточно денег!");
                return true;
            }
            
            org.bukkit.Location homeLoc = new org.bukkit.Location(
                player.getServer().getWorld(home.world),
                home.x, home.y, home.z
            );
            player.teleport(homeLoc);
            player.sendMessage(ChatColor.GREEN + "✓ Такси отвезло вас домой!");
            
        } else if (args[0].equalsIgnoreCase("spawn")) {
            // [НА СПАВН]
            if (!plugin.getEconomyManager().withdraw(player.getUniqueId(), cost)) {
                player.sendMessage(ChatColor.RED + "Недостаточно денег!");
                return true;
            }
            
            Location spawn = player.getServer().getWorlds().get(0).getSpawnLocation();
            player.teleport(spawn);
            player.sendMessage(ChatColor.GREEN + "✓ Такси отвезло вас на спавн!");
            
        } else if (args[0].equalsIgnoreCase("cancel")) {
            // [ОТМЕНА]
            player.sendMessage(ChatColor.YELLOW + "Поездка отменена");
        }

        return true;
    }
}
