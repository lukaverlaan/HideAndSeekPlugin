package me.vuxaer.hideandseek;

import me.vuxaer.hideandseek.listener.*;
import me.vuxaer.hideandseek.manager.DisguiseManager;
import me.vuxaer.hideandseek.manager.GameManager;
import me.vuxaer.hideandseek.manager.PlayerManager;
import me.vuxaer.hideandseek.manager.ScoreboardManager;
import me.vuxaer.hideandseek.net.HttpService;
import org.bukkit.plugin.java.JavaPlugin;

public final class HideAndSeekPlugin extends JavaPlugin {

    private static HideAndSeekPlugin instance;

    private GameManager gameManager;
    private PlayerManager playerManager;
    private DisguiseManager disguiseManager;
    private ScoreboardManager scoreboardManager;

    private HttpService httpService;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();

        playerManager = new PlayerManager();
        gameManager = new GameManager(playerManager);
        disguiseManager = new DisguiseManager(this);
        scoreboardManager = new ScoreboardManager(this);

        httpService = new HttpService(this);

        registerListeners();

        disguiseManager.startTask();
    }

    @Override
    public void onDisable() {
        disguiseManager.cleanup();
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new JoinListener(), this);
        getServer().getPluginManager().registerEvents(new QuitListener(), this);
        getServer().getPluginManager().registerEvents(new MoveListener(), this);
        getServer().getPluginManager().registerEvents(new DamageListener(), this);
        getServer().getPluginManager().registerEvents(new BlockHitListener(), this);
        getServer().getPluginManager().registerEvents(new BlockSelectorListener(), this);
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

    public HttpService getHttpService() {
        return httpService;
    }
}