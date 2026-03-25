package me.vuxaer.hideandseek.listener;

import me.vuxaer.hideandseek.HideAndSeekPlugin;
import me.vuxaer.hideandseek.gui.BlockSelector;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;

public class BlockSelectorListener implements Listener {

    @EventHandler
    public void onClick(InventoryClickEvent e) {

        if (!e.getView().getTitle().equals(BlockSelector.TITLE)) return;

        e.setCancelled(true);

        if (e.getCurrentItem() == null) return;

        Player player = (Player) e.getWhoClicked();
        Material mat = e.getCurrentItem().getType();

        HideAndSeekPlugin.getInstance()
                .getDisguiseManager()
                .disguise(player, mat);

        player.sendMessage("§aSelected: " + mat.name());
        player.closeInventory();
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {

        if (!e.getView().getTitle().equals(BlockSelector.TITLE)) return;

        Player player = (Player) e.getPlayer();

        var disguise = HideAndSeekPlugin.getInstance()
                .getDisguiseManager()
                .getDisguiseByPlayer(player);

        if (disguise == null) {
            Bukkit.getScheduler().runTaskLater(
                    HideAndSeekPlugin.getInstance(),
                    () -> BlockSelector.open(player),
                    1L
            );
        }
    }
}