package me.vuxaer.hideandseek.domain;

import me.vuxaer.hideandseek.util.PlayerRole;
import org.bukkit.entity.Player;

public class GamePlayer {

    private final Player player;
    private PlayerRole role;
    private int hits = 0;
    private boolean alive = true;

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

    public void addHit() {
        hits++;
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
}