package me.vuxaer.hideandseek.command;

import me.vuxaer.hideandseek.HideAndSeekPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.Map;

public class GameCommand implements CommandExecutor {

    private final HideAndSeekPlugin plugin;

    public GameCommand(HideAndSeekPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        var msg = plugin.getMessageManager();

        if (!sender.hasPermission("hs.admin")) {
            sender.sendMessage(msg.get("no_permission"));
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(msg.get("command_usage"));
            return true;
        }

        switch (args[0].toLowerCase()) {

            case "start" -> {
                plugin.getGameManager().startGame();
            }

            case "stop" -> {

                if (!plugin.getGameManager().isGameRunning()) {
                    sender.sendMessage(msg.get("command_no_game"));
                    return true;
                }

                plugin.getGameManager().cancelGame(
                        msg.get("command_stopped_reason")
                );
            }

            default -> sender.sendMessage(msg.get("command_unknown"));
        }

        return true;
    }
}