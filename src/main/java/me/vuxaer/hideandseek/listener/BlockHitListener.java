package me.vuxaer.hideandseek.listener;

import me.vuxaer.hideandseek.HideAndSeekPlugin;
import me.vuxaer.hideandseek.domain.BlockDisguise;
import me.vuxaer.hideandseek.domain.GamePlayer;
import me.vuxaer.hideandseek.util.PlayerRole;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class BlockHitListener implements Listener {

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {

        if (event.getAction() != Action.LEFT_CLICK_BLOCK) return;

        Player player = event.getPlayer();

        GamePlayer attacker = HideAndSeekPlugin.getInstance()
                .getPlayerManager()
                .getPlayer(player);

        if (attacker == null || attacker.getRole() != PlayerRole.SEEKER) return;

        Location loc = event.getClickedBlock().getLocation();

        BlockDisguise disguise = HideAndSeekPlugin.getInstance()
                .getDisguiseManager()
                .getDisguise(loc);

        if (disguise == null) return;

        event.setCancelled(true);

        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_STRONG, 1, 1);

        player.getWorld().spawnParticle(
                Particle.BLOCK_CRACK,
                loc.clone().add(0.5, 0.5, 0.5),
                10,
                event.getClickedBlock().getBlockData()
        );

        Player victim = disguise.getPlayer();

        GamePlayer victimGP = HideAndSeekPlugin.getInstance()
                .getPlayerManager()
                .getPlayer(victim);

        if (!victimGP.canBeHit()) return;

        victimGP.registerHit();

        victim.sendMessage("You got hit! (" + victimGP.getHits() + "/3)");

        if (victimGP.isDead()) {
            victimGP.setAlive(false);

            victim.sendMessage("You are eliminated!");
            victim.setGameMode(GameMode.SPECTATOR);

            HideAndSeekPlugin.getInstance()
                    .getGameManager()
                    .checkWinCondition();
        }
    }
}