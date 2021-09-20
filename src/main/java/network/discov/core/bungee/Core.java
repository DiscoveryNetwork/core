package network.discov.core.bungee;

import net.md_5.bungee.api.plugin.Plugin;

public class Core extends Plugin {
    private static Core instance;

    public Core() { instance = this; }

    @Override
    public void onLoad() {
        super.onLoad();
    }

    @Override
    public void onEnable() {
        super.onEnable();
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    public Core getInstance() {
        return instance;
    }
}
