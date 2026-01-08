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

        String createPlayerTable = "CREATE TABLE IF NOT EXISTS Player (" +
                " ID INT AUTO_INCREMENT PRIMARY KEY," +
                " user_name VARCHAR(50) NOT NULL UNIQUE," +
                " hashed_pass VARCHAR(255) NOT NULL," +
                " avatar VARCHAR(50) DEFAULT 'dragon'," +
                " score INT DEFAULT 0," +
                " isActive BOOLEAN DEFAULT FALSE," +
                " isAvailable BOOLEAN DEFAULT FALSE" +
                ")";

        String createGameTable = "CREATE TABLE IF NOT EXISTS Game (" +
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

            // Insert dummy data for testing (only if tables are empty)
            insertDummyData();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Insert dummy players and games for testing
    private void insertDummyData() {
        try (Statement stmt = connection.createStatement()) {

            // Check if data already exists
            var rs = stmt.executeQuery("SELECT COUNT(*) as count FROM Player");
            if (rs.next() && rs.getInt("count") > 0) {
                System.out.println("Database already contains data. Skipping dummy data insertion.");
                return;
            }

            System.out.println("Inserting dummy data for testing...");

            // Insert dummy players
            String insertPlayers = "INSERT INTO Player (user_name, hashed_pass, avatar, score, isActive, isAvailable) VALUES "
                    +
                    "('Alice', 'pass123', 'dragon', 150, FALSE, FALSE), " +
                    "('Bob', 'pass456', 'knight', 120, FALSE, FALSE), " +
                    "('Charlie', 'pass789', 'wizard', 90, FALSE, FALSE), " +
                    "('Diana', 'pass321', 'archer', 200, FALSE, FALSE), " +
                    "('Eve', 'pass654', 'warrior', 80, FALSE, FALSE)";

            stmt.executeUpdate(insertPlayers);

            // Insert dummy games
            String insertGames = "INSERT INTO Game (date, status, player1ID, player2ID) VALUES " +
            // Games for Alice (ID: 1)
                    "(DATEADD('DAY', -5, CURRENT_TIMESTAMP), 1, 1, 2), " + // Alice won against Bob
                    "(DATEADD('DAY', -4, CURRENT_TIMESTAMP), 2, 1, 3), " + // Alice lost to Charlie
                    "(DATEADD('DAY', -3, CURRENT_TIMESTAMP), 3, 1, 4), " + // Alice drew with Diana
                    "(DATEADD('DAY', -2, CURRENT_TIMESTAMP), 1, 1, 5), " + // Alice won against Eve
                    "(DATEADD('DAY', -1, CURRENT_TIMESTAMP), 1, 2, 1), " + // Bob won against Alice
                    // Games for Bob (ID: 2)
                    "(DATEADD('DAY', -6, CURRENT_TIMESTAMP), 1, 2, 3), " + // Bob won against Charlie
                    "(DATEADD('DAY', -4, CURRENT_TIMESTAMP), 2, 2, 4), " + // Bob lost to Diana
                    "(DATEADD('DAY', -3, CURRENT_TIMESTAMP), 3, 5, 2), " + // Eve drew with Bob
                    // Games for Charlie (ID: 3)
                    "(DATEADD('DAY', -5, CURRENT_TIMESTAMP), 1, 3, 4), " + // Charlie won against Diana
                    "(DATEADD('DAY', -2, CURRENT_TIMESTAMP), 2, 5, 3), " + // Charlie lost to Eve
                    // Games for Diana (ID: 4)
                    "(DATEADD('DAY', -7, CURRENT_TIMESTAMP), 1, 4, 5), " + // Diana won against Eve
                    "(DATEADD('DAY', -1, CURRENT_TIMESTAMP), 3, 4, 3), " + // Diana drew with Charlie
                    // Games for Eve (ID: 5)
                    "(DATEADD('DAY', -6, CURRENT_TIMESTAMP), 2, 1, 5)"; // Eve lost to Alice

            stmt.executeUpdate(insertGames);

            System.out.println("Dummy data inserted successfully!");
            System.out.println("Test users: Alice, Bob, Charlie, Diana, Eve");
            System.out.println("Password for all: pass123, pass456, pass789, pass321, pass654");

        } catch (SQLException e) {
            System.err.println("Error inserting dummy data: " + e.getMessage());
        }
    }
}
