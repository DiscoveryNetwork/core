package network.discov.core.spigot.command;

import network.discov.core.commons.utils.NexusClient;
import network.discov.core.commons.exception.ArtifactNotFoundException;
import network.discov.core.commons.exception.InvalidResponseCodeException;
import network.discov.core.spigot.Core;
import network.discov.core.spigot.model.ExecutorSubCommand;
import network.discov.core.spigot.model.TabArgument;
import org.bukkit.command.CommandSender;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.util.List;

public class InstallCommand extends ExecutorSubCommand {

    public InstallCommand() {
        super("Install a component", "core.command.install");
        arguments.put(0, new NexusComponentTabArgument());
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        String authString = Core.getInstance().getConfig().getString("nexus-auth");
        try {
            String path = NexusClient.downloadFile("plugins/DiscovCore/components", "core-components", args[0], authString);
            if (Core.getInstance().loadComponent(path)) {
                sender.sendMessage(String.format("Component [%s] was successfully installed!", args[0]));
            } else {
                sender.sendMessage("§cError occurred. Please refer to the console for more information.");
            }
        } catch (IOException | ParseException | ArtifactNotFoundException | InvalidResponseCodeException e) {
            e.printStackTrace();
            sender.sendMessage("§cError occurred. Please refer to the console for more information.");
        }
    }
}

class NexusComponentTabArgument extends TabArgument {

    public NexusComponentTabArgument() {
        super("component", true);
    }

    @Override
    public List<String> getSuggestions(CommandSender sender) {
        return Core.getInstance().getAvailableComponents();
    }
}
