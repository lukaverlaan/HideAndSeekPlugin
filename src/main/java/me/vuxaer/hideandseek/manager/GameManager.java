package me.vuxaer.hideandseek.manager;

import me.vuxaer.hideandseek.HideAndSeekPlugin;
import me.vuxaer.hideandseek.domain.GamePlayer;
import me.vuxaer.hideandseek.util.GameState;
import me.vuxaer.hideandseek.util.PlayerRole;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GameManager {

    private GameState state = GameState.WAITING;
    private final PlayerManager playerManager;
    private BukkitRunnable gameTimer;

    public GameManager(PlayerManager playerManager) {
        this.playerManager = playerManager;
    }

    public GameState getState() {
        return state;
    }

    public void startGame() {
        Bukkit.broadcastMessage("Game starting!");

        assignTeams();

        state = GameState.HIDING;
        startHideCountdown();
    }

    private void assignTeams() {
        List<GamePlayer> list = new ArrayList<>(playerManager.getAllPlayers());

        Collections.shuffle(list);

        int half = list.size() / 2;

        for (int i = 0; i < list.size(); i++) {
            GamePlayer gp = list.get(i);

            if (i < half) {
                gp.setRole(PlayerRole.SEEKER);
                gp.getPlayer().sendMessage("You are a SEEKER!");
            } else {
                gp.setRole(PlayerRole.HIDER);
                gp.getPlayer().sendMessage("You are a HIDER!");
            }
        }
    }

    private void startHideCountdown() {
        Bukkit.broadcastMessage("Hiders have 60 seconds!");

        new BukkitRunnable() {
            int time = 60;

            @Override
            public void run() {
                if (time <= 0) {
                    cancel();
                    startSeekingPhase();
                    return;
                }

                if (time % 10 == 0 || time <= 5) {
                    Bukkit.broadcastMessage(time + " seconds remaining!");
                }

                time--;
            }
        }.runTaskTimer(HideAndSeekPlugin.getInstance(), 0, 20);
    }

    private void startSeekingPhase() {
        state = GameState.SEEKING;

        Bukkit.broadcastMessage("Seekers can now move!");

        startGameTimer();
    }

    private void startGameTimer() {

        gameTimer = new BukkitRunnable() {
            int time = 180;

            @Override
            public void run() {

                if (state != GameState.SEEKING) {
                    cancel();
                    return;
                }

                if (time <= 0) {
                    cancel();
                    endGameHidersWin();
                    return;
                }

                if (time % 30 == 0 || time <= 10) {
                    Bukkit.broadcastMessage("Game ends in: " + time + "s");
                }

                time--;
            }

        };

        gameTimer.runTaskTimer(HideAndSeekPlugin.getInstance(), 0, 20);
    }

    public void checkWinCondition() {

        long aliveHiders = playerManager.getAllPlayers().stream()
                .filter(p -> p.getRole() == PlayerRole.HIDER)
                .filter(GamePlayer::isAlive)
                .count();

        if (aliveHiders == 0) {
            endGameSeekersWin();
        }
    }

    public void endGameSeekersWin() {
        state = GameState.ENDING;

        stopTimers();

        Bukkit.broadcastMessage("Seekers win!");

        // TODO: POST request
    }

    public void endGameHidersWin() {
        state = GameState.ENDING;

        stopTimers();

        Bukkit.broadcastMessage("Hiders win!");

        // TODO: POST request
    }

    private void stopTimers() {
        if (gameTimer != null) {
            gameTimer.cancel();
        }
    }
}
