package me.vuxaer.hideandseek.domain;

import me.vuxaer.hideandseek.HideAndSeekPlugin;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class BlockDisguise {

    private final Player player;
    private final Material material;

    private boolean solid = false;
    private long lastMoveTime;

    private ArmorStand stand;

    private Location blockLocation;
    private Material originalMaterial;

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
        ArmorStand as = player.getWorld().spawn(loc, ArmorStand.class);

        as.setInvisible(true);
        as.setGravity(false);
        as.setMarker(true);
        as.getEquipment().setHelmet(new ItemStack(material));

        return as;
    }

    public void updatePosition() {

        if (solid) {
            player.setVelocity(player.getVelocity().zero());
            return;
        }

        if (stand != null) {
            stand.teleport(player.getLocation().clone().add(0, -1.3, 0));
        }
    }

    public void onMove(Location from, Location to) {

        if (solid) {
            player.teleport(blockLocation.clone().add(0.5, 1, 0.5));
            return;
        }

        if (from.distanceSquared(to) < 0.001) return;

        lastMoveTime = System.currentTimeMillis();
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

        Location loc = player.getLocation().getBlock().getLocation();

        if (!loc.getBlock().getType().isAir()) return;
        if (!loc.clone().subtract(0, 1, 0).getBlock().getType().isSolid()) return;

        solid = true;
        blockLocation = loc;
        Block block = loc.getBlock();
        originalMaterial = block.getType();
        block.setType(material);
        removeStand();

        player.teleport(loc.clone().add(0.5, 1, 0.5));
        player.setVelocity(player.getVelocity().zero());
        player.setInvisible(true);
        player.setCollidable(false);
        player.setInvulnerable(true);
        player.setGravity(false);
        player.getWorld().spawnParticle(
                Particle.BLOCK_CRACK,
                loc.clone().add(0.5, 0.5, 0.5),
                20,
                material.createBlockData()
        );
        player.playSound(loc, Sound.BLOCK_STONE_PLACE, 1, 1);

        HideAndSeekPlugin.getInstance()
                .getDisguiseManager()
                .registerSolid(this, loc);
    }

    public void breakDisguise() {

        if (!solid) return;

        blockLocation.getBlock().setType(originalMaterial);

        HideAndSeekPlugin.getInstance()
                .getDisguiseManager()
                .unregisterSolid(blockLocation);

        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 0.8f);

        solid = false;

        player.setCollidable(true);
        player.setInvulnerable(false);
        player.setGravity(true);

        spawn();
    }

    public void remove() {

        if (solid && blockLocation != null) {
            blockLocation.getBlock().setType(originalMaterial);
        }

        removeStand();

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

    public Player getPlayer() {
        return player;
    }

    public boolean isSolid() {
        return solid;
    }

    public Location getBlockLocation() {
        return blockLocation;
    }
}