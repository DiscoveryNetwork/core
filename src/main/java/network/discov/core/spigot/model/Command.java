package network.discov.core.spigot.model;

import network.discov.core.common.TabArgument;
import network.discov.core.spigot.Core;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
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

        String[] commandArgs = (String[]) ArrayUtils.remove(args, 0);
        if (commandArgs.length < arguments.size()) {
            List<String> missingArguments = new ArrayList<>();
            for (TabArgument argument : new ArrayList<>(arguments.values()).subList(commandArgs.length, arguments.size())) {
                missingArguments.add(argument.getName());
            }
            String message = Core.getInstance().getMessageUtil().get("missing-arguments", missingArguments.toString());
            sender.sendMessage(message);
            return true;
        }

        this.execute(sender, args);
        return true;
    }

    public abstract void execute(CommandSender sender, String[] args);

    public HashMap<Integer, TabArgument> getArguments() {
        return arguments;
    }
}