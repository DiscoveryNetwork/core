package network.discov.core.spigot.model;

import network.discov.core.spigot.Core;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;
import java.util.logging.Logger;

public class ComponentLogger extends Logger {
    public ComponentLogger(@NotNull CoreComponent context) {
        super(context.getName(), null);
        this.setParent(Core.getInstance().getLogger());
        this.setLevel(Level.ALL);
    }
}
