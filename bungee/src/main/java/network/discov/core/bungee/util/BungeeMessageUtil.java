package network.discov.core.bungee.util;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import network.discov.core.bungee.Core;
import network.discov.core.commons.utils.MessageUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

public class BungeeMessageUtil extends MessageUtil {
    private Configuration messages;

    @Override
    public void registerDefault(@NotNull String key, @NotNull String message) {
        if (messages.contains(key)) { return; }
        messages.set(key, message);
        save();
    }

    @Override
    public String get(@NotNull String key, Object... args) {
        if (messages.contains(key)) {
            String message = messages.getString(key);
            if (message == null) { return ""; }
            message = String.format(message, args);
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
                    Core.getInstance().getLogger().info("messages.yml doesn't exist. Creating now...");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file;
    }

    @Override
    public void load() {
        try {
            messages = ConfigurationProvider.getProvider(YamlConfiguration.class).load(getFile());
        } catch (IOException e) {
            Core.getInstance().getLogger().warning("Failed to load messages.yml as Configuration!");
            e.printStackTrace();
        }
    }

    @Override
    protected void save() {
        try {
            ConfigurationProvider.getProvider(YamlConfiguration.class).save(messages, getFile());
        } catch (IOException e) {
            Core.getInstance().getLogger().warning("Failed to save Configuration as messages.yml!");
            e.printStackTrace();
        }
    }
}
