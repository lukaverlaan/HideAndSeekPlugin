package me.vuxaer.hideandseek.domain;

import me.vuxaer.hideandseek.util.PlayerRole;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

public class GamePlayer {

    private final Player player;
    private PlayerRole role;
    private int hits = 0;
    private boolean alive = true;
    private long lastHit;

    public GamePlayer(Player player) {
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }

    public PlayerRole getRole() {
        return role;
    }

    public void setRole(PlayerRole role) {
        this.role = role;
    }

    public int getHits() {
        return hits;
    }

    public boolean isDead() {
        return hits >= 3;
    }

    public boolean isAlive() {
        return alive;
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }

    public void reset() {
        hits = 0;
        alive = true;
        role = null;

        Player p = this.player;
        p.setGameMode(GameMode.ADVENTURE);
        p.setHealth(20.0);
        p.setFoodLevel(20);
        p.setFireTicks(0);
        p.setFallDistance(0);
        p.getActivePotionEffects().forEach(effect -> p.removePotionEffect(effect.getType()));
        p.setInvisible(false);
        p.setAllowFlight(false);
        p.setFlying(false);
        p.setGravity(true);
        p.setVelocity(p.getVelocity().zero());
    }

    public boolean canBeHit() {
        return System.currentTimeMillis() - lastHit > 500;
    }

    public void registerHit() {
        lastHit = System.currentTimeMillis();
        hits++;
    }
}