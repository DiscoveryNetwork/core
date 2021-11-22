package network.discov.core.spigot.model;

import network.discov.core.common.TabArgument;
import network.discov.core.spigot.Core;
import network.discov.core.spigot.util.ArgsValidator;
import network.discov.core.spigot.util.TabCompleter;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;

public abstract class ComponentCommandExecutor extends BukkitCommand {
    protected final TreeMap<String, ExecutorSubCommand> commands = new TreeMap<>();
    protected final SpigotComponent component;

    public ComponentCommandExecutor(String name, String description, String[] aliases, SpigotComponent component) {
        super(name);
        this.description = description;
        this.component = component;
        this.setAliases(Arrays.asList(aliases));
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String s, @NotNull String[] args) {
        if (args.length == 0) {
            String message = String.format("§6%s-%s §7by %s | Use /%s help", component.getName(), component.getVersion(), component.getDevelopers(), getName());
            sender.sendMessage(message);
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("help")) {
            return help(sender);
        }

        // Check if the given subcommand is valid
        ExecutorSubCommand subCommand = commands.get(args[0]);
        if (subCommand == null) {
            sender.sendMessage(Core.getInstance().getMessageUtil().get("subcommand-not-found", getName()));
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

        subCommand.execute(sender, commandArgs);
        return true;
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) {
        return TabCompleter.getSuggestions(sender, args, commands);
    }

    private boolean help(@NotNull CommandSender sender) {
        sender.sendMessage(String.format("§f+---+ §9%s §f+---+", getName()));
        int allowed = 0;
        for (String commandName : commands.keySet()) {
            ExecutorSubCommand subCommand = commands.get(commandName);
            if (sender.hasPermission(subCommand.getPermission())) {
                allowed++;
                StringBuilder args = new StringBuilder();
                for (TabArgument argument : subCommand.getArguments().values()) {
                    args.append(String.format(" <%s>", argument.getName()));
                }
                sender.sendMessage(String.format("§3/%s %s%s - §7%s", getName(), commandName, args, subCommand.getDescription()));
            }
        }

        if (allowed == 0) {
            sender.sendMessage(Core.getInstance().getMessageUtil().get("no-permission"));
        }
        return true;
    }
}