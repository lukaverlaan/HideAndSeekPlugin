package me.vuxaer.hideandseek.listener;

import me.vuxaer.hideandseek.HideAndSeekPlugin;
import me.vuxaer.hideandseek.domain.BlockDisguise;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.block.Action;
import org.bukkit.entity.Player;

public class BlockHitListener implements Listener {

    @EventHandler
    public void onHit(PlayerInteractEvent event) {

        if (event.getAction() != Action.LEFT_CLICK_BLOCK) return;

        Player attacker = event.getPlayer();

        var plugin = HideAndSeekPlugin.getInstance();
        var gm = plugin.getGameManager();

        var block = event.getClickedBlock();
        if (block == null) return;

        var loc = block.getLocation().getBlock().getLocation();

        BlockDisguise disguise = plugin.getDisguiseManager().getDisguise(loc);

        if (disguise == null) return;

        Player victim = disguise.getPlayer();

        if (disguise.isSolid()) {
            disguise.breakDisguise();
        }

        gm.handleHit(attacker, victim);
    }
}