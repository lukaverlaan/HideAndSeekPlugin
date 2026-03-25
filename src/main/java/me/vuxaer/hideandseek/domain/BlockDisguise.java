package me.vuxaer.hideandseek.domain;

import me.vuxaer.hideandseek.HideAndSeekPlugin;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class BlockDisguise {

    private final Player player;
    private final Material material;

    private boolean solid = false;
    private Location blockLocation;

    private long lastMoveTime;

    private ArmorStand stand;

    public BlockDisguise(Player player, Material material) {
        this.player = player;
        this.material = material;
        this.lastMoveTime = System.currentTimeMillis();
    }

    public void spawn() {
        player.setInvisible(true);

        if (stand == null) {
            stand = player.getWorld().spawn(player.getLocation(), ArmorStand.class);

            stand.setInvisible(true);
            stand.setGravity(false);
            stand.setMarker(true);

            stand.getEquipment().setHelmet(new ItemStack(material));
        }

        lastMoveTime = System.currentTimeMillis();
    }

    public void remove() {
        removeStand();
        removeBlock();

        player.setInvisible(false);
        player.setCollidable(true);
        player.setInvulnerable(false);
    }

    private void removeStand() {
        if (stand != null && !stand.isDead()) {
            stand.remove();
        }
        stand = null;
    }

    public void updatePosition() {
        if (stand == null || solid) return;

        stand.teleport(player.getLocation().add(0, -1.3, 0));
    }

    public void onMove(Location from, Location to) {

        if (from.distanceSquared(to) < 0.001) return;

        lastMoveTime = System.currentTimeMillis();

        if (solid) {
            removeBlock();
            spawn();
            solid = false;

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

        removeStand();

        blockLocation = player.getLocation().getBlock().getLocation();

        Location center = blockLocation.clone().add(0.5, 0, 0.5);

        player.teleport(center);

        player.setVelocity(player.getVelocity().zero());
        player.setFallDistance(0);

        player.setCollidable(false);
        player.setInvulnerable(true);
        player.setInvisible(true);
        player.setGravity(false);

        org.bukkit.Bukkit.getScheduler().runTaskLater(
                HideAndSeekPlugin.getInstance(),
                () -> {

                    blockLocation.getBlock().setType(material);

                    player.teleport(center.clone().add(0, 0.01, 0));

                    solid = true;

                    HideAndSeekPlugin.getInstance()
                            .getDisguiseManager()
                            .registerSolid(this, blockLocation);

                },
                1L
        );
    }

    private void removeBlock() {
        if (blockLocation != null) {
            if (blockLocation.getBlock().getType() == material) {
                blockLocation.getBlock().setType(Material.AIR);
            }

            HideAndSeekPlugin.getInstance()
                    .getDisguiseManager()
                    .unregisterSolid(blockLocation);

            blockLocation = null;
        }
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