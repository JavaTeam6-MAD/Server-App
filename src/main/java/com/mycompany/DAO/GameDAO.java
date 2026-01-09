package com.mycompany.DAO;

import com.mycompany.config.DBSingleton;
import com.mycompany.model.app.Game;
import java.sql.*;

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
}
