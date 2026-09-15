package com.healthfirst.pims.db;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class DatabaseManager {

    public static final String MODE_H2 = "h2";
    public static final String MODE_MYSQL = "mysql";

    private static final Properties CONFIG = new Properties();
    private static String activeMode = MODE_H2;
    private static String jdbcUrl;
    private static String jdbcUser;
    private static String jdbcPassword;

    static {
        loadConfig();
        resolveConnection();
    }

    private DatabaseManager() {
    }

    private static void loadConfig() {
        CONFIG.setProperty("db.mode", MODE_H2);
        CONFIG.setProperty("mysql.host", "localhost");
        CONFIG.setProperty("mysql.port", "3306");
        CONFIG.setProperty("mysql.database", "healthfirst_pims");
        CONFIG.setProperty("mysql.user", "root");
        CONFIG.setProperty("mysql.password", "");

        try (InputStream in = DatabaseManager.class.getResourceAsStream("/config.properties")) {
            if (in != null) {
                CONFIG.load(in);
            }
        } catch (IOException ignored) {
            // bundled defaults are fine
        }

        Path external = Paths.get("config.properties");
        if (Files.exists(external)) {
            try (InputStream in = Files.newInputStream(external)) {
                CONFIG.load(in);
            } catch (IOException ignored) {
                // keep previous values
            }
        }
    }

    private static void resolveConnection() {
        activeMode = CONFIG.getProperty("db.mode", MODE_H2).trim().toLowerCase();
        if (MODE_MYSQL.equals(activeMode)) {
            String host = CONFIG.getProperty("mysql.host", "localhost");
            String port = CONFIG.getProperty("mysql.port", "3306");
            String database = CONFIG.getProperty("mysql.database", "healthfirst_pims");
            jdbcUrl = "jdbc:mysql://" + host + ":" + port + "/" + database
                    + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Africa/Johannesburg&characterEncoding=utf8";
            jdbcUser = CONFIG.getProperty("mysql.user", "root");
            jdbcPassword = CONFIG.getProperty("mysql.password", "");
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException("MySQL JDBC driver is missing", e);
            }
        } else {
            activeMode = MODE_H2;
            Path dataDir = applicationDirectory().resolve("data");
            try {
                Files.createDirectories(dataDir);
            } catch (IOException e) {
                throw new IllegalStateException("Unable to create data directory: " + dataDir, e);
            }
            Path dbFile = dataDir.resolve("healthfirst_pims");
            jdbcUrl = "jdbc:h2:" + dbFile.toAbsolutePath()
                    + ";MODE=MySQL;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE;AUTO_SERVER=TRUE";
            jdbcUser = "sa";
            jdbcPassword = "";
            try {
                Class.forName("org.h2.Driver");
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException("H2 JDBC driver is missing", e);
            }
        }
    }

    public static Path applicationDirectory() {
        try {
            Path codeSource = Path.of(DatabaseManager.class.getProtectionDomain()
                    .getCodeSource().getLocation().toURI());
            if (Files.isRegularFile(codeSource) && codeSource.toString().endsWith(".jar")) {
                return codeSource.getParent();
            }
        } catch (Exception ignored) {
            // fall through to working directory
        }
        return Paths.get(System.getProperty("user.dir"));
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcPassword);
    }

    public static String getActiveMode() {
        return activeMode;
    }

    public static String getJdbcUrl() {
        return jdbcUrl;
    }

    public static void initialize() throws SQLException {
        try (Connection connection = getConnection()) {
            SchemaInitializer.initialize(connection);
        }
    }
}
