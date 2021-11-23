package network.discov.core.commons;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.logging.Logger;

public abstract class CoreComponent {
    protected String name;
    protected String version;
    protected List<String> developers;
    private final ComponentLogger logger;

    public CoreComponent(String name, Logger parentLogger) {
        this.name = name;
        logger = new ComponentLogger(this, parentLogger);
    }

    abstract public void onEnable();
    abstract public void onDisable();

    abstract protected File getCoreDataFolder();
    abstract protected void saveConfig();
    abstract protected void reloadConfig();
    abstract protected void unregisterCommands();
    abstract protected void unregisterListeners();
    abstract protected PersistentStorage getPersistentStorage();
    abstract protected void addDefaultMessage(String key, String message);

    protected @Nullable InputStream getResourceFile(String fileName) {
        return this.getClass().getClassLoader().getResourceAsStream(fileName);
    }

    public void saveDefaultConfig(@NotNull InputStream stream) {
        File file = getConfigFile();
        if (file.length() == 0) {
            try {
                Files.copy(stream, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private @NotNull File getDataFolder() {
        File directory = new File(getCoreDataFolder(), String.format("data/%s/", name.toLowerCase()));
        if (directory.mkdirs()) {
            getLogger().info("Data directory doesn't exist yet. Creating now...");
        }
        return directory;
    }

    private @NotNull File getFile(String fileName) {
        File dataFolder = getDataFolder();
        File file = new File(dataFolder, fileName);
        if (!file.exists()) {
            try {
                if (file.createNewFile()) {
                    getLogger().info(String.format("Creating file %s...", fileName));
                }
            } catch (IOException e) {
                getLogger().severe(String.format("An error occurred while creating the file %s", fileName));
                e.printStackTrace();
            }
        }
        return file;
    }

    protected @NotNull File getConfigFile() {
        return getFile("config.yml");
    }

    protected <T> void saveObjectToFile(String fileName, T object) {
        File file = getFile(fileName);
        DataUtil.saveObjectToPath(object, file.getPath());
    }

    protected <T> T loadObjectFromFile(String fileName) {
        File file = getFile(fileName);
        return DataUtil.loadObjectFromPath(file.getPath());
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public List<String> getDevelopers() {
        return developers;
    }

    public abstract String getMessage(String key, Object... args);
    public abstract String getGlobalMessage(String key, Object... args);

    public ComponentLogger getLogger() {
        return logger;
    }

    @Override
    public String toString() {
        return "CoreComponent{name='" + name + '\'' + ", version='" + version + '\'' + '}';
    }
}
