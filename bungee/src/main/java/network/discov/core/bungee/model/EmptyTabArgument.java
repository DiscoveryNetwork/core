package network.discov.core.bungee.model;

import net.md_5.bungee.api.CommandSender;

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
