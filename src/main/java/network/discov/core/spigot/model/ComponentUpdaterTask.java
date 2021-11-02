package network.discov.core.spigot.model;

import network.discov.core.common.NexusClient;
import network.discov.core.common.exception.ArtifactNotFoundException;
import network.discov.core.common.exception.InvalidResponseCodeException;
import network.discov.core.spigot.Core;
import org.jetbrains.annotations.NotNull;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

public class ComponentUpdaterTask implements Runnable {
    private final List<SpigotComponent> components;
    private final Logger logger;
    private final boolean updateSnapshots;
    private final String authString;

    public ComponentUpdaterTask(List<SpigotComponent> components, boolean updateSnapshots) {
        this.components = components;
        this.logger = Core.getInstance().getLogger();
        this.updateSnapshots = updateSnapshots;
        this.authString = Core.getInstance().getConfig().getString("nexus-auth");
    }

    @Override
    public void run() {
        int updates = 0;
        for (SpigotComponent component : components) {
            String file = getFileName(component);
            String name = file.split("-")[0];

            if (file.contains("SNAPSHOT") && !updateSnapshots) {
                logger.info(String.format("Component [%s] is running on a SNAPSHOT version! Run /core updater --forceSnapshots", name));
                continue;
            }

            try {
                String latestVersion = NexusClient.getLatestVersion("discov-components", name, authString);
                if (file.equalsIgnoreCase(latestVersion)) {
                    logger.info(String.format("Component [%s] is already running the latest release.", name));
                    continue;
                }

                logger.info("Component [%s] is not running the latest release. Performing update now...");
                NexusClient.downloadFile("plugins/DiscovCore/components", "discov-components", name, authString);
                if (Core.getInstance().unloadComponent(name)) {
                    deleteComponent(component);
                } else {
                    logger.warning(String.format("Failed to unload component [%s]. Delete was cancelled.", name));
                }

                logger.info(String.format("[%s] has been successfully installed.", latestVersion));
                updates++;
            } catch (IOException | ParseException | ArtifactNotFoundException | InvalidResponseCodeException e) {
                logger.warning(e.getMessage());
            }
        }

        logger.info(String.format("ComponentUpdaterTask done. Installed %s new releases.", updates));
    }

    private @NotNull File getFile(@NotNull SpigotComponent component) {
        String path = component.getClass().getProtectionDomain().getCodeSource().getLocation().getFile();
        return new File(path);
    }

    private String getFileName(@NotNull SpigotComponent component) {
        String[] fragments = component.getClass().getProtectionDomain().getCodeSource().getLocation().getFile().split("/");
        return fragments[fragments.length - 1];
    }

    private void deleteComponent(SpigotComponent component) {
        File file = getFile(component);
        if (file.delete()) {
            logger.info(String.format("Component file [%s] was deleted!", file.getName()));
            return;
        }
        logger.warning(String.format("Error occurred while deleting component file [%s]", file.getName()));
    }
}
