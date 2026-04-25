package com.example.smartphone.gui;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class GuardTierMenu implements InventoryHolder {

    private static final String MENU_TITLE = ChatColor.DARK_BLUE + "Уровень охраны";

    private final Player target;

    public GuardTierMenu(Player target) {
        this.target = target;
    }

    public void open(Player owner) {
        Inventory inventory = Bukkit.createInventory(this, 9, MENU_TITLE);
        fillMenu(inventory, owner);
        owner.openInventory(inventory);
    }

    private void fillMenu(Inventory inventory, Player owner) {
        // [ОБЫЧНЫЙ] Слот 0 - Каменный меч + кожаная броня
        inventory.setItem(0, createTierItem(
            Material.STONE_SWORD,
            ChatColor.WHITE + "Обычная",
            ChatColor.GRAY + "Цена: §e100 монет",
            ChatColor.GRAY + "Здоровье: §c30 HP",
            ChatColor.GRAY + "Оружие: §fКаменный меч (Острота I)",
            ChatColor.GRAY + "Броня: §fКожаная (синяя)",
            ChatColor.GRAY + "Время: §e5 минут",
            "",
            ChatColor.GREEN + "→ Клик для найма"
        ));

        // [ПРОФЕССИОНАЛ] Слот 2 - Зачарованный железный меч
        inventory.setItem(2, createTierItem(
            Material.IRON_SWORD,
            ChatColor.GOLD + "Профессионал",
            ChatColor.GRAY + "Цена: §e250 монет",
            ChatColor.GRAY + "Здоровье: §c50 HP",
            ChatColor.GRAY + "Оружие: §eЖелезный меч (Острота II, Заговор огня I)",
            ChatColor.GRAY + "Броня: §eКожаная (оранжевая)",
            ChatColor.GRAY + "Время: §e10 минут",
            "",
            ChatColor.GREEN + "→ Клик для найма"
        ));

        // [ЭЛИТА] Слот 4 - Мощный алмазный меч
        inventory.setItem(4, createTierItem(
            Material.DIAMOND_SWORD,
            ChatColor.DARK_PURPLE + "ЭЛИТА",
            ChatColor.GRAY + "Цена: §e500 монет",
            ChatColor.GRAY + "Здоровье: §c100 HP",
            ChatColor.GRAY + "Оружие: §5Алмазный меч (Острота V, Заговор огня II, Отбрасывание II)",
            ChatColor.GRAY + "Броня: §5Кожаная (фиолетовая)",
            ChatColor.GRAY + "Эффекты: §dСкорость, Сопротивление",
            ChatColor.GRAY + "Время: §e15 минут",
            "",
            ChatColor.GREEN + "→ Клик для найма"
        ));

        // [ВЕЧНАЯ] Слот 6 - Бессрочная охрана
        inventory.setItem(6, createTierItem(
            Material.NETHERITE_SWORD,
            ChatColor.YELLOW + "ВЕЧНАЯ",
            ChatColor.GRAY + "Цена: §e1000 монет",
            ChatColor.GRAY + "Здоровье: §c50 HP",
            ChatColor.GRAY + "Оружие: §6Железный меч (Острота II)",
            ChatColor.GRAY + "Броня: §6Кожаная (золотая)",
            ChatColor.GRAY + "Время: §e∞ Бессрочно",
            "",
            ChatColor.GREEN + "→ Клик для найма"
        ));

        // [ЗАПОЛНИТЕЛИ]
        for (int i : new int[]{1, 3, 5, 7, 8}) {
            inventory.setItem(i, createGlassPane());
        }
    }

    private ItemStack createTierItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(Arrays.asList(lore));
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createGlassPane() {
        ItemStack item = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(" ");
            item.setItemMeta(meta);
        }
        return item;
    }

    public Player getTarget() {
        return target;
    }

    @Override
    public Inventory getInventory() {
        return null;
    }
}
