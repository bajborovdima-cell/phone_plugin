package com.example.smartphone;

import com.example.smartphone.commands.*;
import com.example.smartphone.data.PlayerDataManager;
import com.example.smartphone.economy.EconomyManager;
import com.example.smartphone.entities.GuardManager;
import com.example.smartphone.entities.KillerManager;
import com.example.smartphone.gui.GuardMenu;
import com.example.smartphone.gui.GuardTierMenu;
import com.example.smartphone.gui.HomeMenu;
import com.example.smartphone.gui.KillerMenu;
import com.example.smartphone.gui.KillerTierMenu;
import com.example.smartphone.gui.PhoneMenu;
import com.example.smartphone.listeners.*;
import com.example.smartphone.managers.TaxiManager;
import com.example.smartphone.managers.OneBlockManager;
import com.example.smartphone.managers.DuelManager;
import com.example.smartphone.system.FriendSystem;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

public final class SmartPhonePlugin extends JavaPlugin {

    private static SmartPhonePlugin instance;

    private EconomyManager economyManager;
    private PlayerDataManager playerDataManager;
    private KillerManager killerManager;
    private GuardManager guardManager;
    private TaxiManager taxiManager;
    private FriendSystem friendSystem;
    private OneBlockManager oneBlockManager;
    private DuelManager duelManager;

    @Override
    public void onEnable() {
        instance = this;

        // [ЗАГРУЗКА CONFIG]
        saveDefaultConfig();

        // [МЕНЕДЖЕРЫ]
        playerDataManager = new PlayerDataManager(this);
        economyManager = new EconomyManager(this);
        killerManager = new KillerManager(this);
        guardManager = new GuardManager(this);
        taxiManager = new TaxiManager(this);
        friendSystem = new FriendSystem(this);
        oneBlockManager = new OneBlockManager(this);
        duelManager = new DuelManager(this);

        // [КОМАНДЫ]
        getCommand("phone").setExecutor(new PhoneCommand(this));
        getCommand("taxi").setExecutor(new TaxiCommand(this));
        getCommand("home").setExecutor(new HomeCommand(this));
        getCommand("sethome").setExecutor(new HomeCommand(this));
        getCommand("delhome").setExecutor(new HomeCommand(this));
        getCommand("killer").setExecutor(new KillerCommand(this));
        getCommand("guard").setExecutor(new GuardCommand(this));
        getCommand("balance").setExecutor(new BalanceCommand(this));
        getCommand("pay").setExecutor(new PayCommand(this));
        getCommand("top").setExecutor(new BalanceCommand(this));
        getCommand("shop").setExecutor(new ShopCommand(this));
        getCommand("perkpoints").setExecutor(new PerkPointsCommand(this));
        getCommand("mining").setExecutor(new MiningCommand(this));
        getCommand("duel").setExecutor(new DuelCommand(this));
        getCommand("godwand").setExecutor(new GodWandCommand(this));

        // [СЛУШАТЕЛИ]
        getServer().getPluginManager().registerEvents(new PhoneListener(this), this);
        getServer().getPluginManager().registerEvents(new PhoneMenuListener(this), this);
        getServer().getPluginManager().registerEvents(new HomeMenuListener(this), this);
        getServer().getPluginManager().registerEvents(new HouseMenuListener(this), this);
        getServer().getPluginManager().registerEvents(new OneBlockListener(this), this);
        getServer().getPluginManager().registerEvents(new OneBlockMenuListener(this), this);
        getServer().getPluginManager().registerEvents(new GUIMenuListener(this), this);
        getServer().getPluginManager().registerEvents(new JobListener(this), this);
        getServer().getPluginManager().registerEvents(new PerksMenuListener(this), this);
        getServer().getPluginManager().registerEvents(new PerksListener(this), this);
        getServer().getPluginManager().registerEvents(new MiningListener(this), this);
        getServer().getPluginManager().registerEvents(new GodWandListener(this), this);
        getServer().getPluginManager().registerEvents(new ShopMenuListener(this), this);
        getServer().getPluginManager().registerEvents(new PurchaseConfirmListener(this), this);
        getServer().getPluginManager().registerEvents(new HealthSyncListener(this), this);
        getServer().getPluginManager().registerEvents(new HealthBarListener(this), this);
        getServer().getPluginManager().registerEvents(new BlazeRodListener(this), this);
        getServer().getPluginManager().registerEvents(new DuelListener(this), this);
        getServer().getPluginManager().registerEvents(new DuelMenuListener(this), this);
        getServer().getPluginManager().registerEvents(new PhoenixListener(this), this);
        getServer().getPluginManager().registerEvents(new ShieldListener(this), this);
        getServer().getPluginManager().registerEvents(new DashListener(this), this);
        getServer().getPluginManager().registerEvents(new VampireListener(this), this);
        getServer().getPluginManager().registerEvents(new FireListener(this), this);
        RegenListener regenListener = new RegenListener(this);
        getServer().getPluginManager().registerEvents(new RegenDamageListener(this, regenListener), this);
        getServer().getPluginManager().registerEvents(killerManager, this);
        getServer().getPluginManager().registerEvents(guardManager, this);

        getLogger().info(ChatColor.GREEN + "SmartPhone Plugin включён!");
        getLogger().info(ChatColor.YELLOW + "Команды: /phone, /balance, /shop, /taxi, /home, /killer, /guard");
        getLogger().info(ChatColor.GREEN + "✓ Недвижимость: /phone → Недвижимость");
        getLogger().info(ChatColor.GREEN + "✓ Перки: /phone → Перки");
        getLogger().info(ChatColor.GREEN + "✓ Шахтинг: /phone → Шахтинг");
        if (getConfig().getBoolean("jobs.mining.enabled", true)) {
            getLogger().info(ChatColor.GREEN + "✓ Заработок на добыче блоков включён");
        }
    }

    @Override
    public void onDisable() {
        // [СОХРАНЕНИЕ ДАННЫХ]
        if (playerDataManager != null) {
            playerDataManager.saveAll();
        }
        if (killerManager != null) {
            killerManager.removeAllKillers();
        }
        if (guardManager != null) {
            guardManager.removeAllGuards();
        }
        if (oneBlockManager != null) {
            oneBlockManager.removeAllPlayers();
        }

        getLogger().info(ChatColor.RED + "SmartPhone Plugin выключён!");
    }

    public static SmartPhonePlugin getInstance() {
        return instance;
    }

    public EconomyManager getEconomyManager() {
        return economyManager;
    }

    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }

    public KillerManager getKillerManager() {
        return killerManager;
    }

    public GuardManager getGuardManager() {
        return guardManager;
    }

    public TaxiManager getTaxiManager() {
        return taxiManager;
    }

    public FriendSystem getFriendSystem() {
        return friendSystem;
    }

    public OneBlockManager getOneBlockManager() {
        return oneBlockManager;
    }

    public DuelManager getDuelManager() {
        return duelManager;
    }
}
