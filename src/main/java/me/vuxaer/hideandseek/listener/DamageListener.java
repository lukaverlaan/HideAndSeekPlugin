package me.vuxaer.hideandseek.listener;

import me.vuxaer.hideandseek.HideAndSeekPlugin;
import me.vuxaer.hideandseek.domain.GamePlayer;
import me.vuxaer.hideandseek.manager.GameManager;
import me.vuxaer.hideandseek.util.GameState;
import me.vuxaer.hideandseek.util.PlayerRole;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class DamageListener implements Listener {

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {

        if (!(event.getEntity() instanceof Player victim)) return;
        if (!(event.getDamager() instanceof Player attacker)) return;

        GameManager gm = HideAndSeekPlugin.getInstance().getGameManager();

        if (gm.getState() != GameState.SEEKING) {
            event.setCancelled(true);
            return;
        }

        GamePlayer victimGP = HideAndSeekPlugin.getInstance()
                .getPlayerManager()
                .getPlayer(victim);

        GamePlayer attackerGP = HideAndSeekPlugin.getInstance()
                .getPlayerManager()
                .getPlayer(attacker);

        if (victimGP == null || attackerGP == null) return;

        // Alleen seekers mogen hiders hitten
        if (attackerGP.getRole() != PlayerRole.SEEKER ||
                victimGP.getRole() != PlayerRole.HIDER) {
            event.setCancelled(true);
            return;
        }

        event.setCancelled(true);

        victimGP.addHit();

        victim.sendMessage("You got hit! (" + victimGP.getHits() + "/3)");

        if (victimGP.isDead()) {
            victimGP.setAlive(false);

            victim.sendMessage("You are eliminated!");

            victim.setGameMode(GameMode.SPECTATOR);

            gm.checkWinCondition();
        }
    }
}
