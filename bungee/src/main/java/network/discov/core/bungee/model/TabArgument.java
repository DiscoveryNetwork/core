package network.discov.core.bungee.model;

import net.md_5.bungee.api.CommandSender;

import java.util.List;

public abstract class TabArgument {
    private final String name;
    protected final boolean required;

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

    public boolean isValid(String arg) {
        return true;
    }

    @Override
    public String toString() {
        return name;
    }
}


