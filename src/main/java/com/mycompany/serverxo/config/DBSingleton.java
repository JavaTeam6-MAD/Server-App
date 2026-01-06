package com.mycompany.serverxo.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DBSingleton {
    private static Connection connection;//null
    private final String url = "jdbc:mysql://localhost:3306/";
    private final String dbName = "GameSystem";
    private final String user = "root"; // your MySQL username
    private final String password = "123!"; // your MySQL password

    // Private constructor for Singleton
    private DBSingleton() {
        try {
            // Connect to MySQL without specifying DB first
            connection = DriverManager.getConnection(url, user, password);

            // Create database if it doesn't exist
            try (Statement stmt = connection.createStatement()) {
                stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + dbName);
            }

            // Connect to mySQL table
           connection = DriverManager.getConnection(url + dbName, user, password);

            // Create tables if they don't exist
            createTables();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Singleton getter

    public synchronized static Connection getConnection(){
        if(connection ==null){
            synchronized (DBSingleton.class) {
                     new DBSingleton();//create connection
            }
        }
        return connection;
    }



    // Method to create tables
    private void createTables() {
        String createPlayerTable = " CREATE TABLE IF NOT EXISTS Player " +
                "(ID INT AUTO_INCREMENT PRIMARY KEY NOT NULL," +
                " user_name VARCHAR(50) NOT NULL UNIQUE," +
                " hashed_pass VARCHAR(255) NOT NULL," +
                " char_no VARCHAR(8) DEFAULT 'dragon'," +
                " score INT DEFAULT 0," +
                " isActive BOOLEAN DEFAULT FALSE," +
                " isAvailable BOOLEAN DEFAULT FALSE);";


        String createGameTable = "CREATE TABLE IF NOT EXISTS Game" +
                " (ID INT AUTO_INCREMENT PRIMARY KEY NOT NULL," +
                " date DATETIME DEFAULT CURRENT_TIMESTAMP," +
                " status INT COMMENT '1: player1 won, 2: player2 won, 3: draw'," +
                " player1ID INT NOT NULL, player2ID INT NOT NULL," +
                " CONSTRAINT fk_player1 FOREIGN KEY (player1ID) REFERENCES Player(ID)," +
                " CONSTRAINT fk_player2 FOREIGN KEY (player2ID) REFERENCES Player(ID))";

        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(createPlayerTable);
            stmt.executeUpdate(createGameTable);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

