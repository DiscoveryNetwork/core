package network.discov.core.spigot.model;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class PlayerTabArgument extends TabArgument {
    public PlayerTabArgument(boolean required) {
        super("player", required);
    }

    @Override
    public List<String> getSuggestions(CommandSender sender) {
        List<String> suggestions = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            suggestions.add(player.getName());
        }
        return suggestions;
    }

    @Override
    public boolean isValid(String arg) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getName().equalsIgnoreCase(arg)) {
                return true;
            }
        }
        return false;
    }
}
