package me.vuxaer.hideandseek.domain;

import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class GameResult {

    private final List<PlayerData> seekers;
    private final List<PlayerData> hiders;
    private final String winner;
    private final long durationSeconds;

    public GameResult(List<Player> seekers, List<Player> hiders, String winner, long durationSeconds) {
        this.seekers = seekers.stream()
                .map(PlayerData::new)
                .collect(Collectors.toList());
        this.hiders = hiders.stream()
                .map(PlayerData::new)
                .collect(Collectors.toList());
        this.winner = winner;
        this.durationSeconds = durationSeconds;
    }

    public static class PlayerData {
        private final UUID uuid;
        private final String name;

        public PlayerData(Player player) {
            this.uuid = player.getUniqueId();
            this.name = player.getName();
        }

        public UUID getUuid() {
            return uuid;
        }

        public String getName() {
            return name;
        }
    }

    public List<PlayerData> getSeekers() {
        return seekers;
    }

    public List<PlayerData> getHiders() {
        return hiders;
    }

    public String getWinner() {
        return winner;
    }

    public long getDurationSeconds() {
        return durationSeconds;
    }
}