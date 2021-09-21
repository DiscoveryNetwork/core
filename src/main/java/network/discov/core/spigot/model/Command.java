package network.discov.core.spigot.model;

import network.discov.core.common.CommonUtils;
import network.discov.core.common.TabArgument;
import network.discov.core.spigot.Core;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public abstract class Command extends BukkitCommand {
    protected HashMap<Integer, TabArgument> arguments = new HashMap<>();

    public Command(String name, String description, String permission, String[] aliases) {
        super(name);
        this.description = description;
        this.setAliases(Arrays.asList(aliases));
        if (permission != null) { this.setPermission(permission); }
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String s, @NotNull String[] args) {
        if (!sender.hasPermission(Objects.requireNonNull(getPermission()))) {
            sender.sendMessage(Core.getInstance().getMessageUtil().get("no-permission"));
            return true;
        }

        List<String> missingArguments = CommonUtils.getMissingArguments(args, arguments);
        if (missingArguments.size() != 0) {
            sender.sendMessage(CommonUtils.getArgsMessage(missingArguments, Core.getInstance().getMessageUtil()));
            return true;
        }

        this.executeCommand(sender, args);
        return true;
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) {
        List<String> suggestions = new ArrayList<>();

        if (arguments.containsKey(args.length - 1)) {
            TabArgument argument = arguments.get(args.length - 1);
            if (argument.getPermission() != null && !sender.hasPermission(argument.getPermission())) { return suggestions; }
            suggestions.addAll(argument.getSuggestions(sender));
            return StringUtil.copyPartialMatches(args[0], suggestions, new ArrayList<>());
        }

        return suggestions;
    }

    public abstract void executeCommand(CommandSender sender, String[] args);

}