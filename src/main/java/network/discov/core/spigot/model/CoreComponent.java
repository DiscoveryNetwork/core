package network.discov.core.spigot.model;

import network.discov.core.spigot.Core;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.*;
import java.util.logging.Logger;

public abstract class CoreComponent {
    protected String name;
    protected String version;
    private FileConfiguration configuration;
    private static CoreComponent instance;
    private final ComponentLogger logger;
    private final List<Command> commands = new ArrayList<>();
    private final List<Listener> listeners = new ArrayList<>();

    public CoreComponent() {
        InputStreamReader reader = new InputStreamReader(Objects.requireNonNull(getComponentFile()));
        YamlConfiguration properties = YamlConfiguration.loadConfiguration(reader);
        this.name = properties.getString("name");
        this.version = properties.getString("version");
        this.logger = new ComponentLogger(this);
        instance = this;
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
            getLogger().info("Config directory doesn't exist yet. Creating now...");
        }

        File file = new File(directory, String.format("config-%s.yml", this.name.toLowerCase()));
        if (!file.exists()) {
            try {
                if (file.createNewFile()) {
                    getLogger().info("Creating config file...");
                }
            } catch (IOException e) {
                getLogger().severe("An error occurred while creating the config file");
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
            getLogger().severe("An error occurred while saving the config file");
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

    private SimpleCommandMap getCommandMap() {
        try {
            final Field bukkitCommandMap = Bukkit.getPluginManager().getClass().getDeclaredField("commandMap");
            bukkitCommandMap.setAccessible(true);

            return (SimpleCommandMap) bukkitCommandMap.get(Bukkit.getPluginManager());
        } catch (IllegalAccessException | NoSuchFieldException e) {
            getLogger().warning("Failed to retrieve CommandMap");
            e.printStackTrace();
        }
        return null;
    }

    protected void registerCommand(Command command) {
        SimpleCommandMap commandMap = this.getCommandMap();
        if (commandMap != null) {
            commands.add(command);
            commandMap.register(command.getName(), command);
        }
    }

    public void unregisterCommands() {
        try {
            final Field bukkitCommandMap = Bukkit.getPluginManager().getClass().getDeclaredField("commandMap");
            bukkitCommandMap.setAccessible(true);
            SimpleCommandMap commandMap =  (SimpleCommandMap) bukkitCommandMap.get(Bukkit.getPluginManager());

            final Field knownCommandsField = commandMap.getClass().getSuperclass().getDeclaredField("knownCommands");
            knownCommandsField.setAccessible(true);
            @SuppressWarnings("unchecked")
            HashMap<String, org.bukkit.command.Command> knownCommands = (HashMap<String, org.bukkit.command.Command>) knownCommandsField.get(commandMap);

            for (Command command : commands) {
                knownCommands.remove(command.getName());
                for (String alias : command.getAliases()) {
                    knownCommands.remove(alias);
                }
            }
            commands.clear();
        } catch (IllegalAccessException | NoSuchFieldException e) {
            getLogger().warning("Failed to retrieve CommandMap");
            e.printStackTrace();
        }
    }

    protected void registerListener(Listener listener) {
        listeners.add(listener);
        Bukkit.getServer().getPluginManager().registerEvents(listener, Core.getInstance());
    }

    public void unregisterListeners() {
        for (Listener listener : listeners) {
            HandlerList.unregisterAll(listener);
        }
    }

    protected void addDefaultMessage(String key, String message) {
        Core.getInstance().getMessageUtil().registerDefault(key, message);
    }

    public Logger getLogger() {
        return this.logger;
    }
}
