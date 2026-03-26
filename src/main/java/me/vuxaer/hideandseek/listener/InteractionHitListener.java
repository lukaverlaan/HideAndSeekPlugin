package me.vuxaer.hideandseek.listener;

import me.vuxaer.hideandseek.HideAndSeekPlugin;
import me.vuxaer.hideandseek.domain.BlockDisguise;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class InteractionHitListener implements Listener {

    @EventHandler
    public void onHit(EntityDamageByEntityEvent event) {

        if (!(event.getDamager() instanceof Player attacker)) return;
        if (!(event.getEntity() instanceof Interaction interaction)) return;

        var plugin = HideAndSeekPlugin.getInstance();
        var gm = plugin.getGameManager();

        BlockDisguise disguise = plugin.getDisguiseManager()
                .getByInteraction(interaction);

        if (disguise == null) return;

        event.setCancelled(true);

        Player victim = disguise.getPlayer();

        if (disguise.isSolid()) {
            disguise.breakDisguise();
        }

        gm.handleHit(attacker, victim);
    }
}