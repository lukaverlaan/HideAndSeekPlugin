package me.vuxaer.hideandseek.domain;

import me.vuxaer.hideandseek.HideAndSeekPlugin;
import org.bukkit.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class BlockDisguise {

    private final HideAndSeekPlugin plugin = HideAndSeekPlugin.getInstance();

    private final Player player;
    private final Material material;

    private boolean solid = false;
    private long lastMoveTime;

    private ArmorStand stand;

    private Location solidLocation;
    private Material previousBlockType;

    public BlockDisguise(Player player, Material material) {
        this.player = player;
        this.material = material;
        this.lastMoveTime = System.currentTimeMillis();
    }

    public void spawn() {
        player.setInvisible(true);
        removeStand();
        stand = spawnStand(player.getLocation());
        lastMoveTime = System.currentTimeMillis();
    }

    private ArmorStand spawnStand(Location loc) {
        Location spawnLoc = loc.clone().add(0, -1.3, 0);
        ArmorStand as = player.getWorld().spawn(spawnLoc, ArmorStand.class);
        as.setInvisible(true);
        as.setGravity(false);
        as.setMarker(true);
        as.getEquipment().setHelmet(new ItemStack(material));
        as.setBasePlate(false);
        as.setArms(false);

        return as;
    }

    public void updatePosition() {
        if (!solid && stand != null) {
            stand.teleport(player.getLocation().clone().add(0, -1.3, 0));
        }
    }

    public void onMove(Location from, Location to) {
        if (from.distanceSquared(to) < 0.001) return;
        lastMoveTime = System.currentTimeMillis();
        if (solid) {
            breakDisguise();
        }
    }

    public void breakDisguise() {
        if (!solid) return;
        if (solidLocation != null && previousBlockType != null) {
            solidLocation.getBlock().setType(previousBlockType);
        }
        plugin.getDisguiseManager().unregisterSolid(solidLocation);
        player.setGameMode(GameMode.ADVENTURE);
        player.teleport(solidLocation.clone().add(0.5, 0, 0.5));
        player.setInvisible(true);
        player.setGravity(true);
        sendActionBar(player, plugin.getMessageManager().get("you_moved"));
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 0.8f);
        solid = false;
        spawn();
    }

    public void checkStillness() {
        if (solid) return;
        if (!player.isOnGround()) return;
        long now = System.currentTimeMillis();
        if (now - lastMoveTime >= 3000) {
            turnIntoBlock();
        }
    }

    private void turnIntoBlock() {
        solid = true;
        Location loc = findBestLocation();
        solidLocation = loc;
        previousBlockType = loc.getBlock().getType();
        loc.getBlock().setType(material);

        player.setGameMode(GameMode.SPECTATOR);
        player.teleport(loc.clone().add(0.5, 0, 0.5));

        removeStand();

        sendActionBar(player, plugin.getMessageManager().get("you_are_solid"));
        player.playSound(loc, Sound.BLOCK_STONE_PLACE, 1, 1);

        plugin.getDisguiseManager().registerSolid(this, loc);
    }

    public Location findBestLocation() {
        Location base = player.getLocation();
        Location best = null;
        double bestDistance = Double.MAX_VALUE;
        int[][] offsets = {{0, 0}, {1, 0}, {-1, 0}, {0, 1}, {0, -1}};

        for (int[] offset : offsets) {
            Location check = base.clone().add(offset[0], 0, offset[1]).getBlock().getLocation();
            if (!check.clone().subtract(0, 1, 0).getBlock().getType().isSolid()) continue;
            double distance = check.clone().add(0.5, 0.5, 0.5).distanceSquared(base);
            if (distance < bestDistance) {
                bestDistance = distance;
                best = check;
            }
        }

        if (best == null) {
            best = base.clone().subtract(0, 1, 0).getBlock().getLocation();
        }

        return best;
    }

    public void remove() {
        removeStand();
        if (solid && solidLocation != null && previousBlockType != null) {
            solidLocation.getBlock().setType(previousBlockType);
        }
        player.setInvisible(false);
        player.setGameMode(GameMode.ADVENTURE);
    }

    private void removeStand() {
        if (stand != null && !stand.isDead()) stand.remove();
        stand = null;
    }

    public static void sendActionBar(Player player, String message) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message));
    }

    public Player getPlayer() {
        return player;
    }

    public boolean isSolid() {
        return solid;
    }
}