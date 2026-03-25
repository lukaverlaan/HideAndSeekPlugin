package me.vuxaer.hideandseek.manager;

import me.vuxaer.hideandseek.HideAndSeekPlugin;
import me.vuxaer.hideandseek.domain.GamePlayer;
import me.vuxaer.hideandseek.domain.GameResult;
import me.vuxaer.hideandseek.gui.BlockSelector;
import me.vuxaer.hideandseek.util.GameState;
import me.vuxaer.hideandseek.util.PlayerRole;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class GameManager {

    private static final String SEEKERS = "SEEKERS";
    private static final String HIDERS = "HIDERS";

    private final Set<UUID> selectedHiders = new HashSet<>();

    private final HideAndSeekPlugin plugin = HideAndSeekPlugin.getInstance();
    private final PlayerManager playerManager;

    private GameState state = GameState.WAITING;

    private BukkitRunnable hideTimer;
    private BukkitRunnable gameTimer;

    private long gameStartTime;

    public GameManager(PlayerManager playerManager) {
        this.playerManager = playerManager;
    }

    public GameState getState() {
        return state;
    }

    public void startGame() {
        selectedHiders.clear();

        if (state != GameState.WAITING) return;

        if (playerManager.getAllPlayers().size() < 2) {
            Bukkit.broadcastMessage("Not enough players to start!");
            return;
        }

        Bukkit.broadcastMessage("Game starting!");

        assignTeams();
    }

    public void resetGame() {

        stopTimers();

        for (GamePlayer gp : playerManager.getAllPlayers()) {

            Player player = gp.getPlayer();

            var disguise = plugin.getDisguiseManager().getDisguiseByPlayer(player);
            if (disguise != null) {
                plugin.getDisguiseManager().removeDisguise(disguise);
            }

            gp.reset();
            gp.setAlive(true);
        }

        plugin.getScoreboardManager().clearAll();

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

                selectedHiders.add(gp.getPlayer().getUniqueId());

                BlockSelector.open(gp.getPlayer());
            }
        }
    }

    private void startHideCountdown() {

        if (hideTimer != null) hideTimer.cancel();

        hideTimer = new BukkitRunnable() {

            int time = 60;

            @Override
            public void run() {

                plugin.getScoreboardManager().updateAll(time);

                if (state != GameState.HIDING) {
                    cancel();
                    return;
                }

                if (time <= 0) {
                    cancel();
                    startSeekingPhase();
                    return;
                }

                if (time == 60 || time == 30 || time == 10 || time <= 5) {
                    Bukkit.broadcastMessage("§e" + time + " seconds remaining");
                }

                if (time <= 5) {
                    Bukkit.getOnlinePlayers().forEach(p ->
                            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1)
                    );
                }

                time--;
            }
        };

        hideTimer.runTaskTimer(plugin, 0, 20);
    }

    private void startSeekingPhase() {

        state = GameState.SEEKING;
        gameStartTime = System.currentTimeMillis();

        Bukkit.broadcastMessage("Seekers can now move!");

        startGameTimer();
    }

    private void startGameTimer() {

        gameTimer = new BukkitRunnable() {

            int time = 180;

            @Override
            public void run() {

                plugin.getScoreboardManager().updateAll(time);

                if (state != GameState.SEEKING) {
                    cancel();
                    return;
                }

                if (time <= 0) {
                    cancel();
                    endGame(HIDERS);
                    return;
                }

                time--;
            }
        };

        gameTimer.runTaskTimer(plugin, 0, 20);
    }

    public void handleHit(Player attacker, Player victim) {

        GamePlayer attackerGP = playerManager.getPlayer(attacker);
        GamePlayer victimGP = playerManager.getPlayer(victim);

        if (attackerGP == null || victimGP == null) return;

        if (attackerGP.getRole() != PlayerRole.SEEKER ||
                victimGP.getRole() != PlayerRole.HIDER) return;

        if (!victimGP.canBeHit()) return;

        victimGP.registerHit();

        attacker.playSound(attacker.getLocation(), Sound.ENTITY_PLAYER_ATTACK_STRONG, 1, 1);
        victim.playSound(victim.getLocation(), Sound.ENTITY_PLAYER_HURT, 1, 1);

        victim.getWorld().spawnParticle(
                Particle.CRIT,
                victim.getLocation().add(0, 1, 0),
                10
        );

        if (!victimGP.isDead()) {
            victim.sendMessage("§cYou got hit! (§f" + victimGP.getHits() + "/3§c)");
            return;
        }

        victimGP.setAlive(false);

        victim.sendMessage("§cYou are eliminated!");
        victim.setGameMode(GameMode.SPECTATOR);

        int remaining = (int) playerManager.getAllPlayers().stream()
                .filter(p -> p.getRole() == PlayerRole.HIDER)
                .filter(GamePlayer::isAlive)
                .count();

        String message = remaining > 0
                ? " §7(" + remaining + " hiders left)"
                : "";

        Bukkit.broadcastMessage("§c" + attacker.getName()
                + " §fhas found §a" + victim.getName() + message);

        checkWinCondition();
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
            endGame(SEEKERS);
        } else if (aliveSeekers == 0) {
            endGame(HIDERS);
        }
    }

    private void endGame(String winner) {

        state = GameState.ENDING;
        stopTimers();

        long duration = (System.currentTimeMillis() - gameStartTime) / 1000;

        GameResult result = buildResult(winner, duration);

        if (winner.equals(SEEKERS)) {
            Bukkit.broadcastMessage("§cSeekers have won the game!");
        } else {
            Bukkit.broadcastMessage("§aHiders have won the game!");
        }

        sendResult(result);

        Bukkit.getScheduler().runTaskLater(plugin, this::resetGame, 100);
    }

    private void sendResult(GameResult result) {

        String endpoint = plugin.getConfig().getString("endpoint");

        if (endpoint == null || endpoint.isEmpty()) {
            plugin.getLogger().warning("No endpoint configured!");
            return;
        }

        plugin.getHttpService().sendGameResult(endpoint, result);
    }

    private GameResult buildResult(String winner, long duration) {

        var seekers = playerManager.getAllPlayers().stream()
                .filter(p -> p.getRole() == PlayerRole.SEEKER)
                .map(GamePlayer::getPlayer)
                .toList();

        var hiders = playerManager.getAllPlayers().stream()
                .filter(p -> p.getRole() == PlayerRole.HIDER)
                .map(GamePlayer::getPlayer)
                .toList();

        return new GameResult(seekers, hiders, winner, duration);
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

        Bukkit.getScheduler().runTaskLater(plugin, this::resetGame, 40);
    }

    public boolean isGameRunning() {
        return state == GameState.HIDING || state == GameState.SEEKING;
    }

    public void onHiderSelected(Player player) {

        selectedHiders.remove(player.getUniqueId());

        if (selectedHiders.isEmpty()) {
            Bukkit.broadcastMessage("All hiders are ready!");

            state = GameState.HIDING;
            startHideCountdown();
        }
    }
}