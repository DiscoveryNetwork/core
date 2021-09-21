package network.discov.core.bungee.util;

import network.discov.core.common.MessageUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class BungeeMessageUtil extends MessageUtil {

    @Override
    public void registerDefault(@NotNull String key, @NotNull String message) {

    }

    @Override
    public String get(@NotNull String key, String... args) {
        return null;
    }

    @Override
    protected File getFile() {
        //TODO: Implement this
        return null;
    }

    @Override
    public void load() {
        //TODO: Implement this
    }

    @Override
    protected void save() {
        //TODO: Implement this
    }
}
