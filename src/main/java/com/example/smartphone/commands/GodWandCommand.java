package com.example.smartphone.commands;

import com.example.smartphone.SmartPhonePlugin;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

/**
 * Команда выдачи палочки бога (для админов)
 */
public class GodWandCommand implements CommandExecutor {

    private final SmartPhonePlugin plugin;

    public GodWandCommand(SmartPhonePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Только игроки могут использовать эту команду!");
            return true;
        }

        Player player = (Player) sender;

        // [ПРОВЕРКА ПРАВ]
        if (!player.hasPermission("smartphone.godwand")) {
            player.sendMessage(ChatColor.RED + "У вас нет прав на эту команду!");
            return true;
        }

        // [ВЫДАЧА ПАЛОЧКИ]
        ItemStack godWand = createGodWand();
        player.getInventory().addItem(godWand);

        player.sendMessage(ChatColor.GREEN + "✓ Палочка бога выдана!");
        player.sendMessage(ChatColor.YELLOW + "ПКМ по игроку/мобу для откидывания (Knockback 255)");

        return true;
    }

    /**
     * Создание палочки бога
     */
    private ItemStack createGodWand() {
        ItemStack wand = new ItemStack(Material.BLAZE_ROD);
        ItemMeta meta = wand.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(ChatColor.GOLD + "⚡ Палочка Бога");
            meta.setLore(Arrays.asList(
                ChatColor.GRAY + "Инструмент божественного наказания",
                "",
                ChatColor.RED + "ПКМ — Откинуть цель (Knockback 255)",
                ChatColor.YELLOW + "Уровень: ∞",
                "",
                ChatColor.DARK_PURPLE + "Только для админов"
            ));
            meta.setUnbreakable(true);
            wand.setItemMeta(meta);
        }

        return wand;
    }
}
