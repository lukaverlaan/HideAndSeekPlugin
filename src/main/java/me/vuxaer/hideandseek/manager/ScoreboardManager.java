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
        GameState state = plugin.getGameManager().getState();
        var msg = plugin.getMessageManager();

        for (Player player : Bukkit.getOnlinePlayers()) {

            Scoreboard board = boards.computeIfAbsent(player, p -> {
                Scoreboard b = Bukkit.getScoreboardManager().getNewScoreboard();

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

            int score = 15;
            obj.getScore("§7Hide & Seek").setScore(score--);
            obj.getScore("§8§m────────────").setScore(score--);
            obj.getScore(msg.get("scoreboard.status",
                    Map.of("state", formatState(state)))).setScore(score--);
            obj.getScore(msg.get("scoreboard.time",
                    Map.of("time", String.valueOf(timeLeft)))).setScore(score--);
            obj.getScore("§7 ").setScore(score--);
            obj.getScore(msg.get("scoreboard.seekers", Map.of("count", String.valueOf(getAliveSeekers())))).setScore(score--);

            int shownSeekers = 0;
            for (var gp : plugin.getPlayerManager().getAllPlayers()) {
                if (gp.getRole() == PlayerRole.SEEKER && gp.isAlive()) {
                    if (score <= 0 || shownSeekers >= 5) break;
                    String name = "§c" + gp.getPlayer().getName();
                    obj.getScore(name + "§" + score).setScore(score--);
                    shownSeekers++;
                }
            }

            obj.getScore("§0 ").setScore(score--);
            obj.getScore(msg.get("scoreboard.hiders",
                    Map.of("count", String.valueOf(getAliveHiders())))).setScore(score--);;

            int shownHiders = 0;
            for (var gp : plugin.getPlayerManager().getAllPlayers()) {
                if (gp.getRole() == PlayerRole.HIDER) {
                    if (score <= 0 || shownHiders >= 7) break;

                    String name;
                    if (gp.isAlive()) {
                        name = "§a" + gp.getPlayer().getName();
                    } else {
                        name = "§7§m" + gp.getPlayer().getName();
                    }
                    obj.getScore(name + "§" + score).setScore(score--);
                    shownHiders++;
                }
            }
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

    private int getAliveSeekers() {
        return (int) plugin.getPlayerManager().getAllPlayers().stream()
                .filter(p -> p.getRole() == PlayerRole.SEEKER && p.isAlive())
                .count();
    }

    private int getAliveHiders() {
        return (int) plugin.getPlayerManager().getAllPlayers().stream()
                .filter(p -> p.getRole() == PlayerRole.HIDER && p.isAlive())
                .count();
    }
}