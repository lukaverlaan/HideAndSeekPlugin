package me.vuxaer.hideandseek;

import me.vuxaer.hideandseek.command.GameCommand;
import me.vuxaer.hideandseek.command.GameTabCompleter;
import me.vuxaer.hideandseek.listener.*;
import me.vuxaer.hideandseek.manager.*;
import me.vuxaer.hideandseek.net.HttpService;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class HideAndSeekPlugin extends JavaPlugin {
    private static HideAndSeekPlugin instance;

    private GameManager gameManager;
    private PlayerManager playerManager;
    private DisguiseManager disguiseManager;
    private ScoreboardManager scoreboardManager;
    private MessageManager messageManager;
    private SpawnManager spawnManager;
    private HttpService httpService;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();

        initializeManagers();
        registerOnlinePlayers();
        registerCommands();
        registerListeners();

        disguiseManager.startTask();
    }

    @Override
    public void onDisable() {
        if (disguiseManager != null) {
            disguiseManager.cleanup();
        }
    }

    private void initializeManagers() {
        messageManager = new MessageManager(this);

        playerManager = new PlayerManager();
        gameManager = new GameManager(playerManager);
        disguiseManager = new DisguiseManager(this);
        scoreboardManager = new ScoreboardManager(this);
        spawnManager = new SpawnManager(getConfig());

        httpService = new HttpService(this);
    }

    private void registerCommands() {
        PluginCommand command = getCommand("hs");
        command.setExecutor(new GameCommand(this));
        command.setTabCompleter(new GameTabCompleter());
    }

    private void registerListeners() {
        var pm = getServer().getPluginManager();
        pm.registerEvents(new JoinListener(), this);
        pm.registerEvents(new QuitListener(), this);
        pm.registerEvents(new MoveListener(), this);
        pm.registerEvents(new DamageListener(), this);
        pm.registerEvents(new BlockSelectorListener(), this);
        pm.registerEvents(new BlockHitListener(), this);
        pm.registerEvents(new BlockBreakListener(), this);
        pm.registerEvents(new InteractionHitListener(), this);
    }

    private void registerOnlinePlayers() {
        for (Player player : getServer().getOnlinePlayers()) {
            playerManager.addPlayer(player);
        }
    }

    public static HideAndSeekPlugin getInstance() {
        return instance;
    }

    public GameManager getGameManager() {
        return gameManager;
    }

    public PlayerManager getPlayerManager() {
        return playerManager;
    }

    public DisguiseManager getDisguiseManager() {
        return disguiseManager;
    }

    public ScoreboardManager getScoreboardManager() {
        return scoreboardManager;
    }

    public MessageManager getMessageManager() {
        return messageManager;
    }

    public SpawnManager getSpawnManager() {
        return spawnManager;
    }

    public HttpService getHttpService() {
        return httpService;
    }
}