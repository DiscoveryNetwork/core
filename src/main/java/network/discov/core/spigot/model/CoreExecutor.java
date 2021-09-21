package network.discov.core.spigot.model;

import network.discov.core.common.TabArgument;
import network.discov.core.spigot.Core;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;

public abstract class CoreExecutor implements TabExecutor {
    protected final TreeMap<String, CoreCommand> commands = new TreeMap<>();
    private final String name;
    private final String version;
    private final String main;
    private final String[] developers;

    public CoreExecutor(String name, String version, String main, String[] developers) {
        this.name = name;
        this.version = version;
        this.main = main;
        this.developers = developers;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            String message = String.format("§6%s-%s §7by %s | Use /%s help", name, version, Arrays.toString(developers), main);
            sender.sendMessage(message);
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("help")) {
            return help(sender);
        }

        CoreCommand coreCommand = commands.get(args[0]);
        if (coreCommand == null) {
            sender.sendMessage(Core.getInstance().getMessageUtil().get("subcommand-not-found", main));
            return true;
        }

        if (!sender.hasPermission(coreCommand.getPermission())) {
            sender.sendMessage(Core.getInstance().getMessageUtil().get("no-permission"));
            return true;
        }

        String[] commandArgs = (String[]) ArrayUtils.remove(args, 0);
        if (commandArgs.length < coreCommand.getArguments().size()) {
            List<String> missingArguments = new ArrayList<>();
            for (TabArgument argument : new ArrayList<>(coreCommand.getArguments().values()).subList(commandArgs.length, coreCommand.getArguments().size())) {
                missingArguments.add(argument.getName());
            }
            sender.sendMessage(Core.getInstance().getMessageUtil().get("missing-arguments", Arrays.toString(missingArguments.toArray())));
            return true;
        }

        coreCommand.execute(sender, commandArgs);
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        List<String> suggestions = new ArrayList<>();

        if (args.length == 1) {
            suggestions.add("help");
            for (String commandName : commands.keySet()) {
                if (sender.hasPermission(commands.get(commandName).getPermission())) {
                    suggestions.add(commandName);
                }
            }
            StringUtil.copyPartialMatches(args[0], suggestions, new ArrayList<>());
        }

        if (args.length > 1) {
            CoreCommand coreCommand = commands.get(args[0]);
            if (coreCommand != null && sender.hasPermission(coreCommand.getPermission())) {
                TabArgument argument = coreCommand.getArguments().get(args.length - 2);
                if (argument != null) {
                    suggestions.addAll(argument.getSuggestions(sender));
                }
            }
            StringUtil.copyPartialMatches(args[args.length - 1], suggestions, new ArrayList<>());
        }

        return suggestions;
    }

    private boolean help(CommandSender sender) {
        sender.sendMessage(String.format("§f+---+ §9%s §f+---+", name));
        int allowed = 0;
        for (String commandName : commands.keySet()) {
            CoreCommand coreCommand = commands.get(commandName);
            if (sender.hasPermission(coreCommand.getPermission())) {
                allowed++;
                StringBuilder args = new StringBuilder();
                for (TabArgument argument : coreCommand.getArguments().values()) {
                    args.append(String.format(" <%s>", argument.getName()));
                }
                sender.sendMessage(String.format("§3/%s %s%s - §7%s", main, commandName, args, coreCommand.getDescription()));
            }
        }

        if (allowed == 0) {
            sender.sendMessage(Core.getInstance().getMessageUtil().get("no-permission"));
        }
        return true;
    }
}
