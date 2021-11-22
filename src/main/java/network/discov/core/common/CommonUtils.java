package network.discov.core.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class CommonUtils {
    public static String getArgsMessage(List<String> missingArguments, MessageUtil messageUtil) {
        return messageUtil.get("missing-arguments", missingArguments.toString());
    }

    public static List<String> getMissingArguments(String[] args, Map<Integer, TabArgument> tabArguments) {
        List<String> missingArguments = new ArrayList<>();
        for (TabArgument argument : new ArrayList<>(tabArguments.values()).subList(args.length, tabArguments.size())) {
            if (argument.isRequired()) { missingArguments.add(argument.getName()); }
        }
        return missingArguments;
    }

    public static void logCoreLines(Logger logger, String version, String serverName, String serverVersion) {
        logger.info("   ______              ");
        logger.info("  / ____/___  ________ ");
        logger.info(" / /   / __ \\/ ___/ _ \\         DiscovCore v" + version);
        logger.info("/ /___/ /_/ / /  /  __/         Running on " + serverName + " " + serverVersion);
        logger.info("\\____/\\____/_/   \\___/ ");
        logger.info("");
    }
}
