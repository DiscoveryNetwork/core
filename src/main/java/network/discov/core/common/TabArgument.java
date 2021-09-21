package network.discov.core.common;

import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.List;

public abstract class TabArgument {
    private final String name;
    private final String permission;
    private final boolean required;

    public TabArgument(String name, String permission, boolean required) {
        this.name = name;
        this.permission = permission;
        this.required = required;
    }

    public abstract List<String> getSuggestions(CommandSender sender);

    public String getName() {
        return name;
    }

    public String getPermission() {
        return permission;
    }

    public boolean isRequired() {
        return  required;
    }

    @Override
    public String toString() {
        return name;
    }
}


