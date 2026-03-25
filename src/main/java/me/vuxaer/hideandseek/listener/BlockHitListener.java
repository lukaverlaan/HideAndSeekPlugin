package me.vuxaer.hideandseek.listener;

import me.vuxaer.hideandseek.HideAndSeekPlugin;
import me.vuxaer.hideandseek.domain.BlockDisguise;
import me.vuxaer.hideandseek.domain.GamePlayer;
import me.vuxaer.hideandseek.util.PlayerRole;
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

        var plugin = HideAndSeekPlugin.getInstance();
        var gm = plugin.getGameManager();

        GamePlayer attacker = plugin.getPlayerManager().getPlayer(player);

        if (attacker == null || attacker.getRole() != PlayerRole.SEEKER) return;

        var clicked = event.getClickedBlock().getLocation();

        BlockDisguise disguise = null;

        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    var checkLoc = clicked.clone().add(x, y, z);
                    disguise = plugin.getDisguiseManager().getDisguise(checkLoc);
                    if (disguise != null) break;
                }
                if (disguise != null) break;
            }
            if (disguise != null) break;
        }
        if (disguise == null) return;

        event.setCancelled(true);

        gm.handleHit(player, disguise.getPlayer());
    }
}