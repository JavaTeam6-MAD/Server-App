package com.mycompany.manager;

import java.util.UUID;

public class GameSession {
    private String gameId; // Using UUID String
    private int player1Id; // "X"
    private int player2Id; // "O"
    private String player1Name;
    private String player2Name;
    
    private String[][] board;
    private String currentTurn; // "X" or "O"
    private boolean isGameOver;
    private String winner; // "X", "O", or "Draw"

    public GameSession(int player1Id, String player1Name, int player2Id, String player2Name) {
        this.gameId = UUID.randomUUID().toString();
        this.player1Id = player1Id;
        this.player1Name = player1Name;
        this.player2Id = player2Id;
        this.player2Name = player2Name;
        this.board = new String[3][3];
        this.currentTurn = "X"; // X always starts
        this.isGameOver = false;
        this.winner = null;
    }

    public String getGameId() {
        return gameId;
    }

    public int getPlayer1Id() {
        return player1Id;
    }

    public int getPlayer2Id() {
        return player2Id;
    }
    
    public String getPlayer1Name() {
        return player1Name;
    }

    public String getPlayer2Name() {
        return player2Name;
    }

    public String getCurrentTurn() {
        return currentTurn;
    }

    public boolean isGameOver() {
        return isGameOver;
    }

    public String getWinner() {
        return winner;
    }

    // Returns true if move was successful
    public boolean makeMove(int playerId, int row, int col) {
        if (isGameOver) return false;
        
        // Validate player turn
        String symbol = (playerId == player1Id) ? "X" : (playerId == player2Id ? "O" : null);
        if (symbol == null || !symbol.equals(currentTurn)) {
            return false;
        }

        // Validate board position
        if (row < 0 || row > 2 || col < 0 || col > 2 || board[row][col] != null) {
            return false;
        }

        // Apply move
        board[row][col] = symbol;
        
        // Check game state
        checkGameState();
        
        // Switch turn if game not over
        if (!isGameOver) {
            currentTurn = currentTurn.equals("X") ? "O" : "X";
        }
        
        return true;
    }

    private void checkGameState() {
        // Check rows, cols, diagonals
        for (int i = 0; i < 3; i++) {
            if (checkLine(board[i][0], board[i][1], board[i][2])) return;
            if (checkLine(board[0][i], board[1][i], board[2][i])) return;
        }
        if (checkLine(board[0][0], board[1][1], board[2][2])) return;
        if (checkLine(board[0][2], board[1][1], board[2][0])) return;

        // Check draw
        boolean isFull = true;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (board[i][j] == null) {
                    isFull = false;
                    break;
                }
            }
        }
        
        if (isFull) {
            isGameOver = true;
            winner = "Draw";
        }
    }

    private boolean checkLine(String a, String b, String c) {
        if (a != null && a.equals(b) && b.equals(c)) {
            isGameOver = true;
            winner = a;
            return true;
        }
        return false;
    }
}
