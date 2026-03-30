package me.vuxaer.hideandseek.listener;

import me.vuxaer.hideandseek.HideAndSeekPlugin;
import me.vuxaer.hideandseek.domain.BlockDisguise;
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
        if (event.getFrom().distanceSquared(event.getTo()) < 0.001) return;

        var plugin = HideAndSeekPlugin.getInstance();
        GameManager gm = plugin.getGameManager();
        GamePlayer gp = plugin.getPlayerManager().getPlayer(event.getPlayer());
        if (gp == null) return;
        if (gm.getState() == GameState.HIDING && gp.getRole() == PlayerRole.SEEKER) {
            event.setTo(event.getFrom());
            return;
        }
        if (gp.getRole() != PlayerRole.HIDER) return;

        BlockDisguise disguise = plugin.getDisguiseManager()
                .getDisguiseByPlayer(event.getPlayer());
        if (disguise == null) return;
        disguise.onMove(event.getFrom(), event.getTo());
        disguise.updatePosition();
    }
}