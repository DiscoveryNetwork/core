package network.discov.core.bungee.model;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.jetbrains.annotations.Nullable;

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
    public @Nullable String validate(String arg) {
        for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
            if (player.getName().equalsIgnoreCase(arg)) {
                return null;
            }
        }
        return "Player not found.";
    }
}
