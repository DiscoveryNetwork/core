package network.discov.core.commons.utils;

import java.util.logging.Logger;

public class CommonUtils {
    public static void logCoreLines(Logger logger, String version, String serverName, String serverVersion) {
        logger.info("   ______              ");
        logger.info("  / ____/___  ________ ");
        logger.info(" / /   / __ \\/ ___/ _ \\         DiscovCore v" + version);
        logger.info("/ /___/ /_/ / /  /  __/         Running on " + serverName + " " + serverVersion);
        logger.info("\\____/\\____/_/   \\___/ ");
        logger.info("");
    }
}
