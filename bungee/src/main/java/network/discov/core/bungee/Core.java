package network.discov.core.bungee;

import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import network.discov.core.bungee.model.BungeeComponent;
import network.discov.core.bungee.util.BungeeMessageUtil;
import network.discov.core.commons.CommonUtils;
import network.discov.core.commons.DatabaseConnector;
import network.discov.core.commons.MessageUtil;
import network.discov.core.commons.PersistentStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Core extends Plugin {
    private final List<BungeeComponent> components = new ArrayList<>();
    private final MessageUtil messageUtil = new BungeeMessageUtil();
    private PersistentStorage persistentStorage;
    private DatabaseConnector databaseConnector;
    private static Core instance;

    public Core() { instance = this; }

    @Override
    public void onLoad() {
        messageUtil.load();
    }

    @Override
    public void onEnable() {
        CommonUtils.logCoreLines(getLogger(), getDescription().getVersion(), getProxy().getName(), getProxy().getVersion());
        saveDefaultConfig();
        initDatabase();
        initPersistentStorage();
        loadComponents();
        loadDefaultMessages();
    }

    @Override
    public void onDisable() {
        unloadComponents();
    }

    private void initPersistentStorage() {
        Configuration config = getConfig();
        assert config != null;
        persistentStorage = new PersistentStorage(config.getString("redis.host"), config.getInt("redis.port"), config.getString("redis.password"));
    }

    private void initDatabase() {
        Configuration config = getConfig();
        assert config != null;
        String host = config.getString("database.host");
        String database = config.getString("database.database");
        String username = config.getString("database.username");
        String password = config.getString("database.password");

        assert host != null && database != null && username != null && password != null;
        if (host.equals("null") || database.equals("null") || username.equals("null") || password.equals("null")) {
            return;
        }

        databaseConnector = new DatabaseConnector(host, username, password, database);
    }

    private @NotNull File getComponentDirectory() {
        File directory = new File(getDataFolder(), "components/");
        if (directory.mkdirs()) {
            getLogger().info("Component directory doesn't exist yet. Creating now...");
        }
        return directory;
    }

    private void loadDefaultMessages() {
        messageUtil.registerDefault("missing-arguments", "&cThe following arguments are missing: &7%s");
        messageUtil.registerDefault("args-validator-failed", "&4Validator failed: &c%s");
        messageUtil.registerDefault("player-only", "&cYou need to be a player to execute that command!");
        messageUtil.registerDefault("no-permission", "&cYou don't have permission to execute this command");
    }

    private void saveDefaultConfig() {
        if (!getDataFolder().exists()) {
            getDataFolder().mkdir();
        }

        File file = new File(getDataFolder(), "config.yml");

        if (!file.exists()) {
            try (InputStream in = getResourceAsStream("config-bungee.yml")) {
                Files.copy(in, file.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private @Nullable Configuration getConfig() {
        try {
            return ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(getDataFolder(), "config.yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void loadComponents() {
        for (File file : Objects.requireNonNull(getComponentDirectory().listFiles())) {
            if (!file.getName().toLowerCase().contains(".jar")) { continue; }
            try {
                URLClassLoader loader = new URLClassLoader(new URL[]{file.toURI().toURL()}, this.getClass().getClassLoader());
                InputStream configStream = loader.getResourceAsStream("configuration.yml");
                InputStream stream = loader.getResourceAsStream("component-bungee.yml");

                if (stream != null) {
                    Configuration config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(stream);
                    for (String plugin : config.getStringList("depend")) {
                        if (getProxy().getPluginManager().getPlugin(plugin) == null) {
                            String message = String.format("%s depends on %s, but dependency was not found. Cancelled loading.", config.getString("name"), plugin);
                            getLogger().warning(message);
                        }
                    }

                    @SuppressWarnings("unchecked")
                    Class<BungeeComponent> component = (Class<BungeeComponent>) Class.forName(config.getString("main"), true, loader);
                    BungeeComponent instance = component.getDeclaredConstructor().newInstance();
                    if (configStream != null) { instance.saveDefaultConfig(configStream); }
                    loadComponent(instance);
                } else {
                    getLogger().warning(String.format("Component detected without a (valid) component.yml at [%s]", file));
                }
            } catch (MalformedURLException | ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                getLogger().warning("Something went wrong while trying to load a component at " + file);
                e.printStackTrace();
            }
        }
    }

    private void unloadComponents() {
        for (BungeeComponent component : components) {
            unloadComponent(component);
        }
        components.clear();
    }

    private void loadComponent(BungeeComponent component) {
        components.add(component);
        component.onEnable();
        getLogger().info(String.format("Component [%s] is now enabled!", component.getName()));
    }

    private void unloadComponent(@NotNull BungeeComponent component) {
        component.onDisable();
        component.unregisterCommands();
        component.unregisterListeners();
        getLogger().info(String.format("Component [%s] is now disabled!", component.getName()));
    }

    public MessageUtil getMessageUtil() {
        return messageUtil;
    }

    public @Nullable PersistentStorage getPersistentStorage() {
        return persistentStorage;
    }

    public @Nullable DatabaseConnector getDatabaseConnector() { return databaseConnector; }

    public static Core getInstance() {
        return instance;
    }
}
