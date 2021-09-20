package network.discov.core.common;

import java.sql.*;

public class DatabaseConnector {
    private final String host, database, username, password;
    private Connection connection;

    public DatabaseConnector(String host, String username, String password, String database) {
        this.host = host;
        this.username = username;
        this.password = password;
        this.database = database;
    }

    private void ensureConnection() throws SQLException, ClassNotFoundException {
        if (connection != null && !connection.isClosed()) { return; }

        synchronized (this) {
            if (connection != null && !connection.isClosed()) { return; }
            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection(String.format("jdbc:mysql://%s:%d/%s?autoReconnect=true&useSSL=false", host, 3306, database), username, password);
        }
    }

    public ResultSet executeStatement(String query) throws SQLException, ClassNotFoundException {
        ensureConnection();
        Statement statement = connection.createStatement();
        return statement.executeQuery(query);
    }
}
