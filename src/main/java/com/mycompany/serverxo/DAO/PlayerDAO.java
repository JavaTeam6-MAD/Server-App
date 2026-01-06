package com.mycompany.serverxo.DAO;


import com.mycompany.serverxo.Entity.Player;
import com.mycompany.serverxo.config.DBSingleton;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PlayerDAO {

    private final Connection connection;

    public PlayerDAO() {
        this.connection = DBSingleton.getConnection();
    }

    public void insertPlayer(Player player) throws SQLException {
        String sql = " INSERT INTO Player (user_name, hashed_pass, char_no, score, isActive, isAvailable)VALUES (?, ?, ?, ?, ?, ?)";

        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, player.getUserName());
        ps.setString(2, player.getPassword());
        ps.setString(3, String.valueOf(player.getCharacter()));
        ps.setLong(4, player.getScore());
        ps.setBoolean(5, player.isActive());
        ps.setBoolean(6, player.isAvailable());
        ps.executeUpdate();
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
                Integer.parseInt(rs.getString("char_no")),
                rs.getLong("score"),
                rs.getBoolean("isActive"),
                rs.getBoolean("isAvailable")
        );
    }
}
