package com.example.smartphone.listeners;

import com.example.smartphone.SmartPhonePlugin;
import com.example.smartphone.data.PlayerDataManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Слушатель синхронизации здоровья и эффектов при заходе на сервер
 */
public class HealthSyncListener implements Listener {

    private final SmartPhonePlugin plugin;

    public HealthSyncListener(SmartPhonePlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Установка максимального здоровья и эффектов при заходе игрока
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        PlayerDataManager.PlayerData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());

        // [ВЫНОСЛИВОСТЬ] +2 HP за каждый уровень (максимум 20 сердец)
        double maxHealth = 20.0 + (data.staminaLevel * 2.0);
        if (maxHealth > 40.0) maxHealth = 40.0; // Максимум 20 сердец
        
        player.setMaxHealth(maxHealth);
        player.setHealth(maxHealth); // Полное здоровье при заходе

        // [УРОН] Эффект Сила I/II/III при 1+ уровне и включённом перке
        if (data.damageLevel >= 1 && data.damageEnabled) {
            int strengthLevel = data.damageLevel - 1; // 0, 1, 2
            player.addPotionEffect(new PotionEffect(
                PotionEffectType.STRENGTH,
                Integer.MAX_VALUE,
                strengthLevel, // Сила I/II/III
                false,
                false
            ));
        }
    }
}
