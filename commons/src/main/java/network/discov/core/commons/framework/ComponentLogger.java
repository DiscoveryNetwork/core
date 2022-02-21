package network.discov.core.commons.framework;

import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;
import java.util.logging.Logger;

public class ComponentLogger extends Logger {
    public ComponentLogger(@NotNull CoreComponent context, Logger parent) {
        super(context.getName(), null);
        this.setParent(parent);
        this.setLevel(Level.ALL);
    }
}
