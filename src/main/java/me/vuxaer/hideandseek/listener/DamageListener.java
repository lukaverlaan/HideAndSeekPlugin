package me.vuxaer.hideandseek.listener;

import me.vuxaer.hideandseek.HideAndSeekPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class DamageListener implements Listener {

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        var plugin = HideAndSeekPlugin.getInstance();
        var gm = plugin.getGameManager();

        if (!gm.isGameRunning()) return;

        if (event.getEntity() instanceof Player &&
                event.getDamager() instanceof Player) {
            event.setCancelled(true);
        }
    }
}