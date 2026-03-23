package me.vuxaer.hideandseek.listener;

import me.vuxaer.hideandseek.HideAndSeekPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        HideAndSeekPlugin.getInstance()
                .getPlayerManager()
                .addPlayer(player);

        Bukkit.broadcastMessage(player.getName() + " joined!");

        if (Bukkit.getOnlinePlayers().size() >= 2) {
            HideAndSeekPlugin.getInstance().getGameManager().startGame();
        }
    }
}