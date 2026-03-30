package me.vuxaer.hideandseek.manager;

import me.vuxaer.hideandseek.domain.BlockDisguise;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class DisguiseManager {

    private final JavaPlugin plugin;

    private final Map<String, BlockDisguise> solidDisguises = new HashMap<>();
    private final Set<BlockDisguise> activeDisguises = new HashSet<>();
    private final Map<Interaction, BlockDisguise> interactionMap = new HashMap<>();

    public DisguiseManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    private String toKey(Location loc) {
        return loc.getWorld().getName() + ":" +
                loc.getBlockX() + ":" +
                loc.getBlockY() + ":" +
                loc.getBlockZ();
    }

    public void registerSolid(BlockDisguise disguise, Location loc) {
        solidDisguises.put(toKey(loc), disguise);
    }

    public void unregisterSolid(Location loc) {
        if (loc == null) return;
        solidDisguises.remove(toKey(loc));
    }

    public BlockDisguise getDisguise(Location loc) {
        if (loc == null) return null;
        return solidDisguises.get(toKey(loc));
    }

    public BlockDisguise getDisguiseByPlayer(Player player) {
        return activeDisguises.stream()
                .filter(d -> d.getPlayer().equals(player))
                .findFirst()
                .orElse(null);
    }

    public BlockDisguise getByInteraction(Interaction interaction) {
        return interactionMap.get(interaction);
    }

    public void registerInteraction(Interaction interaction, BlockDisguise disguise) {
        interactionMap.put(interaction, disguise);
    }

    public void unregisterInteraction(Interaction interaction) {
        interactionMap.remove(interaction);
    }

    public void disguise(Player player, Material material) {
        BlockDisguise existing = getDisguiseByPlayer(player);
        if (existing != null) {
            removeDisguise(existing);
        }

        BlockDisguise disguise = new BlockDisguise(player, material);
        addDisguise(disguise);
        disguise.spawn();
    }

    public void addDisguise(BlockDisguise disguise) {
        activeDisguises.add(disguise);
    }

    public void removeDisguise(BlockDisguise disguise) {
        activeDisguises.remove(disguise);
        disguise.remove();
    }

    public void startTask() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (BlockDisguise disguise : new HashSet<>(activeDisguises)) {
                if (disguise.getPlayer() == null || !disguise.getPlayer().isOnline()) {
                    removeDisguise(disguise);
                    continue;
                }
                disguise.updatePosition();
                disguise.checkStillness();
            }
        }, 1L, 1L);
    }

    public void cleanup() {
        for (BlockDisguise disguise : activeDisguises) {
            disguise.remove();
        }
        activeDisguises.clear();
        solidDisguises.clear();
        interactionMap.clear();
    }
}