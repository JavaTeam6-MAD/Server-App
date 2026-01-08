package com.mycompany.DAO;

import com.mycompany.config.DBSingleton;
import com.mycompany.model.app.RecordedGame;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GameDAO {

    private final Connection connection;

    public GameDAO() {
        this.connection = DBSingleton.getConnection();
    }

    /**
     * Get all games for a specific player (where they were either player1 or
     * player2)
     * 
     * @param playerId The ID of the player
     * @return List of RecordedGame objects
     * @throws SQLException if database error occurs
     */
    public List<RecordedGame> getGamesByPlayerId(int playerId) throws SQLException {
        String sql = "SELECT g.ID as gameId, g.date, g.status, " +
                "g.player1ID, p1.user_name as player1Name, p1.avatar as player1Avatar, " +
                "g.player2ID, p2.user_name as player2Name, p2.avatar as player2Avatar " +
                "FROM Game g " +
                "JOIN Player p1 ON g.player1ID = p1.ID " +
                "JOIN Player p2 ON g.player2ID = p2.ID " +
                "WHERE g.player1ID = ? OR g.player2ID = ? " +
                "ORDER BY g.date DESC";

        List<RecordedGame> games = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, playerId);
            ps.setInt(2, playerId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    games.add(mapRowToRecordedGame(rs));
                }
            }
        }

        return games;
    }

    /**
     * Insert a new game record into the database
     * 
     * @param player1Id ID of player 1
     * @param player2Id ID of player 2
     * @param status    Game status (1: player1 won, 2: player2 won, 3: draw)
     * @return The generated game ID
     * @throws SQLException if database error occurs
     */
    public int insertGame(int player1Id, int player2Id, int status) throws SQLException {
        String sql = "INSERT INTO Game (player1ID, player2ID, status, date) VALUES (?, ?, ?, ?)";

        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, player1Id);
            ps.setInt(2, player2Id);
            ps.setInt(3, status);
            ps.setTimestamp(4, new Timestamp(System.currentTimeMillis()));

            int affectedRows = ps.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Inserting game failed, no rows affected.");
            }

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }

        throw new SQLException("Inserting game failed, no ID obtained.");
    }

    /**
     * Map a ResultSet row to a RecordedGame object
     * 
     * @param rs The ResultSet
     * @return RecordedGame object
     * @throws SQLException if database error occurs
     */
    private RecordedGame mapRowToRecordedGame(ResultSet rs) throws SQLException {
        return new RecordedGame(
                rs.getInt("gameId"),
                rs.getTimestamp("date"),
                rs.getInt("status"),
                rs.getInt("player1ID"),
                rs.getString("player1Name"),
                rs.getString("player1Avatar"),
                rs.getInt("player2ID"),
                rs.getString("player2Name"),
                rs.getString("player2Avatar"));
    }
}
