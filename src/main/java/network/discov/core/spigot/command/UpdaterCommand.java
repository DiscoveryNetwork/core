package network.discov.core.spigot.command;

import network.discov.core.common.StringTabArgument;
import network.discov.core.spigot.Core;
import network.discov.core.spigot.model.ExecutorSubCommand;
import network.discov.core.spigot.model.PluginUpdaterTask;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.Arrays;

public class UpdaterCommand extends ExecutorSubCommand {

    public UpdaterCommand() {
        super("Run the core updater", "core.command.updater");
        arguments.put(0, new StringTabArgument("args", false, new String[]{"--forceSnapshots"}));
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        boolean forceSnapshots = Arrays.asList(args).contains("--forceSnapshots");

        sender.sendMessage("§7Starting plugin updater...");
        Bukkit.getScheduler().runTaskAsynchronously(Core.getInstance(), new PluginUpdaterTask(forceSnapshots));

        sender.sendMessage("§7Starting component updater...");
        Core.getInstance().updateComponents(forceSnapshots);

        sender.sendMessage("§aUpdaters are running. Please refer to the console for more information...");
    }
}