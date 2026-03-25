package me.vuxaer.hideandseek.domain;

import me.vuxaer.hideandseek.HideAndSeekPlugin;
import org.bukkit.Location;
import org.bukkit.Material;
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

        Location loc = player.getLocation().getBlock().getLocation();

        player.teleport(loc.clone().add(0.5, 0, 0.5));

        player.setVelocity(player.getVelocity().zero());
        player.setFallDistance(0);

        player.setCollidable(false);
        player.setInvulnerable(true);
        player.setGravity(false);
        player.setInvisible(true);

        removeStand();

        display = spawnDisplay(loc);

        HideAndSeekPlugin.getInstance()
                .getDisguiseManager()
                .registerSolid(this, loc);
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