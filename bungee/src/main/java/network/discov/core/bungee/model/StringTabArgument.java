package network.discov.core.bungee.model;

import net.md_5.bungee.api.CommandSender;

import java.util.Arrays;
import java.util.List;

public class StringTabArgument extends TabArgument {
    private final List<String> options;

    public StringTabArgument(String name, boolean required, String[] options) {
        super(name, required);
        this.options = Arrays.asList(options);
    }

    @Override
    public boolean isValid(String arg) {
        if (!required) {
            return true;
        }

        return options.contains(arg);
    }

    @Override
    public List<String> getSuggestions(CommandSender sender) {
        return options;
    }
}
