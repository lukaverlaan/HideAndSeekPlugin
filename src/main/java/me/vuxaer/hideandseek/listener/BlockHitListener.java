package me.vuxaer.hideandseek.listener;

import me.vuxaer.hideandseek.HideAndSeekPlugin;
import me.vuxaer.hideandseek.domain.BlockDisguise;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class BlockHitListener implements Listener {
    @EventHandler
    public void onBlockHit(PlayerInteractEvent event) {
        if (event.getAction() != Action.LEFT_CLICK_BLOCK) return;
        if (event.getClickedBlock() == null) return;

        var plugin = HideAndSeekPlugin.getInstance();
        var loc = event.getClickedBlock().getLocation();
        BlockDisguise disguise = plugin.getDisguiseManager().getDisguise(loc);

        if (disguise == null) return;
        event.setCancelled(true);

        Player attacker = event.getPlayer();
        Player victim = disguise.getPlayer();

        Vector direction = victim.getLocation().toVector()
                .subtract(attacker.getLocation().toVector())
                .normalize();
        direction.setY(0.35);

        if (disguise.isSolid()) {
            disguise.breakDisguise();
        }

        victim.setVelocity(direction.multiply(0.4));
        plugin.getGameManager().handleHit(attacker, victim);
    }
}