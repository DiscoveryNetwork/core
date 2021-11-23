package network.discov.core.spigot.util;

import network.discov.core.spigot.Core;
import network.discov.core.spigot.model.TabArgument;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CommandUtils {
    private static String getArgsMessage(List<String> missingArguments) {
        return Core.getInstance().getMessageUtil().get("missing-arguments", missingArguments.toString());
    }

    private static List<String> getMissingArguments(String[] args, Map<Integer, TabArgument> tabArguments) {
        List<String> missingArguments = new ArrayList<>();
        for (TabArgument argument : new ArrayList<>(tabArguments.values()).subList(args.length, tabArguments.size())) {
            if (argument.isRequired()) { missingArguments.add(argument.getName()); }
        }
        return missingArguments;
    }

    public static boolean checkArgs(CommandSender sender, Map<Integer, TabArgument> tabArguments, String[] args) {
        // Check if there are missing arguments
        if (args.length < tabArguments.size()) {
            List<String> missingArguments = getMissingArguments(args, tabArguments);
            if (missingArguments.size() != 0) {
                sender.sendMessage(getArgsMessage(missingArguments));
                return false;
            }
        }

        // Call argument validators
        for (Integer key : tabArguments.keySet()) {
            TabArgument argument = tabArguments.get(key);
            String validateResult = argument.validate(args[key]);
            if (validateResult != null) {
                sender.sendMessage(Core.getInstance().getMessageUtil().get("args-validator-failed", validateResult));
                return false;
            }
        }

        return true;
    }
}
