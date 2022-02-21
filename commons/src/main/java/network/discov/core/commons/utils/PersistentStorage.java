package network.discov.core.commons.utils;

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
        return connection.get(getKeyString(uuid, key));
    }

    public void setPlayerValue(UUID uuid, String key, String value) {
        connection.set(getKeyString(uuid, key), value);
    }

    private String getKeyString(UUID uuid, String key) {
        return String.format("%s||%s", uuid.toString(), key);
    }
}