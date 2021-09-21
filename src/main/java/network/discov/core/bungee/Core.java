package network.discov.core.bungee;

import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import network.discov.core.bungee.model.BungeeComponent;
import network.discov.core.bungee.util.BungeeMessageUtil;
import network.discov.core.common.CommonUtils;
import network.discov.core.common.MessageUtil;
import network.discov.core.common.PersistentStorage;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Core extends Plugin {
    private final List<BungeeComponent> components = new ArrayList<>();
    private final MessageUtil messageUtil = new BungeeMessageUtil();
    private final PersistentStorage persistentStorage = new PersistentStorage();
    private static Core instance;

    public Core() { instance = this; }

    @Override
    public void onLoad() {
        messageUtil.load();
    }

    @Override
    public void onEnable() {
        CommonUtils.logCoreLines(getLogger(), getDescription().getVersion(), getProxy().getName(), getProxy().getVersion());
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
        messageUtil.registerDefault("no-permission", "&cYou don't have permission to execute this command");
    }

    private void loadComponents() {
        for (File file : Objects.requireNonNull(getComponentDirectory().listFiles())) {
            if (!file.getName().toLowerCase().contains(".jar")) { continue; }
            try {
                URLClassLoader loader = new URLClassLoader(new URL[]{file.toURI().toURL()}, this.getClass().getClassLoader());
                InputStream stream = loader.getResourceAsStream("component.yml");
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

    private void unloadComponent(BungeeComponent component) {
        component.onDisable();
        component.unregisterCommands();
        component.unregisterListeners();
        getLogger().info(String.format("Component [%s] is now disabled!", component.getName()));
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
