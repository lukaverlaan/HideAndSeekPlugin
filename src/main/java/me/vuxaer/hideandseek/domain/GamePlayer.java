package me.vuxaer.hideandseek.domain;

import me.vuxaer.hideandseek.util.PlayerRole;
import org.bukkit.entity.Player;

public class GamePlayer {

    private final Player player;
    private PlayerRole role;

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
}
