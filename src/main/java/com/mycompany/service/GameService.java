package com.mycompany.service;

import com.mycompany.DAO.GameDAO;
import com.mycompany.model.app.RecordedGame;
import com.mycompany.model.requestModel.getGameHistoryRequestModel;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class GameService {

    private GameDAO gameDAO;

    public GameService() {
        this.gameDAO = new GameDAO();
    }

    /**
     * Handle game history request for a specific player
     * 
     * @param request The game history request containing player ID
     * @return List of RecordedGame objects, or list with single empty game (id=0)
     *         if no games found
     * @throws SQLException if database error occurs
     */
    public List<RecordedGame> handleGameHistoryRequest(getGameHistoryRequestModel request) throws SQLException {
        List<RecordedGame> games = gameDAO.getGamesByPlayerId(request.getId());

        // If no games found, return empty list with a special marker game (id = 0)
        if (games.isEmpty()) {
            RecordedGame emptyGame = new RecordedGame();
            emptyGame.setGameId(0); // Indicates no game history
            games = new ArrayList<>();
            games.add(emptyGame);
        }

        return games;
    }

    /**
     * Record a new game
     * 
     * @param player1Id ID of player 1
     * @param player2Id ID of player 2
     * @param status    Game status (1: player1 won, 2: player2 won, 3: draw)
     * @return The generated game ID
     * @throws SQLException if database error occurs
     */
    public int recordGame(int player1Id, int player2Id, int status) throws SQLException {
        return gameDAO.insertGame(player1Id, player2Id, status);
    }
}
