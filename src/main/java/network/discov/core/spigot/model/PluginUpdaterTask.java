package network.discov.core.spigot.model;

import network.discov.core.common.NexusClient;
import network.discov.core.common.exception.ArtifactNotFoundException;
import network.discov.core.common.exception.InvalidResponseCodeException;
import network.discov.core.spigot.Core;
import network.discov.core.spigot.util.PluginUtil;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.jetbrains.annotations.NotNull;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class PluginUpdaterTask implements Runnable {
    private final List<Plugin> plugins;
    private final Logger logger;
    private final boolean updateSnapshots;
    private final String authString;

    public PluginUpdaterTask(boolean updateSnapshots) {
        this.plugins = getApplicablePlugins();
        this.logger = Core.getInstance().getLogger();
        this.updateSnapshots = updateSnapshots;
        this.authString = Core.getInstance().getConfig().getString("nexus-auth");
    }

    @Override
    public void run() {
        int updates = 0;
        for (Plugin plugin : plugins) {
            String file = getFileName(plugin);
            String name = file.split("-")[0];

            if (file.contains("SNAPSHOT") && !updateSnapshots) {
                logger.info(String.format("Plugin [%s] is running on a SNAPSHOT version! Run /core updater --forceSnapshots to update snapshot versions.", name));
                continue;
            }

            try {
                String latestVersion = NexusClient.getLatestVersion("discov-plugins", name, authString);
                if (file.equalsIgnoreCase(latestVersion)) {
                    logger.info(String.format("Plugin [%s] is already running the latest release.", name));
                    continue;
                }

                logger.info(String.format("Plugin [%s] is not running the latest release. Performing update now...", name));
                NexusClient.downloadFile("plugins", "discov-plugins", name, authString);
                PluginUtil.unload(name);
                deletePlugin(plugin);
                logger.info(String.format("[%s] has been successfully installed.", latestVersion));
                updates++;

            } catch (IOException | ParseException | ArtifactNotFoundException | InvalidResponseCodeException e) {
                logger.warning(e.getMessage());
            }
        }

        logger.info(String.format("PluginUpdaterTask done. Installed %s new releases.", updates));
    }

    private @NotNull List<Plugin> getApplicablePlugins() {
        PluginManager pluginManager = Bukkit.getServer().getPluginManager();
        List<Plugin> plugins = new ArrayList<>();
        for (Plugin plugin : pluginManager.getPlugins()) {
            if (plugin.getDescription().getAuthors().contains("ParrotLync") && !plugin.getName().equals("DiscovCore")) {
                plugins.add(plugin);
            }
        }
        return plugins;
    }

    private @NotNull File getFile(@NotNull Plugin plugin) {
        String path = plugin.getClass().getProtectionDomain().getCodeSource().getLocation().getFile();
        return new File(path);
    }

    private String getFileName(@NotNull Plugin plugin) {
        String[] fragments = plugin.getClass().getProtectionDomain().getCodeSource().getLocation().getFile().split("/");
        return fragments[fragments.length - 1];
    }

    private void deletePlugin(Plugin plugin) {
        File file = getFile(plugin);
        if (file.delete()) {
            logger.info(String.format("Plugin file [%s] was deleted!", file.getName()));
            return;
        }
        logger.warning(String.format("Error occurred while deleting plugin file [%s]", file.getName()));
    }
}
