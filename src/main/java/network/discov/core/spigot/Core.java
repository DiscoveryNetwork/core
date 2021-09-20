package network.discov.core.spigot;

import network.discov.core.spigot.model.CoreComponent;
import network.discov.core.spigot.util.MessageUtil;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

public class Core extends JavaPlugin {
    private final List<CoreComponent> components = new ArrayList<>();
    private final MessageUtil messageUtil = new MessageUtil();
    private static Core instance;

    public Core() { instance = this; }

    @Override
    public void onLoad() {
        messageUtil.loadFromFile();
        super.onLoad();
    }

    @Override
    public void onEnable() {
        logLogoLines();
        loadComponents();
        loadDefaultMessages();
    }

    @Override
    public void onDisable() {
        unloadComponents();
    }

    private void logLogoLines() {
        getLogger().info("");
        getLogger().info("   ______              ");
        getLogger().info("  / ____/___  ________ ");
        getLogger().info(" / /   / __ \\/ ___/ _ \\         DiscovCore v" + getDescription().getVersion());
        getLogger().info("/ /___/ /_/ / /  /  __/           Running on PaperSpigot");
        getLogger().info("\\____/\\____/_/   \\___/ ");
        getLogger().info("");
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
        for (File file : getComponentDirectory().listFiles()) {
            if (!file.getName().toLowerCase().contains(".jar")) { continue; }
            try {
                URLClassLoader loader = new URLClassLoader(new URL[]{file.toURI().toURL()}, this.getClass().getClassLoader());
                InputStream stream = loader.getResourceAsStream("component.yml");
                if (stream != null) {
                    FileConfiguration config = YamlConfiguration.loadConfiguration(new InputStreamReader(stream));

                    @SuppressWarnings("unchecked")
                    Class<CoreComponent> component = (Class<CoreComponent>) Class.forName(config.getString("main"), true, loader);
                    CoreComponent instance = component.getDeclaredConstructor().newInstance();
                    loadComponent(instance);

                    //@SuppressWarnings("unchecked") Class<? extends CoreComponent> component = (Class<? extends CoreComponent>) Class.forName(config.getString("main"));
                } else {
                    getLogger().warning(String.format("Component detected without a (valid) component.yml at [%s]", file.toString()));
                }
            } catch (MalformedURLException | ClassNotFoundException | IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException e) {
                Core.getInstance().getLogger().warning("Something went wrong while trying to load a component at " + file);
                e.printStackTrace();
            }
        }
    }

    private void unloadComponents() {
        for (CoreComponent component : components) {
            unloadComponent(component);
        }
    }

    private void loadComponent(CoreComponent component) {
        components.add(component);
        component.onEnable();
        getLogger().info(String.format("Component [%s] is now enabled!", component.getName()));
    }

    private void unloadComponent(CoreComponent component) {
        components.remove(component);
        component.onDisable();
        getLogger().info(String.format("Component [%s] is now disabled!", component.getName()));
    }

    public MessageUtil getMessageUtil() {
        return messageUtil;
    }

    public static Core getInstance() {
        return instance;
    }
}
