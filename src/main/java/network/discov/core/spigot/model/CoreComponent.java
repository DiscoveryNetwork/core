package network.discov.core.spigot.model;

import network.discov.core.spigot.Core;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.Objects;

public abstract class CoreComponent {
    protected String name;
    protected String version;
    private FileConfiguration configuration;
    private static CoreComponent instance;

    public CoreComponent() {
        InputStreamReader reader = new InputStreamReader(Objects.requireNonNull(getComponentFile()));
        YamlConfiguration properties = YamlConfiguration.loadConfiguration(reader);
        this.name = properties.getString("name");
        this.version = properties.getString("version");
    }

    public static CoreComponent getInstance() {
        return instance;
    }

    abstract public void onEnable();
    abstract public void onDisable();

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    @Override
    public String toString() {
        return "CoreComponent{name='" + name + '\'' + ", version='" + version + '\'' + '}';
    }

    private @NotNull File getConfigFile() {
        File directory = new File(Core.getInstance().getDataFolder(), "config/");
        if (directory.mkdirs()) {
            Core.getInstance().getLogger().info("Config directory doesn't exist yet. Creating now...");
        }

        File file = new File(directory, String.format("config-%s.yml", this.name.toLowerCase()));
        if (!file.exists()) {
            try {
                if (file.createNewFile()) {
                    Core.getInstance().getLogger().info("Creating config file for " + this.toString());
                }
            } catch (IOException e) {
                Core.getInstance().getLogger().severe("An error occurred while creating the config for " + this.toString());
                e.printStackTrace();
            }
        }
        return file;
    }

    private @Nullable InputStream getResourceConfig() {
        return this.getClass().getClassLoader().getResourceAsStream("config.yml");
    }

    private @Nullable InputStream getComponentFile() {
        return this.getClass().getClassLoader().getResourceAsStream("component.yml");
    }

    protected FileConfiguration getConfig() {
        if (this.configuration == null) {
            this.configuration = YamlConfiguration.loadConfiguration(this.getConfigFile());
        }

        return configuration;
    }

    protected void saveConfig(FileConfiguration config) {
        try {
            config.save(this.getConfigFile());
        } catch (IOException e) {
            Core.getInstance().getLogger().severe("An error occurred while saving the config for " + this.toString());
            e.printStackTrace();
        }
    }

    protected void reloadConfig() {
        FileConfiguration config = this.getConfig();
        InputStream resourceConfig = getResourceConfig();
        if (resourceConfig != null) {
            InputStreamReader reader = new InputStreamReader(resourceConfig);
            config.setDefaults(YamlConfiguration.loadConfiguration(reader));
        }
        this.configuration = config;
        this.saveConfig(config);
    }

    protected void registerListener(Listener listener) {
        Bukkit.getServer().getPluginManager().registerEvents(listener, Core.getInstance());
    }

    private CommandMap getCommandMap() {
        try {
            final Field bukkitCommandMap = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            bukkitCommandMap.setAccessible(true);

            return (CommandMap) bukkitCommandMap.get(Bukkit.getServer());
        } catch (IllegalAccessException | NoSuchFieldException e) {
            Core.getInstance().getLogger().warning("Failed to retrieve CommandMap");
            e.printStackTrace();
        }
        return null;
    }

    protected void registerCommand(Command command) {
        CommandMap commandMap = this.getCommandMap();
        if (commandMap != null) {
            commandMap.register(command.getName(), command);
        }
    }

    protected void addDefaultMessage(String key, String message) {
        Core.getInstance().getMessageUtil().registerDefault(key, message);
    }

    public void logInfo(String msg) {
        Core.getInstance().getLogger().info(String.format("[%s] %s", name, msg));
    }

    public void logWarning(String msg) {
        Core.getInstance().getLogger().warning(String.format("[%s] %s", name, msg));
    }

    public void logSevere(String msg) {
        Core.getInstance().getLogger().severe(String.format("[%s] %s", name, msg));
    }
}
