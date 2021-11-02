package network.discov.core.spigot.command;

import network.discov.core.common.TabArgument;
import network.discov.core.spigot.Core;
import network.discov.core.spigot.model.ExecutorSubCommand;
import org.bukkit.command.CommandSender;

import java.util.List;

public class ReloadCommand extends ExecutorSubCommand {

    public ReloadCommand() {
        super("Reload core or a specific component", "core.command.reload");
        arguments.put(0, new ComponentTabArgument());
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            Core.getInstance().reload();
            sender.sendMessage("§7Core config & components were successfully reloaded!");
            return;
        }

        if (args.length == 1) {
            if (Core.getInstance().reloadComponent(args[0])) {
                sender.sendMessage(String.format("§7Component [§3%s§7] was successfully reloaded!", args[0]));
                return;
            }

            sender.sendMessage("§cSomething went wrong. Please check the console for more information.");
        }
    }
}

class ComponentTabArgument extends TabArgument {

    public ComponentTabArgument() {
        super("component", false);
    }

    @Override
    public List<String> getSuggestions(CommandSender sender) {
        return Core.getInstance().getComponentNames();
    }
}