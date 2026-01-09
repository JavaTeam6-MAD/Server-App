package com.mycompany.manager;

import com.mycompany.model.responseModel.MakeMoveResponseModel;

public class GameHandler {
    private GameSession session;

    public GameHandler(GameSession session) {
        this.session = session;
    }

    public synchronized MakeMoveResponseModel processMove(int playerId, int row, int col) {
        boolean success = session.makeMove(playerId, row, col);
        
        if (!success) {
            return null; // Invalid move
        }

        String symbol = (playerId == session.getPlayer1Id()) ? "X" : "O";
        
        return new MakeMoveResponseModel(
            row, 
            col, 
            playerId,
            symbol,
            session.isGameOver(),
            session.getWinner()
        );
    }
    
    public GameSession getSession() {
        return session;
    }
}
