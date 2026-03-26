package me.vuxaer.hideandseek.listener;

import me.vuxaer.hideandseek.HideAndSeekPlugin;
import me.vuxaer.hideandseek.domain.BlockDisguise;
import me.vuxaer.hideandseek.domain.GamePlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        player.setFlying(false);
        player.setAllowFlight(false);
        player.setGravity(true);

        var plugin = HideAndSeekPlugin.getInstance();

        plugin.getPlayerManager().addPlayer(player);

        BlockDisguise disguise = plugin.getDisguiseManager().getDisguiseByPlayer(player);
        if (disguise != null) {
            plugin.getDisguiseManager().removeDisguise(disguise);
        }

        GamePlayer gp = plugin.getPlayerManager().getPlayer(player);
        if (gp != null) {
            gp.reset();
        }

        if (!plugin.getGameManager().isGameRunning()
                && Bukkit.getOnlinePlayers().size() >= 2) {

            plugin.getGameManager().startGame();
        }
    }
}