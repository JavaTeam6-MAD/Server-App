package com.mycompany.manager;

import com.mycompany.DAO.PlayerDAO;
import com.mycompany.model.app.Player;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

public class ServerScreenManager {

    private final PlayerDAO playerDAO;

    public ServerScreenManager() {
        this.playerDAO = new PlayerDAO();
    }

    public List<Player> getAllPlayers() {
        try {
            return playerDAO.getAllPlayers();
        } catch (SQLException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public int getOnlineCount() {
        List<Player> players = getAllPlayers();
        return (int) players.stream().filter(Player::isIsActive).count();
    }

    public int getOfflineCount() {
        List<Player> players = getAllPlayers();
        return (int) players.stream().filter(p -> !p.isIsActive()).count();
    }
}
