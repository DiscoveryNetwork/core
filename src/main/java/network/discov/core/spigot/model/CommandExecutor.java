package network.discov.core.spigot.model;

import network.discov.core.common.TabArgument;
import network.discov.core.spigot.Core;
import network.discov.core.spigot.util.ArgsValidator;
import network.discov.core.spigot.util.TabCompleter;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;

public abstract class CommandExecutor implements TabExecutor {
    protected final TreeMap<String, ExecutorSubCommand> commands = new TreeMap<>();
    private final String name;
    private final String version;
    private final String main;
    private final String[] developers;

    public CommandExecutor(String name, String version, String main, String[] developers) {
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

        // Check if subcommand is valid
        ExecutorSubCommand subCommand = commands.get(args[0]);
        if (subCommand == null) {
            sender.sendMessage(Core.getInstance().getMessageUtil().get("subcommand-not-found", main));
            return true;
        }

        // Permission check
        if (!sender.hasPermission(subCommand.getPermission())) {
            sender.sendMessage(Core.getInstance().getMessageUtil().get("no-permission"));
            return true;
        }

        // Check if there are missing arguments & call validators
        String[] commandArgs = (String[]) ArrayUtils.remove(args, 0);
        if (!ArgsValidator.checkArgs(sender, subCommand.getArguments(), commandArgs)) {
            return true;
        }

        // Execute the command
        subCommand.execute(sender, commandArgs);
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        return TabCompleter.getSuggestions(sender, args, commands);
    }

    private boolean help(CommandSender sender) {
        sender.sendMessage(String.format("§f+---+ §9%s §f+---+", name));
        int allowed = 0;
        for (String commandName : commands.keySet()) {
            ExecutorSubCommand subCommand = commands.get(commandName);
            if (sender.hasPermission(subCommand.getPermission())) {
                allowed++;
                StringBuilder args = new StringBuilder();
                for (TabArgument argument : subCommand.getArguments().values()) {
                    args.append(String.format(" <%s>", argument.getName()));
                }
                sender.sendMessage(String.format("§3/%s %s%s - §7%s", main, commandName, args, subCommand.getDescription()));
            }
        }

        if (allowed == 0) {
            sender.sendMessage(Core.getInstance().getMessageUtil().get("no-permission"));
        }
        return true;
    }
}
