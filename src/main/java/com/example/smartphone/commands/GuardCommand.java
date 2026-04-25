package com.example.smartphone.commands;

import com.example.smartphone.SmartPhonePlugin;
import com.example.smartphone.gui.GuardMenu;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GuardCommand implements CommandExecutor {

    private final SmartPhonePlugin plugin;

    public GuardCommand(SmartPhonePlugin plugin) {
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
            // [ОТКРЫТИЕ GUI]
            GuardMenu menu = new GuardMenu();
            menu.open(player);
            
        } else if (args[0].equalsIgnoreCase("dismiss")) {
            // [ОТПУСТИТЬ]
            plugin.getGuardManager().removeGuard(player);
            player.sendMessage(ChatColor.YELLOW + "Охранник отпущен");
        }

        return true;
    }
}
