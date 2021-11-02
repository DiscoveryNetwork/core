package network.discov.core.common;

import org.bukkit.command.CommandSender;

import java.util.List;

public abstract class TabArgument {
    private final String name;
    private final boolean required;

    public TabArgument(String name, boolean required) {
        this.name = name;
        this.required = required;
    }

    public abstract List<String> getSuggestions(CommandSender sender);

    public String getName() {
        return name;
    }

    public boolean isRequired() {
        return  required;
    }

    @Override
    public String toString() {
        return name;
    }
}


