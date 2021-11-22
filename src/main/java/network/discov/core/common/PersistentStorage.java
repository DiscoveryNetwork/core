package network.discov.core.common;

import network.discov.core.spigot.Core;
import redis.clients.jedis.Jedis;

import java.util.UUID;

public class PersistentStorage {
    private final Jedis connection;

    public PersistentStorage(String host, int port, String password) {
        connection = new Jedis(host, port);
        connection.auth(password);
    }

    public String getValue(String key) {
        return connection.get(key);
    }

    public void setValue(String key, String value) {
        connection.set(key, value);
    }

    public String getPlayerValue(UUID uuid, String key) {
        String value = connection.get(getKeyString(uuid, key));
        Core.getInstance().getLogger().info(String.format("UUID: %s | Key: %s | Value: %s", uuid, key, value));
        return value;
    }

    public void setPlayerValue(UUID uuid, String key, String value) {
        connection.set(getKeyString(uuid, key), value);
    }

    private String getKeyString(UUID uuid, String key) {
        return String.format("%s||%s", uuid.toString(), key);
    }
}