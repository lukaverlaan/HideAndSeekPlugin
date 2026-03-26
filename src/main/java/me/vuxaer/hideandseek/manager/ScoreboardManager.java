package me.vuxaer.hideandseek.manager;

import me.vuxaer.hideandseek.HideAndSeekPlugin;
import me.vuxaer.hideandseek.util.GameState;
import me.vuxaer.hideandseek.util.PlayerRole;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class ScoreboardManager {

    private final HideAndSeekPlugin plugin;
    private final Map<Player, Scoreboard> boards = new HashMap<>();

    public ScoreboardManager(HideAndSeekPlugin plugin) {
        this.plugin = plugin;
    }

    public void updateAll(int timeLeft) {

        int hiders = (int) plugin.getPlayerManager().getAllPlayers().stream()
                .filter(p -> p.getRole() == PlayerRole.HIDER && p.isAlive())
                .count();

        int seekers = (int) plugin.getPlayerManager().getAllPlayers().stream()
                .filter(p -> p.getRole() == PlayerRole.SEEKER && p.isAlive())
                .count();

        GameState state = plugin.getGameManager().getState();

        for (Player player : Bukkit.getOnlinePlayers()) {

            Scoreboard board = boards.computeIfAbsent(player, p -> {
                Scoreboard b = Bukkit.getScoreboardManager().getNewScoreboard();

                var msg = plugin.getMessageManager();

                Objective obj = b.registerNewObjective(
                        "game",
                        "dummy",
                        msg.get("scoreboard.title")
                );

                obj.setDisplaySlot(DisplaySlot.SIDEBAR);

                p.setScoreboard(b);
                return b;
            });

            Objective obj = board.getObjective("game");
            if (obj == null) continue;

            for (String entry : new HashSet<>(board.getEntries())) {
                board.resetScores(entry);
            }

            var msg = plugin.getMessageManager();

            obj.getScore("§8play.sprookjescraft.nl").setScore(0);
            obj.getScore("§8§m────────────§r").setScore(1);

            obj.getScore(msg.get("scoreboard.seekers",
                    Map.of("count", String.valueOf(seekers)))).setScore(2);

            obj.getScore(msg.get("scoreboard.hiders",
                    Map.of("count", String.valueOf(hiders)))).setScore(3);

            obj.getScore("§7 ").setScore(4); // unieke lege regel

            obj.getScore(msg.get("scoreboard.time",
                    Map.of("time", String.valueOf(timeLeft)))).setScore(5);

            obj.getScore(msg.get("scoreboard.status",
                    Map.of("state", formatState(state)))).setScore(6);

            obj.getScore("§8§m────────────").setScore(7);
            obj.getScore("§7Hide & Seek").setScore(8);
        }
    }

    public void clearAll() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
        }
        boards.clear();
    }

    private String formatState(GameState state) {

        var msg = plugin.getMessageManager();

        return switch (state) {
            case WAITING -> msg.get("state.waiting");
            case HIDING -> msg.get("state.hiding");
            case SEEKING -> msg.get("state.seeking");
            case ENDING -> msg.get("state.finished");
        };
    }
}