package com.mycompany.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DBSingleton {

    private static Connection connection;

    // H2 embedded database (file-based)
    // DB file will be created in project root
    private static final String URL = "jdbc:h2:./gamesystem;DB_CLOSE_DELAY=-1";
    private static final String USER = "sa";
    private static final String PASSWORD = "";

    // Private constructor (Singleton)
    private DBSingleton() {
        try {
            // Load H2 driver (optional in newer versions, but safe)
            Class.forName("org.h2.Driver");

            // Connect to H2
            connection = DriverManager.getConnection(URL, USER, PASSWORD);

            // Create tables
            createTables();

        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    // Singleton getter
    public static synchronized Connection getConnection() {
        if (connection == null) {
            new DBSingleton();
        }
        return connection;
    }

    // Create tables
    private void createTables() {

        String createPlayerTable =
                "CREATE TABLE IF NOT EXISTS Player (" +
                        " ID INT AUTO_INCREMENT PRIMARY KEY," +
                        " user_name VARCHAR(50) NOT NULL UNIQUE," +
                        " hashed_pass VARCHAR(255) NOT NULL," +
                        " avatar VARCHAR(8) DEFAULT 'dragon'," +
                        " score INT DEFAULT 0," +
                        " isActive BOOLEAN DEFAULT FALSE," +
                        " isAvailable BOOLEAN DEFAULT FALSE" +
                        ")";

        String createGameTable =
                "CREATE TABLE IF NOT EXISTS Game (" +
                        " ID INT AUTO_INCREMENT PRIMARY KEY," +
                        " date TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                        " status INT," +
                        " player1ID INT NOT NULL," +
                        " player2ID INT NOT NULL," +
                        " CONSTRAINT fk_player1 FOREIGN KEY (player1ID) REFERENCES Player(ID)," +
                        " CONSTRAINT fk_player2 FOREIGN KEY (player2ID) REFERENCES Player(ID)" +
                        ")";

        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(createPlayerTable);
            stmt.executeUpdate(createGameTable);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
