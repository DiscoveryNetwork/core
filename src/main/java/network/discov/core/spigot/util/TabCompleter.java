package network.discov.core.spigot.util;

import network.discov.core.common.TabArgument;
import network.discov.core.spigot.model.ExecutorSubCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class TabCompleter {
    @NotNull
    public static List<String> getSuggestions(@NotNull CommandSender sender, @NotNull String[] args, TreeMap<String, ExecutorSubCommand> commands) {
        List<String> suggestions = new ArrayList<>();

        if (args.length == 1) {
            suggestions.add("help");
            for (String commandName : commands.keySet()) {
                if (sender.hasPermission(commands.get(commandName).getPermission())) {
                    suggestions.add(commandName);
                }
            }
            return StringUtil.copyPartialMatches(args[0], suggestions, new ArrayList<>());
        }

        if (args.length > 1) {
            ExecutorSubCommand subCommand = commands.get(args[0]);
            if (subCommand != null && sender.hasPermission(subCommand.getPermission())) {
                TabArgument argument = subCommand.getArguments().get(args.length - 2);
                if (argument != null) {
                    suggestions.addAll(argument.getSuggestions(sender));
                }
            }
           return StringUtil.copyPartialMatches(args[args.length - 1], suggestions, new ArrayList<>());
        }

        return suggestions;
    }
}
