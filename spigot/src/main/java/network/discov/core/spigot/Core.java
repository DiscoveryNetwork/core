package network.discov.core.spigot;

import com.google.common.collect.Iterables;
import network.discov.core.commons.exception.InvalidResponseCodeException;
import network.discov.core.commons.utils.*;
import network.discov.core.spigot.command.CoreCommandExecutor;
import network.discov.core.spigot.model.ComponentUpdaterTask;
import network.discov.core.spigot.model.PluginUpdaterTask;
import network.discov.core.spigot.model.SpigotComponent;
import network.discov.core.spigot.util.SpigotMessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Core extends JavaPlugin {
    private final List<SpigotComponent> components = new ArrayList<>();
    private final MessageUtil messageUtil = new SpigotMessageUtil();
    private final List<String> availableComponents = new ArrayList<>();
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
        saveDefaultConfig();
        CommonUtils.logCoreLines(Bukkit.getLogger(), getDescription().getVersion(), getServer().getName(), getServer().getVersion());
        getServer().getMessenger().registerOutgoingPluginChannel(this, "core:broadcast");
        fetchAvailableComponents();
        initPersistentStorage();
        initDatabase();
        Objects.requireNonNull(getCommand("core")).setExecutor(new CoreCommandExecutor());
        loadComponents();
        loadDefaultMessages();
    }

    @Override
    public void onDisable() {
        getServer().getMessenger().unregisterOutgoingPluginChannel(this);
        new PluginUpdaterTask(false).run();
        new ComponentUpdaterTask(components, false).run();
        unloadComponents();

        assert databaseConnector != null;
        try {
            databaseConnector.closeConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void reload() {
        fetchAvailableComponents();
        reloadConfig();
        unloadComponents();
        loadComponents();
        getLogger().info("Core config & components were reloaded.");
    }

    private void fetchAvailableComponents() {
        String authString = getConfig().getString("nexus-auth");
        assert authString != null;

        try {
            List<String> names = NexusClient.getAvailableComponents("core-components", authString);
            for (String name : names) {
                if (!availableComponents.contains(name)) {
                    availableComponents.add(name);
                }
            }
        } catch (IOException | ParseException | InvalidResponseCodeException e) {
            getLogger().warning(e.getMessage());
        }
    }

    private void initPersistentStorage() {
        FileConfiguration config = getConfig();
        persistentStorage = new PersistentStorage(config.getString("redis.host"), config.getInt("redis.port"), config.getString("redis.password"));
    }

    private void initDatabase() {
        String host = getConfig().getString("database.host");
        String database = getConfig().getString("database.database");
        String username = getConfig().getString("database.username");
        String password = getConfig().getString("database.password");

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
        messageUtil.registerDefault("player-not-found", "&cThe specified player was not found!");
        messageUtil.registerDefault("no-permission", "&cYou don't have permission to execute this command");
        messageUtil.registerDefault("subcommand-not-found", "&cThat subcommand was not found! &e&oUse &b&o/%s help &e&ofor a list of all available commands");
        messageUtil.registerDefault("error-parse-number", "&cFailed to parse number &7[&3%s&7]");
    }

    private void loadComponents() {
        for (File file : Objects.requireNonNull(getComponentDirectory().listFiles())) {
            if (!file.getName().toLowerCase().contains(".jar")) { continue; }
            try {
                doLoadComponent(file);
            } catch (MalformedURLException | ClassNotFoundException | IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException e) {
                getLogger().warning("Something went wrong while trying to load a component at " + file);
                e.printStackTrace();
            }
        }
    }

    private void doLoadComponent(@NotNull File file) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, MalformedURLException {
        URLClassLoader loader = new URLClassLoader(new URL[]{file.toURI().toURL()}, this.getClass().getClassLoader());
        InputStream configStream = loader.getResourceAsStream("configuration.yml");
        InputStream componentStream = loader.getResourceAsStream("component.yml");

        if (componentStream != null) {
            YamlConfiguration componentFile = YamlConfiguration.loadConfiguration(new InputStreamReader(componentStream ));
            for (String plugin : componentFile.getStringList("depend")) {
                if (getServer().getPluginManager().getPlugin(plugin) == null || !getServer().getPluginManager().isPluginEnabled(plugin)) {
                    String message = String.format("%s depends on %s, but dependency was not found. Cancelled loading.", componentFile.getString("name"), plugin);
                    getLogger().warning(message);
                    return;
                }
            }

            @SuppressWarnings("unchecked")
            Class<SpigotComponent> component = (Class<SpigotComponent>) Class.forName(componentFile.getString("main"), true, loader);
            SpigotComponent instance = component.getDeclaredConstructor().newInstance();
            if (configStream != null) { instance.saveDefaultConfig(configStream); }
            loadComponent(instance);
        } else {
            getLogger().warning(String.format("Component detected without a (valid) component.yml at [%s]", file));
        }
    }

    private void unloadComponents() {
        for (SpigotComponent component : components) {
            unloadComponent(component);
        }
        components.clear();
    }

    private void loadComponent(SpigotComponent component) {
        availableComponents.remove(component.getName());

        try {
            component.onEnable();
            components.add(component);
            getLogger().info(String.format("Component [%s] is now enabled!", component.getName()));
        } catch (Exception e) {
            getLogger().warning(String.format("An error occurred while enabling component [%s]", component.getName()));
            e.printStackTrace();
        }
    }

    private void unloadComponent(@NotNull SpigotComponent component) {
        component.getScheduler().cancelTasks();
        component.onDisable();
        component.unregisterCommands();
        component.unregisterListeners();
        getLogger().info(String.format("Component [%s] is now disabled!", component.getName()));
    }

    private @Nullable SpigotComponent getSpigotComponent(String name) {
        for (SpigotComponent component : components) {
            if (component.getName().equalsIgnoreCase(name)) {
                return component;
            }
        }

        return null;
    }

    public List<String> getComponentNames() {
        List<String> names = new ArrayList<>();
        for (SpigotComponent component : components) {
            names.add(component.getName());
        }
        return names;
    }

    public boolean loadComponent(String path) {
        File file = new File(path);
        try {
            doLoadComponent(file);
        } catch (ClassNotFoundException | MalformedURLException | NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean unloadComponent(String name) {
        SpigotComponent component = getSpigotComponent(name);
        if (component == null) { return false; }

        unloadComponent(component);
        return true;
    }

    public boolean reloadComponent(String name) {
        SpigotComponent component = getSpigotComponent(name);
        if (component == null) { return false; }

        unloadComponent(component);
        loadComponent(component);
        return true;
    }

    public void updateComponents(boolean forceSnapshots) {
        getServer().getScheduler().runTaskAsynchronously(this, new ComponentUpdaterTask(components, forceSnapshots));
    }

    public void broadcast(@NotNull String message, String permission) {
        Player player = Iterables.get(Bukkit.getOnlinePlayers(), 0);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);

        try {
            dataOutputStream.writeUTF(message);
            dataOutputStream.writeUTF(permission);
        } catch (IOException e) {
            e.printStackTrace();
        }

        player.sendPluginMessage(this, "core:broadcast", byteArrayOutputStream.toByteArray());
    }

    public List<String> getAvailableComponents() {
        return availableComponents;
    }

    public String getServerName() {
        return getConfig().getString("server-name");
    }

    public MessageUtil getMessageUtil() {
        return messageUtil;
    }

    public @Nullable PersistentStorage getPersistentStorage() {
        return persistentStorage;
    }

    public @Nullable DatabaseConnector getDatabaseConnector() {
        return databaseConnector;
    }

    public static Core getInstance() {
        return instance;
    }
}
