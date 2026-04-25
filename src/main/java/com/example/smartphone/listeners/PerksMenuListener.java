package com.example.smartphone.listeners;

import com.example.smartphone.SmartPhonePlugin;
import com.example.smartphone.data.PlayerDataManager;
import com.example.smartphone.gui.PerksMenu;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * Слушатель меню перков
 */
public class PerksMenuListener implements Listener {

    private final SmartPhonePlugin plugin;

    public PerksMenuListener(SmartPhonePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        Inventory inventory = event.getInventory();

        // [ПРОВЕРКА МЕНЮ ПЕРКОВ]
        if (!(inventory.getHolder() instanceof PerksMenu)) {
            return;
        }

        event.setCancelled(true);

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) {
            return;
        }

        if (!clickedItem.hasItemMeta() || !clickedItem.getItemMeta().hasDisplayName()) {
            return;
        }

        String displayName = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName());
        PlayerDataManager.PlayerData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
        PerksMenu perksMenu = new PerksMenu(plugin);

        // [ОБРАБОТКА КЛИКОВ ПО ПЕРКАМ]
        switch (event.getSlot()) {
            case 0: // Урон
                if (event.isLeftClick()) {
                    upgradeDamage(player, data, perksMenu);
                } else if (event.isRightClick()) {
                    toggleDamage(player, data, perksMenu);
                }
                break;

            case 1: // Выносливость
                if (event.isLeftClick()) {
                    upgradeStamina(player, data, perksMenu);
                }
                break;

            case 2: // Bhop
                if (event.isLeftClick()) {
                    upgradeBhop(player, data, perksMenu);
                }
                break;

            case 3: // Высокий прыжок
                if (event.isLeftClick()) {
                    upgradeDoubleJump(player, data, perksMenu);
                } else if (event.isRightClick()) {
                    toggleDoubleJump(player, data, perksMenu);
                }
                break;

            // [НОВЫЕ ПЕРКИ]
            case 9: // Феникс
                if (event.isLeftClick()) {
                    upgradePhoenix(player, data, perksMenu);
                } else if (event.isRightClick()) {
                    togglePhoenix(player, data, perksMenu);
                }
                break;

            case 10: // Щит
                if (event.isLeftClick()) {
                    upgradeShield(player, data, perksMenu);
                } else if (event.isRightClick()) {
                    toggleShield(player, data, perksMenu);
                }
                break;

            case 11: // Регенерация
                if (event.isLeftClick()) {
                    upgradeRegen(player, data, perksMenu);
                } else if (event.isRightClick()) {
                    toggleRegen(player, data, perksMenu);
                }
                break;

            case 12: // Рывок
                if (event.isLeftClick()) {
                    upgradeDash(player, data, perksMenu);
                } else if (event.isRightClick()) {
                    toggleDash(player, data, perksMenu);
                }
                break;

            case 13: // Вампиризм
                if (event.isLeftClick()) {
                    upgradeVampire(player, data, perksMenu);
                } else if (event.isRightClick()) {
                    toggleVampire(player, data, perksMenu);
                }
                break;

            case 14: // Огненный удар
                if (event.isLeftClick()) {
                    upgradeFire(player, data, perksMenu);
                } else if (event.isRightClick()) {
                    toggleFire(player, data, perksMenu);
                }
                break;
        }
    }

    private void upgradeDamage(Player player, PlayerDataManager.PlayerData data, PerksMenu perksMenu) {
        int maxLevel = perksMenu.getMaxDamageLevel();
        if (data.damageLevel >= maxLevel) {
            player.sendMessage(ChatColor.RED + "Максимальный уровень!");
            return;
        }

        int cost = perksMenu.getDamageCost(data.damageLevel);
        if (data.perkPoints < cost) {
            player.sendMessage(ChatColor.RED + "Недостаточно очков перков! Нужно: " + cost);
            return;
        }

        data.perkPoints -= cost;
        data.damageLevel++;
        plugin.getPlayerDataManager().savePlayer(data);

        // [ЭФФЕКТ СИЛА] Даём только если перк включён
        if (data.damageEnabled) {
            int strengthLevel = data.damageLevel - 1; // 0, 1, 2
            player.removePotionEffect(org.bukkit.potion.PotionEffectType.STRENGTH);
            player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                org.bukkit.potion.PotionEffectType.STRENGTH,
                Integer.MAX_VALUE, // Бесконечно
                strengthLevel, // Сила I/II/III
                false, // Без частиц
                false // Скрыть иконку
            ));
            player.sendMessage(ChatColor.RED + "⚔️ Эффект: Сила " + getRomanNumeral(data.damageLevel));
        }

        player.sendMessage(ChatColor.GREEN + "✓ Урон улучшен до уровня " + data.damageLevel);
        perksMenu.open(player);
    }
    
    private String getRomanNumeral(int level) {
        switch (level) {
            case 1: return "I";
            case 2: return "II";
            case 3: return "III";
            default: return String.valueOf(level);
        }
    }

    private void upgradeStamina(Player player, PlayerDataManager.PlayerData data, PerksMenu perksMenu) {
        int maxLevel = perksMenu.getMaxStaminaLevel();
        if (data.staminaLevel >= maxLevel) {
            player.sendMessage(ChatColor.RED + "Максимальный уровень!");
            return;
        }

        int cost = perksMenu.getStaminaCost(data.staminaLevel);
        if (data.perkPoints < cost) {
            player.sendMessage(ChatColor.RED + "Недостаточно очков перков! Нужно: " + cost);
            return;
        }

        data.perkPoints -= cost;
        data.staminaLevel++;
        plugin.getPlayerDataManager().savePlayer(data);
        
        // [ДОБАВЛЕНИЕ СЕРДЕЦ] +2 HP за каждый уровень
        double maxHealth = 20.0 + (data.staminaLevel * 2.0); // Базовое 20 HP + 2 за уровень
        if (maxHealth > 40.0) maxHealth = 40.0; // Максимум 20 сердец
        player.setMaxHealth(maxHealth);
        player.setHealth(player.getMaxHealth()); // Полное лечение
        
        player.sendMessage(ChatColor.GREEN + "✓ Выносливость улучшена до уровня " + data.staminaLevel);
        player.sendMessage(ChatColor.RED + "❤ Максимальное здоровье: " + (int)(maxHealth / 2) + " сердец");
        perksMenu.open(player);
    }

    private void upgradeBhop(Player player, PlayerDataManager.PlayerData data, PerksMenu perksMenu) {
        int maxLevel = perksMenu.getMaxBhopLevel();
        if (data.bhopLevel >= maxLevel) {
            player.sendMessage(ChatColor.RED + "Максимальный уровень!");
            return;
        }

        int cost = perksMenu.getBhopCost(data.bhopLevel);
        if (data.perkPoints < cost) {
            player.sendMessage(ChatColor.RED + "Недостаточно очков перков! Нужно: " + cost);
            return;
        }

        data.perkPoints -= cost;
        data.bhopLevel++;
        plugin.getPlayerDataManager().savePlayer(data);
        player.sendMessage(ChatColor.GREEN + "✓ Bhop улучшен до уровня " + data.bhopLevel);
        perksMenu.open(player);
    }

    private void upgradeDoubleJump(Player player, PlayerDataManager.PlayerData data, PerksMenu perksMenu) {
        int maxLevel = perksMenu.getMaxDoubleJumpLevel();
        if (data.doubleJumpLevel >= maxLevel) {
            player.sendMessage(ChatColor.RED + "Максимальный уровень!");
            return;
        }

        int cost = perksMenu.getDoubleJumpCost(data.doubleJumpLevel);
        if (data.perkPoints < cost) {
            player.sendMessage(ChatColor.RED + "Недостаточно очков перков! Нужно: " + cost);
            return;
        }

        data.perkPoints -= cost;
        data.doubleJumpLevel++;
        plugin.getPlayerDataManager().savePlayer(data);
        
        player.sendMessage(ChatColor.GREEN + "✓ Высокий прыжок улучшен до уровня " + data.doubleJumpLevel);
        perksMenu.open(player);
    }
    
    private void toggleDoubleJump(Player player, PlayerDataManager.PlayerData data, PerksMenu perksMenu) {
        data.doubleJumpEnabled = !data.doubleJumpEnabled;
        plugin.getPlayerDataManager().savePlayer(data);

        String status = data.doubleJumpEnabled ? "ВКЛ" : "ВЫКЛ";
        player.sendMessage(ChatColor.GREEN + "✓ Высокий прыжок: " + status);
        perksMenu.open(player);
    }
    
    // === НОВЫЕ ПЕРКИ ===
    
    private void upgradePhoenix(Player player, PlayerDataManager.PlayerData data, PerksMenu perksMenu) {
        int maxLevel = perksMenu.getMaxPhoenixLevel();
        if (data.phoenixLevel >= maxLevel) {
            player.sendMessage(ChatColor.RED + "Максимальный уровень!");
            return;
        }
        int cost = perksMenu.getPhoenixCost(data.phoenixLevel);
        if (data.perkPoints < cost) {
            player.sendMessage(ChatColor.RED + "Недостаточно очков перков! Нужно: " + cost);
            return;
        }
        data.perkPoints -= cost;
        data.phoenixLevel++;
        data.phoenixCharges = data.phoenixLevel; // Даем заряды
        plugin.getPlayerDataManager().savePlayer(data);
        player.sendMessage(ChatColor.GREEN + "✓ Феникс улучшен до уровня " + data.phoenixLevel);
        player.sendMessage(ChatColor.RED + "🔥 Заряды: " + data.phoenixCharges);
        perksMenu.open(player);
    }
    
    private void upgradeShield(Player player, PlayerDataManager.PlayerData data, PerksMenu perksMenu) {
        int maxLevel = perksMenu.getMaxShieldLevel();
        if (data.shieldLevel >= maxLevel) {
            player.sendMessage(ChatColor.RED + "Максимальный уровень!");
            return;
        }
        int cost = perksMenu.getShieldCost(data.shieldLevel);
        if (data.perkPoints < cost) {
            player.sendMessage(ChatColor.RED + "Недостаточно очков перков! Нужно: " + cost);
            return;
        }
        data.perkPoints -= cost;
        data.shieldLevel++;
        plugin.getPlayerDataManager().savePlayer(data);
        player.sendMessage(ChatColor.GREEN + "✓ Щит улучшен до уровня " + data.shieldLevel);
        player.sendMessage(ChatColor.BLUE + "🛡️ Шанс блока: " + (data.shieldLevel * 5) + "%");
        perksMenu.open(player);
    }
    
    private void upgradeRegen(Player player, PlayerDataManager.PlayerData data, PerksMenu perksMenu) {
        int maxLevel = perksMenu.getMaxRegenLevel();
        if (data.regenLevel >= maxLevel) {
            player.sendMessage(ChatColor.RED + "Максимальный уровень!");
            return;
        }
        int cost = perksMenu.getRegenCost(data.regenLevel);
        if (data.perkPoints < cost) {
            player.sendMessage(ChatColor.RED + "Недостаточно очков перков! Нужно: " + cost);
            return;
        }
        data.perkPoints -= cost;
        data.regenLevel++;
        plugin.getPlayerDataManager().savePlayer(data);
        player.sendMessage(ChatColor.GREEN + "✓ Регенерация улучшена до уровня " + data.regenLevel);
        player.sendMessage(ChatColor.GREEN + "💚 Восстановление: " + (data.regenLevel * 2) + " HP каждые " + (5 - data.regenLevel) + " сек");
        perksMenu.open(player);
    }
    
    private void upgradeDash(Player player, PlayerDataManager.PlayerData data, PerksMenu perksMenu) {
        int maxLevel = perksMenu.getMaxDashLevel();
        if (data.dashLevel >= maxLevel) {
            player.sendMessage(ChatColor.RED + "Максимальный уровень!");
            return;
        }
        int cost = perksMenu.getDashCost(data.dashLevel);
        if (data.perkPoints < cost) {
            player.sendMessage(ChatColor.RED + "Недостаточно очков перков! Нужно: " + cost);
            return;
        }
        data.perkPoints -= cost;
        data.dashLevel++;
        plugin.getPlayerDataManager().savePlayer(data);
        player.sendMessage(ChatColor.GREEN + "✓ Рывок улучшен до уровня " + data.dashLevel);
        player.sendMessage(ChatColor.YELLOW + "⚡ Дальность: " + (data.dashLevel * 5) + " блоков");
        perksMenu.open(player);
    }
    
    private void upgradeVampire(Player player, PlayerDataManager.PlayerData data, PerksMenu perksMenu) {
        int maxLevel = perksMenu.getMaxVampireLevel();
        if (data.vampireLevel >= maxLevel) {
            player.sendMessage(ChatColor.RED + "Максимальный уровень!");
            return;
        }
        int cost = perksMenu.getVampireCost(data.vampireLevel);
        if (data.perkPoints < cost) {
            player.sendMessage(ChatColor.RED + "Недостаточно очков перков! Нужно: " + cost);
            return;
        }
        data.perkPoints -= cost;
        data.vampireLevel++;
        plugin.getPlayerDataManager().savePlayer(data);
        player.sendMessage(ChatColor.GREEN + "✓ Вампиризм улучшен до уровня " + data.vampireLevel);
        player.sendMessage(ChatColor.DARK_RED + "🩸 Шанс: " + (data.vampireLevel * 10) + "%");
        perksMenu.open(player);
    }
    
    private void upgradeFire(Player player, PlayerDataManager.PlayerData data, PerksMenu perksMenu) {
        int maxLevel = perksMenu.getMaxFireLevel();
        if (data.fireLevel >= maxLevel) {
            player.sendMessage(ChatColor.RED + "Максимальный уровень!");
            return;
        }
        int cost = perksMenu.getFireCost(data.fireLevel);
        if (data.perkPoints < cost) {
            player.sendMessage(ChatColor.RED + "Недостаточно очков перков! Нужно: " + cost);
            return;
        }
        data.perkPoints -= cost;
        data.fireLevel++;
        plugin.getPlayerDataManager().savePlayer(data);
        player.sendMessage(ChatColor.GREEN + "✓ Огненный удар улучшен до уровня " + data.fireLevel);
        player.sendMessage(ChatColor.GOLD + "🔥 Шанс поджога: " + (data.fireLevel * 25) + "%");
        perksMenu.open(player);
    }
    
    // === ПЕРЕКЛЮЧАТЕЛИ ПЕРКОВ ===
    
    private void togglePhoenix(Player player, PlayerDataManager.PlayerData data, PerksMenu perksMenu) {
        data.phoenixEnabled = !data.phoenixEnabled;
        plugin.getPlayerDataManager().savePlayer(data);
        String status = data.phoenixEnabled ? "ВКЛ" : "ВЫКЛ";
        player.sendMessage(ChatColor.GREEN + "✓ Феникс: " + status);
        perksMenu.open(player);
    }
    
    private void toggleShield(Player player, PlayerDataManager.PlayerData data, PerksMenu perksMenu) {
        data.shieldEnabled = !data.shieldEnabled;
        plugin.getPlayerDataManager().savePlayer(data);
        String status = data.shieldEnabled ? "ВКЛ" : "ВЫКЛ";
        player.sendMessage(ChatColor.GREEN + "✓ Щит: " + status);
        perksMenu.open(player);
    }
    
    private void toggleRegen(Player player, PlayerDataManager.PlayerData data, PerksMenu perksMenu) {
        data.regenEnabled = !data.regenEnabled;
        plugin.getPlayerDataManager().savePlayer(data);
        String status = data.regenEnabled ? "ВКЛ" : "ВЫКЛ";
        player.sendMessage(ChatColor.GREEN + "✓ Регенерация: " + status);
        perksMenu.open(player);
    }
    
    private void toggleDash(Player player, PlayerDataManager.PlayerData data, PerksMenu perksMenu) {
        data.dashEnabled = !data.dashEnabled;
        plugin.getPlayerDataManager().savePlayer(data);
        String status = data.dashEnabled ? "ВКЛ" : "ВЫКЛ";
        player.sendMessage(ChatColor.GREEN + "✓ Рывок: " + status);
        perksMenu.open(player);
    }
    
    private void toggleVampire(Player player, PlayerDataManager.PlayerData data, PerksMenu perksMenu) {
        data.vampireEnabled = !data.vampireEnabled;
        plugin.getPlayerDataManager().savePlayer(data);
        String status = data.vampireEnabled ? "ВКЛ" : "ВЫКЛ";
        player.sendMessage(ChatColor.GREEN + "✓ Вампиризм: " + status);
        perksMenu.open(player);
    }
    
    private void toggleFire(Player player, PlayerDataManager.PlayerData data, PerksMenu perksMenu) {
        data.fireEnabled = !data.fireEnabled;
        plugin.getPlayerDataManager().savePlayer(data);
        String status = data.fireEnabled ? "ВКЛ" : "ВЫКЛ";
        player.sendMessage(ChatColor.GREEN + "✓ Огненный удар: " + status);
        perksMenu.open(player);
    }
    
    private void toggleDamage(Player player, PlayerDataManager.PlayerData data, PerksMenu perksMenu) {
        data.damageEnabled = !data.damageEnabled;
        plugin.getPlayerDataManager().savePlayer(data);
        
        // Снимаем эффект силы если перк отключён
        if (!data.damageEnabled) {
            player.removePotionEffect(org.bukkit.potion.PotionEffectType.STRENGTH);
            player.sendMessage(ChatColor.GREEN + "✓ Урон: ВЫКЛ (эффект снят)");
        } else {
            // Даём эффект силы если перк включён и есть уровень
            if (data.damageLevel > 0) {
                int strengthLevel = data.damageLevel - 1;
                player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                    org.bukkit.potion.PotionEffectType.STRENGTH,
                    Integer.MAX_VALUE,
                    strengthLevel,
                    false,
                    false
                ));
                player.sendMessage(ChatColor.GREEN + "✓ Урон: ВКЛ (Сила " + getRomanNumeral(data.damageLevel) + ")");
            } else {
                player.sendMessage(ChatColor.GREEN + "✓ Урон: ВКЛ (прокачайте для эффекта)");
            }
        }
        perksMenu.open(player);
    }
}
