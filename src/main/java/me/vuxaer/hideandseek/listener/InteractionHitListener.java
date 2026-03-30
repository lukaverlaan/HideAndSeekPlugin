package me.vuxaer.hideandseek.listener;

import me.vuxaer.hideandseek.HideAndSeekPlugin;
import me.vuxaer.hideandseek.domain.BlockDisguise;
import me.vuxaer.hideandseek.util.PlayerRole;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.util.Vector;

public class InteractionHitListener implements Listener {

    @EventHandler
    public void onHit(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Interaction interaction)) return;
        if (!(event.getDamager() instanceof Player attacker)) return;

        var plugin = HideAndSeekPlugin.getInstance();
        var gm = plugin.getGameManager();
        var gp = plugin.getPlayerManager().getPlayer(attacker);
        if (gp == null || gp.getRole() != PlayerRole.SEEKER) return;

        BlockDisguise disguise = plugin.getDisguiseManager().getByInteraction(interaction);
        if (disguise == null) return;

        event.setCancelled(true);

        Player victim = disguise.getPlayer();

        Vector direction = victim.getLocation().toVector()
                .subtract(attacker.getLocation().toVector())
                .normalize();
        direction.setY(0.35);

        victim.setVelocity(direction.multiply(0.4));

        gm.handleHit(attacker, victim);
    }
}