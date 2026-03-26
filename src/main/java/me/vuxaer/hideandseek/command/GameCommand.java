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

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {

            case "help" -> {
                sendHelp(sender);
            }

            case "start" -> {

                if (!sender.hasPermission("hs.admin")) {
                    sender.sendMessage(msg.get("no_permission"));
                    return true;
                }

                if (plugin.getGameManager().isGameRunning()) {
                    sender.sendMessage(msg.get("command_game_running"));
                    return true;
                }

                plugin.getGameManager().startGame();
            }

            case "stop" -> {

                if (!sender.hasPermission("hs.admin")) {
                    sender.sendMessage(msg.get("no_permission"));
                    return true;
                }

                if (!plugin.getGameManager().isGameRunning()) {
                    sender.sendMessage(msg.get("command_no_game"));
                    return true;
                }

                plugin.getGameManager().cancelGame(
                        msg.get("command_stopped_reason")
                );
            }

            case "reload" -> {

                if (!sender.hasPermission("hs.admin")) {
                    sender.sendMessage(msg.get("no_permission"));
                    return true;
                }

                plugin.reloadConfig();
                plugin.getMessageManager().load();

                sender.sendMessage(msg.get("command_reload"));
            }

            default -> sendHelp(sender);
        }

        return true;
    }

    private void sendHelp(CommandSender sender) {

        var msg = plugin.getMessageManager();

        sender.sendMessage(" ");
        sender.sendMessage(msg.get("command_help_header"));
        sender.sendMessage(" ");

        if (sender.hasPermission("hs.admin")) {
            sender.sendMessage(msg.get("command_help_start"));
            sender.sendMessage(msg.get("command_help_stop"));
            sender.sendMessage(msg.get("command_help_reload"));
        }

        sender.sendMessage(" ");
    }
}