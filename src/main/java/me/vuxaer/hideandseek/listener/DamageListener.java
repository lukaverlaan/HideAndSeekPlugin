package me.vuxaer.hideandseek.listener;

import me.vuxaer.hideandseek.HideAndSeekPlugin;
import me.vuxaer.hideandseek.util.GameState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class DamageListener implements Listener {

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {

        if (!(event.getEntity() instanceof Player victim)) return;
        if (!(event.getDamager() instanceof Player attacker)) return;

        var plugin = HideAndSeekPlugin.getInstance();
        var gm = plugin.getGameManager();

        if (gm.getState() != GameState.SEEKING) {
            event.setCancelled(true);
            return;
        }

        event.setCancelled(true);

        gm.handleHit(attacker, victim);
    }
}