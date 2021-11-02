package network.discov.core.common;

import org.jetbrains.annotations.NotNull;

import java.io.File;

public abstract class MessageUtil {
    abstract public void registerDefault(@NotNull String key, @NotNull String message);

    abstract public String get(@NotNull String key, Object... args);

    protected abstract File getFile();

    public abstract void load();

    protected abstract void save();
}
