package network.discov.core.spigot.command;

import network.discov.core.spigot.Core;
import network.discov.core.spigot.model.CoreExecutor;

public class CoreCommandExecutor extends CoreExecutor {

    public CoreCommandExecutor() {
        super(Core.getInstance().getDescription().getName(), Core.getInstance().getDescription().getVersion(),
                "core", new String[]{"ParrotLync"});
    }
}
