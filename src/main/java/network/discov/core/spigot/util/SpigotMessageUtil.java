package network.discov.core.spigot.util;

import network.discov.core.common.MessageUtil;
import network.discov.core.spigot.Core;
import org.apache.commons.lang.NotImplementedException;
import org.bukkit.ChatColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class SpigotMessageUtil extends MessageUtil {
    YamlConfiguration messages;

    public void registerDefault(@NotNull String key, @NotNull String message) {
        if (messages.contains(key)) { return; }
        messages.set(key, message);
        save();
    }

    public String get(@NotNull String key, String... args) {
        if (messages.contains(key)) {
            String message = messages.getString(key);
            if (message == null) { return ""; }
            message = String.format(message, (Object[]) args);
            return ChatColor.translateAlternateColorCodes('&', message);
        }

        return "";
    }

    @Override
    protected File getFile() {
        File file = new File(Core.getInstance().getDataFolder(), "messages.yml");
        if (!file.exists()) {
            try {
                if (file.createNewFile()) {
                    Core.getInstance().getLogger().info("messages.yml doesn't exist yet. Creating now...");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file;
    }

    @Override
    public void load() {
        YamlConfiguration config = new YamlConfiguration();
        try {
            config.load(getFile());
            messages = config;
        } catch (IOException | InvalidConfigurationException e) {
            Core.getInstance().getLogger().warning("Failed to load messages.yml as YamlConfiguration!");
            e.printStackTrace();
        }
    }

    @Override
    protected void save() {
        try {
            messages.save(getFile());
        } catch (IOException e) {
            Core.getInstance().getLogger().warning("Failed to save messages.yml!");
            e.printStackTrace();
        }
    }
}