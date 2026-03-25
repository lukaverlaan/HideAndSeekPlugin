package me.vuxaer.hideandseek.listener;

import me.vuxaer.hideandseek.HideAndSeekPlugin;
import me.vuxaer.hideandseek.domain.GamePlayer;
import me.vuxaer.hideandseek.manager.GameManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class QuitListener implements Listener {

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {

        var plugin = HideAndSeekPlugin.getInstance();
        Player player = event.getPlayer();

        GameManager gm = plugin.getGameManager();

        var disguise = plugin.getDisguiseManager().getDisguiseByPlayer(player);
        if (disguise != null) {
            plugin.getDisguiseManager().removeDisguise(disguise);
        }

        GamePlayer gp = plugin.getPlayerManager().getPlayer(player);

        if (gp != null) {
            gp.setAlive(false);
            plugin.getPlayerManager().removePlayer(player);
        }

        if (gm.isGameRunning()) {
            gm.checkWinCondition();
        }
    }
}