package network.discov.core.common;

import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.List;

public abstract class TabArgument {
    protected String name;

    public abstract List<String> getSuggestions(CommandSender sender);

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}


class StringTabArgument extends TabArgument {
    private final List<String> options;

    public StringTabArgument(String[] options) {
        this.options = Arrays.asList(options);
    }

    @Override
    public List<String> getSuggestions(CommandSender sender) {
        return options;
    }
}