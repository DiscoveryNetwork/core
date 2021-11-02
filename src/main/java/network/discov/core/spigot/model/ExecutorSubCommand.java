package network.discov.core.spigot.model;

import network.discov.core.common.TabArgument;
import org.bukkit.command.CommandSender;

import java.util.HashMap;

public abstract class ExecutorSubCommand {
    protected String description;
    protected String permission;
    protected HashMap<Integer, TabArgument> arguments = new HashMap<>();

    public ExecutorSubCommand(String description, String permission) {
        this.description = description;
        this.permission = permission;
    }

    public abstract void execute(CommandSender sender, String[] args);

    public String getDescription() {
        return description;
    }

    public String getPermission() {
        return permission;
    }

    public HashMap<Integer, TabArgument> getArguments() {
        return arguments;
    }
}
