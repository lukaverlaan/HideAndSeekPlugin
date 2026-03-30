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
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
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

    public boolean startGame() {

        selectedHiders.clear();

        if (state != GameState.WAITING) return false;

        if (Bukkit.getOnlinePlayers().size() < 2) {
            Bukkit.broadcastMessage(plugin.getMessageManager().get("not_enough_players"));
            return false;
        }

        Bukkit.broadcastMessage(plugin.getMessageManager().get("game_starting"));

        assignTeams();
        return true;
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

        int seekersAmount  = Math.max(1, list.size() / 3);

        for (int i = 0; i < list.size(); i++) {

            GamePlayer gp = list.get(i);
            gp.reset();

            if (i < seekersAmount ) {
                gp.setRole(PlayerRole.SEEKER);

                Player player = gp.getPlayer();
                player.setGameMode(GameMode.SURVIVAL);

                gp.getPlayer().sendMessage(
                        plugin.getMessageManager().get("you_are_seeker")
                );
            } else {
                gp.setRole(PlayerRole.HIDER);
                gp.getPlayer().sendMessage(
                        plugin.getMessageManager().get("you_are_hider")
                );

                selectedHiders.add(gp.getPlayer().getUniqueId());

                BlockSelector.open(gp.getPlayer());
            }
        }
    }

    private void startHideCountdown() {

        if (hideTimer != null) hideTimer.cancel();

        for (GamePlayer gp : playerManager.getAllPlayers()) {
            if (gp.getRole() == PlayerRole.SEEKER) {
                gp.getPlayer().addPotionEffect(
                        new PotionEffect(PotionEffectType.BLINDNESS, 20 * 60, 1, false, false)
                );
            }
        }

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
                    var msg = plugin.getMessageManager();

                    String timeMsg = msg.getTime("second_remaining", "seconds_remaining", time);
                    Bukkit.broadcastMessage(timeMsg);
                }

                if (time <= 5 && time > 0) {
                    for (Player p : Bukkit.getOnlinePlayers()) {

                        p.sendTitle(
                                "§e§l" + time,
                                plugin.getMessageManager().get("get_ready"),
                                0, 20, 0
                        );

                        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1.5f);
                    }
                }

                time--;
            }
        };

        hideTimer.runTaskTimer(plugin, 0, 20);
    }

    private void startSeekingPhase() {

        state = GameState.SEEKING;
        gameStartTime = System.currentTimeMillis();

        for (GamePlayer gp : playerManager.getAllPlayers()) {
            gp.getPlayer().removePotionEffect(PotionEffectType.BLINDNESS);
        }

        Bukkit.broadcastMessage(
                plugin.getMessageManager().get("seekers_move")
        );

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendTitle("§a§lGO!", "", 5, 40, 10);
            p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
        }

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

        var disguise = plugin.getDisguiseManager().getDisguiseByPlayer(victim);

        if (disguise != null && !disguise.isSolid()) {

            var direction = victim.getLocation().toVector()
                    .subtract(attacker.getLocation().toVector())
                    .normalize();

            direction.setY(0.35);

            victim.setVelocity(direction.multiply(0.4));

            attacker.playSound(attacker.getLocation(),
                    Sound.ENTITY_PLAYER_ATTACK_KNOCKBACK, 1, 1);
        }

        victimGP.registerHit();

        attacker.playSound(attacker.getLocation(), Sound.ENTITY_PLAYER_ATTACK_STRONG, 1, 1);
        victim.playSound(victim.getLocation(), Sound.ENTITY_PLAYER_HURT, 1, 1);

        victim.getWorld().spawnParticle(
                Particle.CRIT,
                victim.getLocation().add(0, 1, 0),
                10
        );

        if (!victimGP.isDead()) {
            victim.sendMessage(
                    plugin.getMessageManager().get("you_got_hit",
                            Map.of("hits", String.valueOf(victimGP.getHits())))
            );
            return;
        }

        if (disguise != null) {
            plugin.getDisguiseManager().removeDisguise(disguise);
        }

        victimGP.setAlive(false);

        victim.sendMessage(
                plugin.getMessageManager().get("you_eliminated")
        );

        victim.playSound(victim.getLocation(), Sound.ENTITY_WITHER_DEATH, 1, 1);
        victim.getWorld().spawnParticle(Particle.EXPLOSION_NORMAL, victim.getLocation(), 1);
        victim.setGameMode(GameMode.SPECTATOR);

        int remaining = (int) playerManager.getAllPlayers().stream()
                .filter(p -> p.getRole() == PlayerRole.HIDER)
                .filter(GamePlayer::isAlive)
                .count();

        String base = plugin.getMessageManager().get("player_found",
                Map.of(
                        "attacker", attacker.getName(),
                        "victim", victim.getName()
                )
        );

        String extra = "";
        if (remaining > 0) {
            String key = (remaining == 1) ? "hider_left" : "hiders_left";

            extra = plugin.getMessageManager().get(
                    key,
                    Map.of("count", String.valueOf(remaining))
            );
        }

        Bukkit.broadcastMessage(base + extra);

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

        plugin.getScoreboardManager().updateAll(0);

        long duration = (System.currentTimeMillis() - gameStartTime) / 1000;
        GameResult result = buildResult(winner, duration);

        if (winner.equals(SEEKERS)) {
            Bukkit.broadcastMessage(plugin.getMessageManager().get("seekers_win"));
        } else {
            Bukkit.broadcastMessage(plugin.getMessageManager().get("hiders_win"));
        }

        for (Player p : Bukkit.getOnlinePlayers()) {

            p.sendTitle(
                    "§6§lGAME OVER",
                    winner.equals(SEEKERS) ? plugin.getMessageManager().get("seekers_win_endtitle") : plugin.getMessageManager().get("hiders_win_endtitle"),
                    10, 60, 10
            );

            p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1, 1);
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

        Bukkit.broadcastMessage(
                plugin.getMessageManager().get("game_cancelled", Map.of("reason", reason))
        );

        Bukkit.getScheduler().runTaskLater(plugin, this::resetGame, 40);
    }

    public boolean isGameRunning() {
        return state == GameState.HIDING || state == GameState.SEEKING;
    }

    public void onHiderSelected(Player player) {

        selectedHiders.remove(player.getUniqueId());

        if (selectedHiders.isEmpty()) {

            Bukkit.broadcastMessage(
                    plugin.getMessageManager().get("all_hiders_ready")
            );

            state = GameState.HIDING;
            startHideCountdown();
        }
    }
}