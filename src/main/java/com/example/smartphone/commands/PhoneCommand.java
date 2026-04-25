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

public class PhoneCommand implements CommandExecutor {

    private final SmartPhonePlugin plugin;

    public PhoneCommand(SmartPhonePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Только игроки могут использовать эту команду!");
            return true;
        }

        Player player = (Player) sender;
        
        // [ВЫДАЧА ТЕЛЕФОНА]
        ItemStack phone = createPhone();
        player.getInventory().addItem(phone);
        
        player.sendMessage(ChatColor.GREEN + "✓ Вы получили телефон!");
        player.sendMessage(ChatColor.YELLOW + "ПКМ по телефону в руке для открытия меню");
        
        return true;
    }

    private ItemStack createPhone() {
        ItemStack phone = new ItemStack(Material.LIGHT_BLUE_DYE);
        ItemMeta meta = phone.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(plugin.getConfig().getString("phone.name", "§b§lТелефон"));
            phone.setItemMeta(meta);
        }

        return phone;
    }
}
