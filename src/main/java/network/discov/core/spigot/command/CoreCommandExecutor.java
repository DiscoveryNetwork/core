package network.discov.core.spigot.command;

import network.discov.core.spigot.Core;
import network.discov.core.spigot.model.CommandExecutor;

public class CoreCommandExecutor extends CommandExecutor {

    public CoreCommandExecutor() {
        super(Core.getInstance().getDescription().getName(), Core.getInstance().getDescription().getVersion(),
                "core", new String[]{"ParrotLync"});

        commands.put("reload", new ReloadCommand());
        commands.put("updater", new UpdaterCommand());
        commands.put("install", new InstallCommand());
    }
}
