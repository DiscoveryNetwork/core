package network.discov.core.bungee.util;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import network.discov.core.bungee.Core;
import network.discov.core.common.CommonUtils;
import network.discov.core.common.TabArgument;

import java.util.List;
import java.util.Map;

public class ArgsValidator {
    public static boolean checkArgs(CommandSender sender, Map<Integer, TabArgument> tabArguments, String[] args) {
        // Check if there are missing arguments
        if (args.length < tabArguments.size()) {
            List<String> missingArguments = CommonUtils.getMissingArguments(args, tabArguments);
            if (missingArguments.size() != 0) {
                sender.sendMessage(TextComponent.fromLegacyText(CommonUtils.getArgsMessage(missingArguments, Core.getInstance().getMessageUtil())));
                return false;
            }
        }

        // Call argument validators
        for (Integer key : tabArguments.keySet()) {
            TabArgument argument = tabArguments.get(key);
            if (!argument.isValid(args[key])) {
                sender.sendMessage(TextComponent.fromLegacyText(Core.getInstance().getMessageUtil().get("validator-failed")));
                return false;
            }
        }

        return true;
    }
}
