package com.mycompany.service;

import com.mycompany.DAO.GameDAO;
import com.mycompany.model.app.Game;
import com.mycompany.model.requestModel.getGameHistoryRequestModel;

import java.sql.SQLException;
import java.util.List;

public class GameService {
    private GameDAO gameDAO;

    public GameService() {
        this.gameDAO = new GameDAO();
    }

    /**
     * Handles the request to get game history for a specific player
     * 
     * @param req The request containing the player ID
     * @return List of games for the specified player
     * @throws SQLException if database error occurs
     */
    public List<Game> handleGetGameHistory(getGameHistoryRequestModel req) throws SQLException {
        return gameDAO.getGameHistoryByPlayerId(req.getId());
    }
}
