package network.discov.core.bungee.model;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import network.discov.core.bungee.Core;
import network.discov.core.commons.CoreComponent;
import network.discov.core.commons.PersistentStorage;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public abstract class BungeeComponent extends CoreComponent {
    private final List<Command> commands = new ArrayList<>();
    private final List<Listener> listeners = new ArrayList<>();
    private final Scheduler scheduler;
    private Configuration configuration;

    public BungeeComponent(String name) {
        super(name, Core.getInstance().getLogger());
        Configuration properties = ConfigurationProvider.getProvider(YamlConfiguration.class).load(getResourceFile("component-bungee.yml"));
        this.name = properties.getString("name");
        this.version = properties.getString("version");
        this.scheduler = new Scheduler();
    }

    abstract public void onEnable();
    abstract public void onDisable();

    @Override
    protected File getCoreDataFolder() {
        return Core.getInstance().getDataFolder();
    }

    protected Configuration getConfig() {
        if (this.configuration == null) {
            try {
                configuration = ConfigurationProvider.getProvider(YamlConfiguration.class).load(getConfigFile());
            } catch (IOException e) {
                getLogger().severe("Could not retrieve config file!");
                e.printStackTrace();
            }
        }

        return configuration;
    }

    @Override
    protected void saveConfig() {
        try {
            ConfigurationProvider.getProvider(YamlConfiguration.class).save(configuration, getConfigFile());
        } catch (IOException e) {
            getLogger().severe("Could not save config file!");
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
        Configuration config = this.getConfig();
        InputStream resourceConfig = getResourceFile("config.yml");
        if (resourceConfig != null) {
            setDefaults(config, resourceConfig);
        }
        this.configuration = config;
        saveConfig();
    }

    private void setDefaults(Configuration config, InputStream resource) {
        Configuration resourceConfig = ConfigurationProvider.getProvider(YamlConfiguration.class).load(resource);
        for (String key : resourceConfig.getKeys()) {
            if (!config.contains(key)) {
                config.set(key, resourceConfig.get(key));
            }
        }
    }

    protected void registerCommand(Command command) {
        if (!commands.contains(command)) {
            commands.add(command);
            ProxyServer.getInstance().getPluginManager().registerCommand(Core.getInstance(), command);
        }
    }

    protected void registerListener(Listener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
            ProxyServer.getInstance().getPluginManager().registerListener(Core.getInstance(), listener);
        }
    }

    @Override
    public void unregisterCommands() {
        for (Command command : commands) {
            ProxyServer.getInstance().getPluginManager().unregisterCommand(command);
        }
        commands.clear();
    }

    @Override
    public void unregisterListeners() {
        for (Listener listener : listeners) {
            ProxyServer.getInstance().getPluginManager().unregisterListener(listener);
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
