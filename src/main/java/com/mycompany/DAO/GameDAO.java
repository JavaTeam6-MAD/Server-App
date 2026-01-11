package com.mycompany.DAO;

import com.mycompany.config.DBSingleton;
import com.mycompany.model.app.Game;
import com.mycompany.model.utils.GameStatus;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GameDAO {
    private final Connection connection;

    public GameDAO() {
        this.connection = DBSingleton.getConnection();
    }

    public int insertGame(Game game, int status) throws SQLException {
        String sql = "INSERT INTO Game (status, player1ID, player2ID, isRecorded1, isRecorded2) VALUES (?, ?, ?, ?, ?)";
        
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            ps.setInt(1, status);
            ps.setInt(2, game.getPlayer1().getId());
            ps.setInt(3, game.getPlayer2().getId());
            ps.setBoolean(4, false); // Default isRecorded
            ps.setBoolean(5, false);

            int affectedRows = ps.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating game failed, no rows affected.");
            }

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Creating game failed, no ID obtained.");
                }
            }
        }
    }

    public List<Game> getAllGames() throws SQLException {
        List<Game> games = new ArrayList<>();

        String sql = "SELECT * FROM Game";

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            PlayerDAO playerDAO = new PlayerDAO(); // أنشئه مرة واحدة

            while (rs.next()) {
                Game game = new Game();

                game.setId(rs.getInt("ID"));
                game.setDate(rs.getTimestamp("date"));

                // status (INT -> ENUM)
                int statusInt = rs.getInt("status");
                game.setStatus(GameStatus.values()[statusInt - 1]);
                // لو status في DB من 1 → 3

                int player1Id = rs.getInt("player1ID");
                int player2Id = rs.getInt("player2ID");

                game.setPlayer1(playerDAO.getPlayerById(player1Id));
                game.setPlayer2(playerDAO.getPlayerById(player2Id));

                games.add(game);
            }
        }

        return games;
    }


}
