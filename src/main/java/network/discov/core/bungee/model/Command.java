package network.discov.core.bungee.model;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import network.discov.core.bungee.Core;
import network.discov.core.bungee.util.ArgsValidator;
import network.discov.core.common.TabArgument;

import java.util.HashMap;
import java.util.Objects;

public abstract class Command extends net.md_5.bungee.api.plugin.Command {
    protected HashMap<Integer, TabArgument> arguments = new HashMap<>();
    protected final String description;

    protected Command(String name, String description, String permission, String[] aliases) {
        super(name, permission, aliases);
       this.description = description;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        // Permission check
        if (!sender.hasPermission(Objects.requireNonNull(getPermission()))) {
            sender.sendMessage(TextComponent.fromLegacyText(Core.getInstance().getMessageUtil().get("no-permission")));
            return;
        }

        // Check if there are missing arguments & call validators
        if (!ArgsValidator.checkArgs(sender, arguments, args)) {
            return;
        }

        this.executeCommand(sender, args);
    }

    public HashMap<Integer, TabArgument> getArguments() {
        return arguments;
    }

    public String getDescription() {
        return description;
    }

    public abstract void executeCommand(CommandSender sender, String[] args);
}
