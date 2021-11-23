package network.discov.core.spigot.model;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.Nullable;

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

    /**
     * Validates a string for this TabArgument
     * @param arg The argument to validate
     * @return null if argument is valid, error message when invalid
     */
    public @Nullable String validate(String arg) {
        return null;
    }

    @Override
    public String toString() {
        return name;
    }
}


