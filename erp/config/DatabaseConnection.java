package com.littlehorizons.erp.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Thread-safe Singleton managing the MySQL JDBC connection pool / lifecycle.
 * Reads configuration from config.properties or provides sensible defaults.
 */
public class DatabaseConnection {

    private static volatile DatabaseConnection instance;
    private final Properties props = new Properties();

    private DatabaseConnection() {
        File configFile = new File("config.properties");
        if (configFile.exists()) {
            try (FileInputStream in = new FileInputStream(configFile)) {
                props.load(in);
            } catch (IOException e) {
                System.err.println("Could not read config.properties: " + e.getMessage());
            }
        } else {
            // Default configuration matching documentation
            props.setProperty("db.url", "jdbc:mysql://localhost:3306/erp_little_horizons?useSSL=false&serverTimezone=America/Bogota&allowPublicKeyRetrieval=true");
            props.setProperty("db.user", "erp_user");
            props.setProperty("db.password", "CHANGE_ME");
        }
    }

    public static DatabaseConnection getInstance() {
        if (instance == null) {
            synchronized (DatabaseConnection.class) {
                if (instance == null) {
                    instance = new DatabaseConnection();
                }
            }
        }
        return instance;
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                props.getProperty("db.url"),
                props.getProperty("db.user"),
                props.getProperty("db.password")
        );
    }

    public boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    public Properties getProperties() {
        return props;
    }
}
