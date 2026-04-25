package com.example.smartphone.gui;

import com.example.smartphone.SmartPhonePlugin;
import com.example.smartphone.data.PlayerDataManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

/**
 * Меню перков: урон, выносливость, bhop, двойной прыжок
 */
public class PerksMenu implements InventoryHolder {

    private final SmartPhonePlugin plugin;
    private static final int MENU_SIZE = 54; // Увеличено до 54 слотов
    private static final String MENU_TITLE = ChatColor.DARK_PURPLE + "Перки";

    public PerksMenu(SmartPhonePlugin plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        Inventory inventory = Bukkit.createInventory(this, MENU_SIZE, MENU_TITLE);
        fillMenu(inventory, player);
        player.openInventory(inventory);
    }

    private void fillMenu(Inventory inventory, Player player) {
        // Получаем уровни перков игрока
        var data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());

        // [РЯД 1: Боевые перки]
        inventory.setItem(0, createPerkItem(
            Material.DIAMOND_SWORD,
            ChatColor.RED + "⚔️ Урон",
            data.damageLevel,
            getMaxDamageLevel(),
            getDamageCost(data.damageLevel),
            "Увеличивает урон по мобам",
            data.damageEnabled ? "ВКЛ" : "ВЫКЛ"
        ));

        inventory.setItem(1, createPerkItem(
            Material.LEATHER_CHESTPLATE,
            ChatColor.GREEN + "🛡️ Выносливость",
            data.staminaLevel,
            getMaxStaminaLevel(),
            getStaminaCost(data.staminaLevel),
            "Увеличивает максимальное здоровье",
            ""
        ));

        inventory.setItem(2, createPerkItem(
            Material.FEATHER,
            ChatColor.AQUA + "🦅 Bhop",
            data.bhopLevel,
            getMaxBhopLevel(),
            getBhopCost(data.bhopLevel),
            "Скорость и прыжок в воздухе",
            ""
        ));

        inventory.setItem(3, createPerkItem(
            Material.BLAZE_POWDER,
            ChatColor.GOLD + "🦘 Высокий прыжок",
            data.doubleJumpLevel,
            getMaxDoubleJumpLevel(),
            getDoubleJumpCost(data.doubleJumpLevel),
            "Позволяет прыгнуть выше",
            data.doubleJumpEnabled ? "ВКЛ" : "ВЫКЛ"
        ));

        // [РЯД 2: Новые перки]
        inventory.setItem(9, createPerkItem(
            Material.TOTEM_OF_UNDYING,
            ChatColor.RED + "🔥 Феникс",
            data.phoenixLevel,
            getMaxPhoenixLevel(),
            getPhoenixCost(data.phoenixLevel),
            "Воскрешение после смерти",
            getPhoenixStatus(data)
        ));

        inventory.setItem(10, createPerkItem(
            Material.SHIELD,
            ChatColor.BLUE + "🛡️ Щит",
            data.shieldLevel,
            getMaxShieldLevel(),
            getShieldCost(data.shieldLevel),
            "Шанс блокировать урон",
            data.shieldEnabled ? "ВКЛ" : "ВЫКЛ"
        ));

        inventory.setItem(11, createPerkItem(
            Material.GOLDEN_APPLE,
            ChatColor.GREEN + "💚 Регенерация",
            data.regenLevel,
            getMaxRegenLevel(),
            getRegenCost(data.regenLevel),
            "Пассивное восстановление HP (2/4/6)",
            data.regenEnabled ? "ВКЛ" : "ВЫКЛ"
        ));

        inventory.setItem(12, createPerkItem(
            Material.FIREWORK_ROCKET,
            ChatColor.YELLOW + "⚡ Рывок",
            data.dashLevel,
            getMaxDashLevel(),
            getDashCost(data.dashLevel),
            "Мгновенный рывок вперёд (ПКМ)",
            data.dashEnabled ? "ВКЛ" : "ВЫКЛ"
        ));

        inventory.setItem(13, createPerkItem(
            Material.RED_DYE,
            ChatColor.DARK_RED + "🩸 Вампиризм",
            data.vampireLevel,
            getMaxVampireLevel(),
            getVampireCost(data.vampireLevel),
            "Шанс восстановить HP при ударе",
            data.vampireEnabled ? "ВКЛ" : "ВЫКЛ"
        ));

        inventory.setItem(14, createPerkItem(
            Material.BLAZE_ROD,
            ChatColor.GOLD + "🔥 Огненный удар",
            data.fireLevel,
            getMaxFireLevel(),
            getFireCost(data.fireLevel),
            "Шанс поджечь цель при ударе",
            data.fireEnabled ? "ВКЛ" : "ВЫКЛ"
        ));

        // [РЯД 3: Информация]
        inventory.setItem(18, createInfoItem(
            Material.EXPERIENCE_BOTTLE,
            ChatColor.WHITE + "Доступно очков",
            ChatColor.GOLD + String.valueOf(data.perkPoints)
        ));

        // [РЯД 4: Заполнители]
        for (int i = 27; i < 54; i++) {
            inventory.setItem(i, createGlassPane());
        }
    }

    private ItemStack createPerkItem(Material material, String name, int currentLevel, 
                                     int maxLevel, int cost, String description, String status) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            String levelStr = currentLevel >= maxLevel ? 
                (ChatColor.RED + String.valueOf(currentLevel)) : 
                (ChatColor.GREEN + String.valueOf(currentLevel));
            String statusStr = status.equals("ВКЛ") ? 
                (ChatColor.GREEN + status) : 
                (ChatColor.RED + status);
            meta.setLore(Arrays.asList(
                ChatColor.GRAY + description,
                "",
                ChatColor.AQUA + "Уровень: " + levelStr + ChatColor.GRAY + "/" + maxLevel,
                statusStr + ChatColor.GRAY + " (ПКМ для переключения)",
                ChatColor.GOLD + "Стоимость: " + cost + " очков",
                "",
                currentLevel >= maxLevel ? 
                    ChatColor.RED + "Максимальный уровень!" :
                    ChatColor.GREEN + "ЛКМ для улучшения"
            ));
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createInfoItem(Material material, String name, String value) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(Arrays.asList(ChatColor.GRAY + value));
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

    // === БАЛАНС ПЕРКОВ ===
    
    public int getMaxDamageLevel() {
        return plugin.getConfig().getInt("perks.damage.maxLevel", 3);
    }

    public int getMaxStaminaLevel() {
        return plugin.getConfig().getInt("perks.stamina.maxLevel", 10);
    }

    public int getMaxBhopLevel() {
        return plugin.getConfig().getInt("perks.bhop.maxLevel", 5);
    }

    public int getMaxDoubleJumpLevel() {
        return plugin.getConfig().getInt("perks.doubleJump.maxLevel", 3);
    }

    public int getDamageCost(int currentLevel) {
        return (currentLevel + 1) * 2;
    }

    public int getStaminaCost(int currentLevel) {
        return (currentLevel + 1) * 2;
    }

    public int getBhopCost(int currentLevel) {
        return (currentLevel + 1) * 3;
    }

    public int getDoubleJumpCost(int currentLevel) {
        return (currentLevel + 1) * 5;
    }
    
    // === НОВЫЕ ПЕРКИ ===
    
    private String getPhoenixStatus(PlayerDataManager.PlayerData data) {
        if (data.phoenixLevel <= 0) return "Не изучено";
        
        // Проверяем перезарядку зарядов (1 заряд каждые 5 минут)
        long currentTime = System.currentTimeMillis();
        long timeSinceLastCharge = currentTime - data.phoenixLastChargeTime;
        long rechargeTime = 5 * 60 * 1000; // 5 минут
        
        if (data.phoenixCharges < data.phoenixLevel && timeSinceLastCharge < rechargeTime) {
            long remaining = (rechargeTime - timeSinceLastCharge) / 1000;
            long minutes = remaining / 60;
            long seconds = remaining % 60;
            return "Перезарядка: " + minutes + ":" + (seconds < 10 ? "0" : "") + seconds;
        }
        
        return (data.phoenixEnabled ? "ВКЛ" : "ВЫКЛ") + " | Заряды: " + data.phoenixCharges + "/" + data.phoenixLevel;
    }
    
    public int getMaxPhoenixLevel() {
        return plugin.getConfig().getInt("perks.phoenix.maxLevel", 3);
    }
    public int getPhoenixCost(int currentLevel) {
        return (currentLevel + 1) * 10;
    }
    
    public int getMaxShieldLevel() {
        return plugin.getConfig().getInt("perks.shield.maxLevel", 3);
    }
    public int getShieldCost(int currentLevel) {
        return (currentLevel + 1) * 5;
    }
    
    public int getMaxRegenLevel() {
        return plugin.getConfig().getInt("perks.regen.maxLevel", 3);
    }
    public int getRegenCost(int currentLevel) {
        return (currentLevel + 1) * 4;
    }
    
    public int getMaxDashLevel() {
        return plugin.getConfig().getInt("perks.dash.maxLevel", 3);
    }
    public int getDashCost(int currentLevel) {
        return (currentLevel + 1) * 6;
    }
    
    public int getMaxVampireLevel() {
        return plugin.getConfig().getInt("perks.vampire.maxLevel", 3);
    }
    public int getVampireCost(int currentLevel) {
        return (currentLevel + 1) * 5;
    }
    
    public int getMaxFireLevel() {
        return plugin.getConfig().getInt("perks.fire.maxLevel", 3);
    }
    public int getFireCost(int currentLevel) {
        return (currentLevel + 1) * 5;
    }

    @Override
    public Inventory getInventory() {
        return null;
    }
}
