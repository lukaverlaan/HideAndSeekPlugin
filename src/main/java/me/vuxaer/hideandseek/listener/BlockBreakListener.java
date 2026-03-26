package me.vuxaer.hideandseek.listener;

import me.vuxaer.hideandseek.HideAndSeekPlugin;
import me.vuxaer.hideandseek.domain.BlockDisguise;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class BlockBreakListener implements Listener {

    @EventHandler
    public void onBreak(BlockBreakEvent event) {

        Player attacker = event.getPlayer();

        var plugin = HideAndSeekPlugin.getInstance();
        var gm = plugin.getGameManager();

        BlockDisguise disguise = plugin.getDisguiseManager()
                .getDisguise(event.getBlock().getLocation());

        if (disguise == null) return;

        event.setCancelled(true);

        Player victim = disguise.getPlayer();

        if (disguise.isSolid()) {
            disguise.breakDisguise();
        }

        gm.handleHit(attacker, victim);
    }
}