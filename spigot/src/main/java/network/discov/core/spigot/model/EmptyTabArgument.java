package network.discov.core.spigot.model;

import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class EmptyTabArgument extends TabArgument {
    public EmptyTabArgument(String name, boolean required) {
        super(name, required);
    }

    @Override
    public List<String> getSuggestions(CommandSender sender) {
        return new ArrayList<>();
    }
}
