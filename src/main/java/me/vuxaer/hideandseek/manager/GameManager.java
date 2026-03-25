package me.vuxaer.hideandseek.manager;

import me.vuxaer.hideandseek.HideAndSeekPlugin;
import me.vuxaer.hideandseek.domain.GamePlayer;
import me.vuxaer.hideandseek.util.GameState;
import me.vuxaer.hideandseek.util.PlayerRole;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GameManager {

    private GameState state = GameState.WAITING;
    private final PlayerManager playerManager;
    private BukkitRunnable hideTimer;
    private BukkitRunnable gameTimer;

    public GameManager(PlayerManager playerManager) {
        this.playerManager = playerManager;
    }

    public GameState getState() {
        return state;
    }

    public void startGame() {

        if (isGameRunning()) {
            return;
        }

        if (playerManager.getAllPlayers().size() < 2) {
            Bukkit.broadcastMessage("Not enough players to start!");
            return;
        }

        Bukkit.broadcastMessage("Game starting!");

        assignTeams();

        state = GameState.HIDING;
        startHideCountdown();
    }

    public void resetGame() {

        stopTimers();

        var plugin = HideAndSeekPlugin.getInstance();

        for (GamePlayer gp : playerManager.getAllPlayers()) {

            Player player = gp.getPlayer();

            var disguise = plugin.getDisguiseManager().getDisguiseByPlayer(player);
            if (disguise != null) {
                plugin.getDisguiseManager().removeDisguise(disguise);
            }

            gp.reset();
            gp.setAlive(true);
        }

        state = GameState.WAITING;
    }

    private void assignTeams() {
        List<GamePlayer> list = new ArrayList<>(playerManager.getAllPlayers());

        Collections.shuffle(list);

        int half = list.size() / 2;

        for (int i = 0; i < list.size(); i++) {
            GamePlayer gp = list.get(i);

            gp.reset();

            if (i < half) {
                gp.setRole(PlayerRole.SEEKER);
                gp.getPlayer().sendMessage("You are a SEEKER!");

            } else {
                gp.setRole(PlayerRole.HIDER);
                gp.getPlayer().sendMessage("You are a HIDER!");

                Material material = Math.random() < 0.5
                        ? Material.OAK_PLANKS
                        : Material.STONE;

                HideAndSeekPlugin.getInstance()
                        .getDisguiseManager()
                        .disguise(gp.getPlayer(), material);
            }
        }
    }

    private void startHideCountdown() {

        if (hideTimer != null) {
            hideTimer.cancel();
        }

        hideTimer = new BukkitRunnable() {
            int time = 60;

            @Override
            public void run() {

                if (state != GameState.HIDING) {
                    cancel();
                    return;
                }

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
        };

        hideTimer.runTaskTimer(HideAndSeekPlugin.getInstance(), 0, 20);
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

        long aliveSeekers = playerManager.getAllPlayers().stream()
                .filter(p -> p.getRole() == PlayerRole.SEEKER)
                .filter(GamePlayer::isAlive)
                .count();

        if (aliveHiders == 0) {
            endGameSeekersWin();
        } else if (aliveSeekers == 0) {
            endGameHidersWin();
        }
    }

    public void endGameSeekersWin() {
        state = GameState.ENDING;

        stopTimers();

        Bukkit.broadcastMessage("Seekers win!");

        // TODO: POST request

        Bukkit.getScheduler().runTaskLater(
                HideAndSeekPlugin.getInstance(),
                this::resetGame,
                100
        );
    }

    public void endGameHidersWin() {
        state = GameState.ENDING;

        stopTimers();

        Bukkit.broadcastMessage("Hiders win!");

        // TODO: POST request

        Bukkit.getScheduler().runTaskLater(
                HideAndSeekPlugin.getInstance(),
                this::resetGame,
                100
        );
    }

    private void stopTimers() {

        if (gameTimer != null) {
            gameTimer.cancel();
            gameTimer = null;
        }

        if (hideTimer != null) {
            hideTimer.cancel();
            hideTimer = null;
        }
    }

    public void cancelGame(String reason) {

        if (!isGameRunning()) return;

        state = GameState.ENDING;

        stopTimers();

        Bukkit.broadcastMessage("Game cancelled: " + reason);

        Bukkit.getScheduler().runTaskLater(
                HideAndSeekPlugin.getInstance(),
                this::resetGame,
                40
        );
    }

    public boolean isGameRunning() {
        return state == GameState.HIDING || state == GameState.SEEKING;
    }
}
