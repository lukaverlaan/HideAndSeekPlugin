package me.vuxaer.hideandseek.listener;

import me.vuxaer.hideandseek.HideAndSeekPlugin;
import me.vuxaer.hideandseek.domain.GamePlayer;
import me.vuxaer.hideandseek.manager.GameManager;
import me.vuxaer.hideandseek.util.GameState;
import me.vuxaer.hideandseek.util.PlayerRole;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class MoveListener implements Listener {

    @EventHandler
    public void onMove(PlayerMoveEvent event) {

        GameManager gm = HideAndSeekPlugin.getInstance().getGameManager();

        if (gm.getState() == GameState.HIDING) {

            GamePlayer gp = HideAndSeekPlugin.getInstance()
                    .getPlayerManager()
                    .getPlayer(event.getPlayer());

            if (gp != null && gp.getRole() == PlayerRole.SEEKER) {
                event.setTo(event.getFrom());
            }
        }
    }
}