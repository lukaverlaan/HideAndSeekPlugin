package me.vuxaer.hideandseek.manager;

import me.vuxaer.hideandseek.domain.GamePlayer;
import org.bukkit.entity.Player;

import java.util.*;

public class PlayerManager {

    private final Map<UUID, GamePlayer> players = new HashMap<>();

    public void addPlayer(Player player) {
        players.put(player.getUniqueId(), new GamePlayer(player));
    }

    public void removePlayer(Player player) {
        players.remove(player.getUniqueId());
    }

    public GamePlayer getPlayer(Player player) {
        return players.get(player.getUniqueId());
    }

    public Collection<GamePlayer> getAllPlayers() {
        return players.values();
    }
}