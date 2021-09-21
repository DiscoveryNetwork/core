package network.discov.core.spigot;

import com.google.common.collect.Iterables;
import network.discov.core.common.CommonUtils;
import network.discov.core.common.MessageUtil;
import network.discov.core.common.PersistentStorage;
import network.discov.core.spigot.command.CoreCommandExecutor;
import network.discov.core.spigot.model.SpigotComponent;
import network.discov.core.spigot.util.SpigotMessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Core extends JavaPlugin {
    private final List<SpigotComponent> components = new ArrayList<>();
    private final MessageUtil messageUtil = new SpigotMessageUtil();
    private final PersistentStorage persistentStorage = new PersistentStorage();
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
        Objects.requireNonNull(getCommand("core")).setExecutor(new CoreCommandExecutor());
        loadComponents();
        loadDefaultMessages();
    }

    @Override
    public void onDisable() {
        unloadComponents();
    }

    private File getComponentDirectory() {
        File directory = new File(getDataFolder(), "components/");
        if (directory.mkdirs()) {
            getLogger().info("Component directory doesn't exist yet. Creating now...");
        }
        return directory;
    }

    private void loadDefaultMessages() {
        messageUtil.registerDefault("missing-arguments", "&cThe following arguments are missing: &7%s");
        messageUtil.registerDefault("player-only", "&cYou need to be a player to execute that command!");
        messageUtil.registerDefault("player-not-found", "&cThe specified player was not found!");
        messageUtil.registerDefault("no-permission", "&cYou don't have permission to execute this command");
        messageUtil.registerDefault("subcommand-not-found", "&cThat subcommand was not found! &e&oUse &b&o/%s help &e&ofor a list of all available commands");
    }

    private void loadComponents() {
        for (File file : Objects.requireNonNull(getComponentDirectory().listFiles())) {
            if (!file.getName().toLowerCase().contains(".jar")) { continue; }
            try {
                URLClassLoader loader = new URLClassLoader(new URL[]{file.toURI().toURL()}, this.getClass().getClassLoader());
                InputStream stream = loader.getResourceAsStream("component.yml");
                if (stream != null) {
                    FileConfiguration config = YamlConfiguration.loadConfiguration(new InputStreamReader(stream));
                    for (String plugin : config.getStringList("depend")) {
                        if (getServer().getPluginManager().getPlugin(plugin) == null) {
                            String message = String.format("%s depends on %s, but dependency was not found. Cancelled loading.", config.getString("name"), plugin);
                            getLogger().warning(message);
                        }
                    }

                    @SuppressWarnings("unchecked")
                    Class<SpigotComponent> component = (Class<SpigotComponent>) Class.forName(config.getString("main"), true, loader);
                    SpigotComponent instance = component.getDeclaredConstructor().newInstance();
                    loadComponent(instance);
                } else {
                    getLogger().warning(String.format("Component detected without a (valid) component.yml at [%s]", file));
                }
            } catch (MalformedURLException | ClassNotFoundException | IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException e) {
                getLogger().warning("Something went wrong while trying to load a component at " + file);
                e.printStackTrace();
            }
        }
    }

    private void unloadComponents() {
        for (SpigotComponent component : components) {
            unloadComponent(component);
        }
        components.clear();
    }

    private void loadComponent(SpigotComponent component) {
        components.add(component);
        component.onEnable();
        getLogger().info(String.format("Component [%s] is now enabled!", component.getName()));
    }

    private void unloadComponent(SpigotComponent component) {
        component.onDisable();
        component.unregisterCommands();
        component.unregisterListeners();
        getLogger().info(String.format("Component [%s] is now disabled!", component.getName()));
    }

    public void sendNetworkBroadcast(String message, String permission) {
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

    public String getServerName() {
        return getConfig().getString("server-name");
    }

    public MessageUtil getMessageUtil() {
        return messageUtil;
    }

    public PersistentStorage getPersistentStorage() {
        return persistentStorage;
    }

    public static Core getInstance() {
        return instance;
    }
}
