package network.discov.core.spigot.util;

import network.discov.core.spigot.Core;
import org.bukkit.ChatColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class MessageUtil {
    HashMap<String, String> messages = new HashMap<>();

    public void registerDefault(@NotNull String key, @NotNull String message) {
        if (messages.containsKey(key)) { return; }
        messages.put(key, message);
        saveToFile(key, message);
    }

    public String get(@NotNull String key, String... args) {
        if (messages.containsKey(key)) {
            String message = messages.get(key);
            message = String.format(message, (Object[]) args);
            return ChatColor.translateAlternateColorCodes('&', message);
        }

        return "";
    }

    private File getFile() {
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

    public void loadFromFile() {
        YamlConfiguration config = new YamlConfiguration();
        try {
            config.load(getFile());
        } catch (IOException | InvalidConfigurationException e) {
            Core.getInstance().getLogger().warning("Failed to load messages.yml as YamlConfiguration!");
            e.printStackTrace();
            return;
        }


    }

    private void saveToFile(String key, String message) {

    }
}