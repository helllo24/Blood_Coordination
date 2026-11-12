package com.blooddonation.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Centralized JDBC connection utility for MySQL database operations.
 * Loads configuration from db.properties with environment variable fallbacks.
 */
public class DBConnectionUtil {

    private static String jdbcUrl;
    private static String jdbcUser;
    private static String jdbcPassword;
    private static String jdbcDriver;

    static {
        loadConfiguration();
        try {
            Class.forName(jdbcDriver);
        } catch (ClassNotFoundException e) {
            System.err.println("[DBConnectionUtil] MySQL JDBC Driver not found: " + e.getMessage());
        }
    }

    private DBConnectionUtil() {
        // Prevent instantiation
    }

    /**
     * Loads database credentials centrally from db.properties or system environment.
     */
    private static void loadConfiguration() {
        Properties props = new Properties();
        try (InputStream input = DBConnectionUtil.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (input != null) {
                props.load(input);
            }
        } catch (IOException e) {
            System.err.println("[DBConnectionUtil] Could not read db.properties, using fallback defaults: " + e.getMessage());
        }

        // Support environment variables / system properties with priority, then properties file, then defaults
        jdbcUrl = getSetting(props, "db.url", "DB_URL", "jdbc:mysql://localhost:3306/blood_donor_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true");
        jdbcUser = getSetting(props, "db.user", "DB_USER", "root");
        jdbcPassword = getSetting(props, "db.password", "DB_PASSWORD", "root@1234");
        jdbcDriver = getSetting(props, "db.driver", "DB_DRIVER", "com.mysql.cj.jdbc.Driver");
    }

    private static String getSetting(Properties props, String propKey, String envKey, String defaultValue) {
        String envVal = System.getenv(envKey);
        if (envVal != null && !envVal.isBlank()) {
            return envVal;
        }
        String sysVal = System.getProperty(propKey);
        if (sysVal != null && !sysVal.isBlank()) {
            return sysVal;
        }
        String propVal = props.getProperty(propKey);
        if (propVal != null && !propVal.isBlank()) {
            return propVal;
        }
        return defaultValue;
    }

    /**
     * Obtains a live JDBC Connection to the MySQL database.
     *
     * @return active java.sql.Connection
     * @throws SQLException if a database access error occurs
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcPassword);
    }

    /**
     * Safely closes one or more AutoCloseable resources without throwing checked exceptions.
     *
     * @param resources Resources such as Connection, PreparedStatement, or ResultSet
     */
    public static void closeQuietly(AutoCloseable... resources) {
        if (resources == null) return;
        for (AutoCloseable res : resources) {
            if (res != null) {
                try {
                    res.close();
                } catch (Exception e) {
                    System.err.println("[DBConnectionUtil] Error closing resource: " + e.getMessage());
                }
            }
        }
    }
}
