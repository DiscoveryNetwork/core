package network.discov.core.spigot.model;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public class StringTabArgument extends TabArgument {
    private final List<String> options;

    public StringTabArgument(String name, boolean required, String[] options) {
        super(name, required);
        this.options = Arrays.asList(options);
    }

    @Override
    public @Nullable String validate(String arg) {
        if (required && !options.contains(arg)) {
            return "The given argument is not a valid option.";
        }

        return null;
    }

    @Override
    public List<String> getSuggestions(CommandSender sender) {
        return options;
    }
}
