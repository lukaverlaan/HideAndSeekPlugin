package me.vuxaer.hideandseek.listener;

import me.vuxaer.hideandseek.HideAndSeekPlugin;
import me.vuxaer.hideandseek.gui.BlockSelector;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;

import java.util.Map;

public class BlockSelectorListener implements Listener {
    @EventHandler
    public void onClick(InventoryClickEvent e) {

        String title = HideAndSeekPlugin.getInstance()
                .getMessageManager()
                .get("choose_block");

        if (!e.getView().getTitle().equals(title)) return;

        e.setCancelled(true);

        if (e.getCurrentItem() == null) return;

        Player player = (Player) e.getWhoClicked();
        Material mat = e.getCurrentItem().getType();

        var plugin = HideAndSeekPlugin.getInstance();

        plugin.getDisguiseManager().disguise(player, mat);
        plugin.getGameManager().onHiderSelected(player);

        player.sendMessage(plugin.getMessageManager().get("selected_block", Map.of("block", "§e" + BlockSelector.formatMaterial(mat))));

        player.closeInventory();
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {

        if (!e.getView().getTitle().equals(BlockSelector.TITLE)) return;

        Player player = (Player) e.getPlayer();
        var plugin = HideAndSeekPlugin.getInstance();

        if (plugin.getGameManager().isForceClosed(player)) {
            return;
        }

        var disguise = plugin.getDisguiseManager()
                .getDisguiseByPlayer(player);

        if (disguise == null) {
            Bukkit.getScheduler().runTaskLater(
                    plugin,
                    () -> BlockSelector.open(player),
                    1L
            );
        }
    }
}