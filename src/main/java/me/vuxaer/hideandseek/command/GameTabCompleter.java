package me.vuxaer.hideandseek.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;

public class GameTabCompleter implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {

        List<String> completions = new ArrayList<>();

        if (args.length == 1) {

            if (sender.hasPermission("hs.admin")) {
                completions.add("start");
                completions.add("stop");
                completions.add("reload");
            }

            completions.add("help");
        }

        String input = args[0].toLowerCase();

        return completions.stream()
                .filter(s -> s.startsWith(input))
                .toList();
    }
}