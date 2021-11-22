package network.discov.core.bungee.model;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import network.discov.core.common.TabArgument;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class PlayerTabArgument extends TabArgument {

    public PlayerTabArgument(String name, boolean required) {
        super(name, required);
    }

    @Override
    public List<String> getSuggestions(CommandSender sender) {
        List<String> suggestions = new ArrayList<>();
        for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
            suggestions.add(player.getName());
        }
        return suggestions;
    }

    @Override
    public boolean isValid(String arg) {
        for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
            if (player.getName().equalsIgnoreCase(arg)) {
                return true;
            }
        }
        return false;
    }
}
