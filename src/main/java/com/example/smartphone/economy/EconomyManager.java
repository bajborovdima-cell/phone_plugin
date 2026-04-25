package com.example.smartphone.economy;

import com.example.smartphone.SmartPhonePlugin;
import com.example.smartphone.data.PlayerDataManager;
import com.example.smartphone.data.PlayerDataManager.PlayerData;

import java.util.UUID;

public class EconomyManager {

    private final SmartPhonePlugin plugin;
    private final PlayerDataManager playerDataManager;

    public EconomyManager(SmartPhonePlugin plugin) {
        this.plugin = plugin;
        this.playerDataManager = plugin.getPlayerDataManager();
    }

    public double getBalance(UUID uuid) {
        PlayerData data = playerDataManager.getPlayerData(uuid);
        return data.balance;
    }

    public boolean hasBalance(UUID uuid, double amount) {
        return getBalance(uuid) >= amount;
    }

    public boolean deposit(UUID uuid, double amount) {
        PlayerData data = playerDataManager.getPlayerData(uuid);
        data.balance += amount;
        plugin.getPlayerDataManager().savePlayer(data);
        return true;
    }

    public boolean withdraw(UUID uuid, double amount) {
        PlayerData data = playerDataManager.getPlayerData(uuid);
        if (data.balance < amount) {
            return false;
        }
        data.balance -= amount;
        plugin.getPlayerDataManager().savePlayer(data);
        return true;
    }

    public boolean transfer(UUID from, UUID to, double amount) {
        if (!withdraw(from, amount)) {
            return false;
        }
        deposit(to, amount);
        return true;
    }

    public String format(double amount) {
        return String.valueOf((int) amount) + " " + plugin.getConfig().getString("economy.currency_name", "монет");
    }
}
