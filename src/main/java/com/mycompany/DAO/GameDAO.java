package com.mycompany.DAO;

import com.mycompany.config.DBSingleton;
import com.mycompany.model.app.Game;
import com.mycompany.model.app.Player;
import com.mycompany.model.utils.GameStatus;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GameDAO {

    private final Connection connection;
    private final PlayerDAO playerDAO;

    public GameDAO() {
        this.connection = DBSingleton.getConnection();
        this.playerDAO = new PlayerDAO();
    }

    /**
     * Returns game history for a given player ID from the database.
     * Fetches all games where the player is either player1 or player2.
     */
    public List<Game> getGameHistoryByPlayerId(int playerId) throws SQLException {
        List<Game> games = new ArrayList<>();

        String sql = "SELECT * FROM Game WHERE player1ID = ? OR player2ID = ? ORDER BY date DESC";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, playerId);
            ps.setInt(2, playerId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Game game = mapRowToGame(rs);
                    games.add(game);
                }
            }
        }

        return games;
    }

    /**
     * Maps a database row to a Game object
     */
    private Game mapRowToGame(ResultSet rs) throws SQLException {
        Game game = new Game();
        game.setId(rs.getInt("ID"));
        game.setDate(rs.getTimestamp("date"));
        game.setScore(rs.getLong("score"));
        game.setIsRecorded(rs.getBoolean("isRecorded"));

        // Map status integer to GameStatus enum
        int statusCode = rs.getInt("status");
        game.setStatus(mapStatusCodeToEnum(statusCode));

        // Fetch player1 and player2 details
        int player1Id = rs.getInt("player1ID");
        int player2Id = rs.getInt("player2ID");

        try {
            Player player1 = playerDAO.getPlayerById(player1Id);
            Player player2 = playerDAO.getPlayerById(player2Id);

            game.setPlayer1(player1);
            game.setPlayer2(player2);
        } catch (SQLException e) {
            System.err.println("Error fetching player details for game " + game.getId());
            e.printStackTrace();
        }

        return game;
    }

    /**
     * Maps database status code to GameStatus enum
     * 1: player1 won (WIN for player1, LOSE for player2)
     * 2: player2 won (LOSE for player1, WIN for player2)
     * 3: draw
     */
    private GameStatus mapStatusCodeToEnum(int statusCode) {
        switch (statusCode) {
            case 1:
                return GameStatus.WIN; // Player1 won
            case 2:
                return GameStatus.LOSE; // Player2 won (means player1 lost)
            case 3:
                return GameStatus.DRAW;
            default:
                return GameStatus.DRAW;
        }
    }
}
