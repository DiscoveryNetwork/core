package network.discov.core.spigot.model;

import network.discov.core.common.TabArgument;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class PlayerTabArgument extends TabArgument {
    public PlayerTabArgument(String permission, boolean required) {
        super("player", permission, required);
    }

    @Override
    public List<String> getSuggestions(CommandSender sender) {
        List<String> suggestions = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            suggestions.add(player.getName());
        }
        return suggestions;
    }
}
