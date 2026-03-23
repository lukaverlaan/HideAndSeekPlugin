package me.vuxaer.hideandseek;

import me.vuxaer.hideandseek.listener.JoinListener;
import me.vuxaer.hideandseek.listener.MoveListener;
import me.vuxaer.hideandseek.manager.GameManager;
import me.vuxaer.hideandseek.manager.PlayerManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class HideAndSeekPlugin extends JavaPlugin {

    private static HideAndSeekPlugin instance;
    private GameManager gameManager;
    private PlayerManager playerManager;

    @Override
    public void onEnable() {
        instance = this;

        playerManager = new PlayerManager();
        gameManager = new GameManager(playerManager);

        getServer().getPluginManager().registerEvents(new JoinListener(), this);
        getServer().getPluginManager().registerEvents(new MoveListener(), this);
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
}
