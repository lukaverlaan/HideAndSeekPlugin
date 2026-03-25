package me.vuxaer.hideandseek.manager;

import me.vuxaer.hideandseek.HideAndSeekPlugin;
import me.vuxaer.hideandseek.domain.GamePlayer;
import me.vuxaer.hideandseek.util.GameState;
import me.vuxaer.hideandseek.util.PlayerRole;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.util.HashMap;
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
                Objective obj = b.registerNewObjective("game", "dummy", "§aHide & Seek");
                obj.setDisplaySlot(DisplaySlot.SIDEBAR);
                p.setScoreboard(b);
                return b;
            });

            Objective obj = board.getObjective("game");

            for (String entry : board.getEntries()) {
                board.resetScores(entry);
            }

            String timeLine = "§fTime remaining: §e" + timeLeft + "s";

            obj.getScore("§7").setScore(4);
            obj.getScore("§fState: §a" + state.name()).setScore(3);
            obj.getScore(timeLine).setScore(2);
            obj.getScore("§fHiders: §a" + hiders).setScore(1);
            obj.getScore("§fSeekers: §c" + seekers).setScore(0);
        }
    }

    public void clearAll() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
        }
        boards.clear();
    }
}