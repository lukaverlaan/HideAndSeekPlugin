package me.vuxaer.hideandseek.domain;

import me.vuxaer.hideandseek.HideAndSeekPlugin;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
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
    private final HideAndSeekPlugin plugin = HideAndSeekPlugin.getInstance();

    private final Player player;
    private final Material material;

    private boolean solid = false;
    private long lastMoveTime;

    private ArmorStand stand;
    private BlockDisplay display;
    private Interaction interaction;

    private Location solidLocation;

    public BlockDisguise(Player player, Material material) {
        this.player = player;
        this.material = material;
        this.lastMoveTime = System.currentTimeMillis();
    }

    public void spawn() {
        player.setInvisible(true);

        removeDisplay();
        removeInteraction();

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
                new Vector3f(-0.5f, 0, -0.5f),
                new AxisAngle4f(),
                new Vector3f(1, 1, 1),
                new AxisAngle4f()
        ));

        bd.setInterpolationDuration(0);
        bd.setInterpolationDelay(0);

        return bd;
    }

    private Interaction spawnInteraction(Location loc) {

        Interaction interaction = (Interaction) player.getWorld()
                .spawnEntity(loc.clone().add(0, 0.5, 0), EntityType.INTERACTION);

        interaction.setInteractionWidth(1.0f);
        interaction.setInteractionHeight(1.0f);

        HideAndSeekPlugin.getInstance()
                .getDisguiseManager()
                .registerInteraction(interaction, this);

        return interaction;
    }

    public void updatePosition() {

        if (!solid && stand != null) {
            stand.teleport(player.getLocation().clone().add(0, -1.3, 0));
        }

        if (solid && display != null) {
            display.teleport(solidLocation);
            interaction.teleport(solidLocation.clone());
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

        HideAndSeekPlugin.getInstance()
                .getDisguiseManager()
                .unregisterSolid(solidLocation);

        HideAndSeekPlugin.getInstance()
                .getDisguiseManager()
                .unregisterInteraction(interaction);

        sendActionBar(player, plugin.getMessageManager().get("you_moved"));

        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 0.8f);

        solid = false;

        removeDisplay();
        removeInteraction();
        spawn();

        player.setCollidable(true);
        player.setInvulnerable(false);
        player.setGravity(true);
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

        player.teleport(loc.clone().add(0.5, 0, 0.5));
        player.setVelocity(player.getVelocity().zero());
        player.setFallDistance(0);
        player.setCollidable(false);
        player.setInvulnerable(true);
        player.setGravity(false);
        player.setInvisible(true);

        removeStand();

        Location centered = loc.clone().add(0.5, 0, 0.5);

        display = spawnDisplay(centered);
        interaction = spawnInteraction(centered);
        solidLocation = centered;

        player.getWorld().spawnParticle(
                Particle.BLOCK_CRACK,
                loc.clone().add(0.5, 0.5, 0.5),
                20,
                material.createBlockData()
        );

        sendActionBar(player, plugin.getMessageManager().get("you_are_solid"));

        player.playSound(loc, Sound.BLOCK_STONE_PLACE, 1, 1);

        HideAndSeekPlugin.getInstance()
                .getDisguiseManager()
                .registerSolid(this, loc);
    }

    public Location findBestLocation() {
        Location base = player.getLocation();
        Location best = null;
        double bestDistance = Double.MAX_VALUE;

        int[][] offsets = {
                {0, 0}, {1, 0}, {-1, 0}, {0, 1}, {0, -1}
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
        removeInteraction();

        player.setInvisible(false);
        player.setCollidable(true);
        player.setInvulnerable(false);
        player.setGravity(true);
    }

    private void removeStand() {
        if (stand != null && !stand.isDead()) stand.remove();
        stand = null;
    }

    private void removeDisplay() {
        if (display != null && !display.isDead()) display.remove();
        display = null;
    }

    private void removeInteraction() {
        if (interaction != null && !interaction.isDead()) {
            interaction.remove();
        }
        interaction = null;
    }

    public static void sendActionBar(Player player, String message) {
        player.spigot().sendMessage(
                ChatMessageType.ACTION_BAR,
                new TextComponent(message)
        );
    }

    public Player getPlayer() {
        return player;
    }

    public boolean isSolid() {
        return solid;
    }
}