package network.discov.core.common;

import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.List;

public class StringTabArgument extends TabArgument {
    private final List<String> options;

    public StringTabArgument(String name, String permission, boolean required, String[] options) {
        super(name, permission, required);
        this.options = Arrays.asList(options);
    }

    @Override
    public List<String> getSuggestions(CommandSender sender) {
        return options;
    }
}
