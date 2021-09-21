package network.discov.core.common;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public abstract class CoreComponent {
    protected String name;
    protected String version;
    private ComponentLogger logger;

    abstract public void onEnable();
    abstract public void onDisable();

    abstract protected File getCoreDataFolder();

    abstract protected void saveConfig();
    abstract protected void saveDefaultConfig();
    abstract protected void reloadConfig();
    abstract protected void unregisterCommands();
    abstract protected void unregisterListeners();

    abstract protected PersistentStorage getPersistentStorage();

    abstract protected void addDefaultMessage(String key, String message);

    protected void setupLogger() {
        assert logger == null;
        logger = new ComponentLogger(this);
    }

    protected @Nullable InputStream getResourceFile(String fileName) {
        return getClass().getClassLoader().getResourceAsStream(fileName);
    }

    protected @NotNull File getConfigFile() {
        File directory = new File(getCoreDataFolder(), "config/");
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

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public abstract String getMessage(String key, boolean global, String... args);

    protected ComponentLogger getLogger() {
        return logger;
    }

    @Override
    public String toString() {
        return "CoreComponent{name='" + name + '\'' + ", version='" + version + '\'' + '}';
    }
}
