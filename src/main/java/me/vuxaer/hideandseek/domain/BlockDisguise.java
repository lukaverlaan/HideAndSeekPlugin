package me.vuxaer.hideandseek.domain;

import me.vuxaer.hideandseek.HideAndSeekPlugin;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

public class BlockDisguise {

    private final Player player;
    private final Material material;

    private boolean solid = false;
    private long lastMoveTime;

    private ArmorStand stand;
    private BlockDisplay display;

    public BlockDisguise(Player player, Material material) {
        this.player = player;
        this.material = material;
        this.lastMoveTime = System.currentTimeMillis();
    }

    public void spawn() {

        player.setInvisible(true);

        removeDisplay();

        if (stand == null) {
            stand = spawnStand(player.getLocation());
        }

        lastMoveTime = System.currentTimeMillis();
    }

    private ArmorStand spawnStand(Location loc) {
        ArmorStand as = player.getWorld().spawn(loc, ArmorStand.class);

        as.setInvisible(true);
        as.setGravity(false);
        as.setMarker(true);

        as.getEquipment().setHelmet(new ItemStack(material));

        return as;
    }

    private BlockDisplay spawnDisplay(Location loc) {

        BlockDisplay bd = (BlockDisplay) player.getWorld()
                .spawnEntity(loc, EntityType.BLOCK_DISPLAY);

        bd.setBlock(material.createBlockData());

        bd.setTransformation(new Transformation(
                new Vector3f(0, 0, 0),
                new AxisAngle4f(),
                new Vector3f(1, 1, 1),
                new AxisAngle4f()
        ));

        bd.setInterpolationDuration(0);
        bd.setInterpolationDelay(0);

        return bd;
    }

    public void updatePosition() {

        if (!solid && stand != null) {
            Location loc = player.getLocation();

            stand.teleport(loc.clone().add(0, -1.3, 0));
        }

        if (solid && display != null) {
            Location loc = player.getLocation().getBlock().getLocation();
            display.teleport(loc);
        }
    }

    public void onMove(Location from, Location to) {

        if (from.distanceSquared(to) < 0.001) return;

        lastMoveTime = System.currentTimeMillis();

        if (solid) {

            HideAndSeekPlugin.getInstance()
                    .getDisguiseManager()
                    .unregisterSolid(from.getBlock().getLocation());

            player.spigot().sendMessage(
                    net.md_5.bungee.api.ChatMessageType.ACTION_BAR,
                    new net.md_5.bungee.api.chat.TextComponent(
                            HideAndSeekPlugin.getInstance()
                                    .getMessageManager()
                                    .get("you_moved")
                    )
            );

            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 0.8f);

            solid = false;

            removeDisplay();
            spawn();

            player.setCollidable(true);
            player.setInvulnerable(false);
            player.setGravity(true);
        }
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

        player.teleport(loc.clone().add(0.5, 0, 0.5));
        player.setVelocity(player.getVelocity().zero());
        player.setFallDistance(0);
        player.setCollidable(false);
        player.setInvulnerable(true);
        player.setGravity(false);
        player.setInvisible(true);

        removeStand();

        display = spawnDisplay(loc);

        player.getWorld().spawnParticle(
                Particle.BLOCK_CRACK,
                loc.clone().add(0.5, 0.5, 0.5),
                20,
                material.createBlockData()
        );

        player.playSound(loc, Sound.BLOCK_STONE_PLACE, 1, 1);

        player.spigot().sendMessage(
                net.md_5.bungee.api.ChatMessageType.ACTION_BAR,
                new net.md_5.bungee.api.chat.TextComponent(
                        HideAndSeekPlugin.getInstance()
                                .getMessageManager()
                                .get("you_are_solid")
                )
        );

        HideAndSeekPlugin.getInstance()
                .getDisguiseManager()
                .registerSolid(this, loc);
    }

    public Location findBestLocation() {
        Location base = player.getLocation();
        Location best = null;
        double bestDistance = Double.MAX_VALUE;

        int[][] offsets = {
                {0, 0},
                {1, 0},
                {-1, 0},
                {0, 1},
                {0, -1}
        };

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
        removeDisplay();

        player.setInvisible(false);
        player.setCollidable(true);
        player.setInvulnerable(false);
        player.setGravity(true);
    }

    private void removeStand() {
        if (stand != null && !stand.isDead()) {
            stand.remove();
        }
        stand = null;
    }

    private void removeDisplay() {
        if (display != null && !display.isDead()) {
            display.remove();
        }
        display = null;
    }

    public Player getPlayer() {
        return player;
    }

    public boolean isSolid() {
        return solid;
    }
}