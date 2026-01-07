package com.mycompany.DAO;



import com.mycompany.config.DBSingleton;
import com.mycompany.model.app.Player;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PlayerDAO {

    private final Connection connection;

    public PlayerDAO() {
        this.connection = DBSingleton.getConnection();
    }


    public Player insertPlayer(Player player) throws SQLException {
        String sql = " INSERT INTO Player (user_name, hashed_pass, avatar, score, isActive, isAvailable)VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps =
                     connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, player.getUserName());
            ps.setString(2, player.getPassword());
            ps.setString(3, player.getAvatar());
            ps.setLong(4, player.getScore());
            ps.setBoolean(5, player.isIsActive());
            ps.setBoolean(6, player.isIsAvailable());

            int affectedRows = ps.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Inserting player failed, no rows affected. :)");
            }

            // get generated ID
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    player.setId(rs.getInt(1));
                }
            }
        }catch (SQLIntegrityConstraintViolationException e) {
            throw e;
        }

        return player;
    }


    public Player getPlayerById(int id) throws SQLException {
        String sql = "SELECT * FROM Player WHERE ID = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, id);

        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return mapRowToPlayer(rs);
        }
        return null;
    }

    public Player getPlayerByUsername(String username) throws SQLException {
        String sql = "SELECT * FROM Player WHERE user_name = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, username);

        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return mapRowToPlayer(rs);
        }
        return null;
    }
    public Player getPlayerByUsernameAndPassword(String username, String password) throws SQLException {
        String sql = "SELECT * FROM Player WHERE user_name = ? AND hashed_pass = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            // 2. Set both parameters
            ps.setString(1, username);
            ps.setString(2, password);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    // 3. If a match is found, map and return the player
                    return mapRowToPlayer(rs);
                }
            }
        }
        // Return null if no match is found
        return null;
    }

    public List<Player> getAllPlayers() throws SQLException {
        String sql = "SELECT * FROM Player";
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(sql);

        List<Player> players = new ArrayList<>();
        while (rs.next()) {
            players.add(mapRowToPlayer(rs));
        }
        return players;
    }


    public void updateScore(int playerId, long newScore) throws SQLException {
        String sql = "UPDATE Player SET score = ? WHERE ID = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setLong(1, newScore);
        ps.setInt(2, playerId);
        ps.executeUpdate();
    }

    public void updatePlayerStatus(int playerId, boolean isActive, boolean isAvailable) throws SQLException {
        String sql = "UPDATE Player SET isActive = ?, isAvailable = ? WHERE ID = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setBoolean(1, isActive);
        ps.setBoolean(2, isAvailable);
        ps.setInt(3, playerId);
        ps.executeUpdate();
    }


    public void deletePlayer(int playerId) throws SQLException {
        String sql = "DELETE FROM Player WHERE ID = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, playerId);
        ps.executeUpdate();
    }


    private Player mapRowToPlayer(ResultSet rs) throws SQLException {
        return new Player(
                rs.getInt("ID"),
                rs.getString("user_name"),
                rs.getString("hashed_pass"),
                rs.getString("avatar"),
                rs.getLong("score"),
                rs.getBoolean("isActive"),
                rs.getBoolean("isAvailable")
        );
    }
}
