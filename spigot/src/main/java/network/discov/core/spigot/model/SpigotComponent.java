package network.discov.core.spigot.model;

import network.discov.core.commons.framework.CoreComponent;
import network.discov.core.commons.utils.PersistentStorage;
import network.discov.core.spigot.Core;
import org.bukkit.Bukkit;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public abstract class SpigotComponent extends CoreComponent {
    private final List<Command> commands = new ArrayList<>();
    private final List<ComponentCommandExecutor> executors = new ArrayList<>();
    private final List<Listener> listeners = new ArrayList<>();
    private final Scheduler scheduler;
    private FileConfiguration configuration;

    public SpigotComponent(String name) {
        super(name, Core.getInstance().getLogger());
        InputStreamReader reader = new InputStreamReader(Objects.requireNonNull(getResourceFile("component.yml")));
        YamlConfiguration properties = YamlConfiguration.loadConfiguration(reader);
        this.version = properties.getString("version");
        this.developers = properties.getStringList("developers");
        this.scheduler = new Scheduler();
    }

    abstract public void onEnable();
    abstract public void onDisable();

    @Override
    protected File getCoreDataFolder() {
        return Core.getInstance().getDataFolder();
    }

    protected FileConfiguration getConfig() {
        if (this.configuration == null) {
            this.configuration = YamlConfiguration.loadConfiguration(this.getConfigFile());
        }

        return configuration;
    }

    @Override
    protected void saveConfig() {
        try {
            configuration.save(this.getConfigFile());
        } catch (IOException e) {
            getLogger().severe("An error occurred while saving the config file");
            e.printStackTrace();
        }
    }

    @Override
    public void saveDefaultConfig(@NotNull InputStream stream) {
        super.saveDefaultConfig(stream);
        getConfig();
    }

    @Override
    protected void reloadConfig() {
        FileConfiguration config = this.getConfig();
        InputStream resourceConfig = getResourceFile("configuration.yml");
        if (resourceConfig != null) {
            InputStreamReader reader = new InputStreamReader(resourceConfig);
            config.setDefaults(YamlConfiguration.loadConfiguration(reader));
        }
        this.configuration = config;
        saveConfig();
    }

    private @Nullable SimpleCommandMap getCommandMap() {
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
        if (!commands.contains(command)) {
            SimpleCommandMap commandMap = this.getCommandMap();
            if (commandMap != null) {
                commands.add(command);
                commandMap.register(command.getName(), command);
            }
        }
    }

    protected void registerExecutor(ComponentCommandExecutor executor) {
        if (!executors.contains(executor)) {
            SimpleCommandMap commandMap = this.getCommandMap();
            if (commandMap != null) {
                executors.add(executor);
                commandMap.register(executor.getName(), executor);
            }
        }
    }

    protected void registerListener(Listener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
            Bukkit.getServer().getPluginManager().registerEvents(listener, Core.getInstance());
        }
    }

    @Override
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

            for (ComponentCommandExecutor executor : executors) {
                knownCommands.remove(executor.getName());
                for (String alias : executor.getAliases()) {
                    knownCommands.remove(alias);
                }
            }
            executors.clear();
        } catch (IllegalAccessException | NoSuchFieldException e) {
            getLogger().warning("Failed to retrieve CommandMap");
            e.printStackTrace();
        }
    }

    @Override
    public void unregisterListeners() {
        for (Listener listener : listeners) {
            HandlerList.unregisterAll(listener);
        }
        listeners.clear();
    }

    public Scheduler getScheduler() {
        return scheduler;
    }

    @Override
    protected PersistentStorage getPersistentStorage() {
        return Core.getInstance().getPersistentStorage();
    }

    @Override
    public String getMessage(String key, Object... args) {
        return Core.getInstance().getMessageUtil().get(getKey(key), args);
    }

    @Override
    public String getGlobalMessage(String key, Object... args) {
        return Core.getInstance().getMessageUtil().get(key, args);
    }

    @Override
    protected void addDefaultMessage(String key, String message) {
        Core.getInstance().getMessageUtil().registerDefault(getKey(key), message);
    }

    private String getKey(String key) {
        return String.format("%s.%s", name.toLowerCase(), key);
    }
}
