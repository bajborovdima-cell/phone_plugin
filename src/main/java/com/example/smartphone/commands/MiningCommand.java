package com.example.smartphone.commands;

import com.example.smartphone.SmartPhonePlugin;
import com.example.smartphone.gui.MiningMenu;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Команда для открытия меню шахтинга
 */
public class MiningCommand implements CommandExecutor {

    private final SmartPhonePlugin plugin;

    public MiningCommand(SmartPhonePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cТолько игроки могут использовать эту команду!");
            return true;
        }

        Player player = (Player) sender;
        MiningMenu menu = new MiningMenu(plugin);
        menu.open(player);

        return true;
    }
}
