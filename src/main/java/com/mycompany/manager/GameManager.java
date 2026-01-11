package com.mycompany.manager;

import com.mycompany.DAO.GameDAO;
import com.mycompany.controller.PlayerHandler;
import com.mycompany.model.app.Game;
import com.mycompany.model.app.Player;
import com.mycompany.model.requestModel.EndGameSessionRequestModel;
import com.mycompany.model.requestModel.ReceiveChallengeRequestModel;
import com.mycompany.model.responseModel.ReceiveChallengeResponseModel;
import com.mycompany.model.responseModel.MakeMoveResponseModel;
import com.mycompany.model.notification.ServerShutdownNotification;
import com.mycompany.service.PlayerService;
import com.mycompany.model.utils.GameStatus;

import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GameManager {
    private static GameManager instance;
    private Map<Integer, PlayerHandler> onlinePlayers;
    private Map<String, GameHandler> activeGames;
    private Map<Integer, String> playerGameMap; // Fast lookup player -> gameId (String)

    private GameDAO gameDAO;
    private PlayerService playerService;

    private GameManager() {
        onlinePlayers = new ConcurrentHashMap<>();
        activeGames = new ConcurrentHashMap<>();
        playerGameMap = new ConcurrentHashMap<>();
        gameDAO = new GameDAO();
        playerService = new PlayerService();
    }

    public static synchronized GameManager getInstance() {
        if (instance == null) {
            instance = new GameManager();
        }
        return instance;
    }

    // === Player Management ===
    public void addPlayer(int playerId, PlayerHandler handler) {
        onlinePlayers.put(playerId, handler);
    }

    public void removePlayer(int playerId) {
        onlinePlayers.remove(playerId);
        GameHandler game = getGameByPlayerId(playerId);
        if (game != null) {
            // Forfeit or End Game?
            // For now, simple removal.
            activeGames.remove(game.getSession().getGameId().toString());
            playerGameMap.remove(game.getSession().getPlayer1Id());
            playerGameMap.remove(game.getSession().getPlayer2Id());
        }
    }

    /**
     * Broadcast server shutdown notification to all connected clients
     */
    public void broadcastServerShutdown() {
        ServerShutdownNotification notification = new ServerShutdownNotification("Server is shutting down");
        System.out.println("Broadcasting shutdown notification to " + onlinePlayers.size() + " clients");

        for (Map.Entry<Integer, PlayerHandler> entry : onlinePlayers.entrySet()) {
            try {
                PlayerHandler handler = entry.getValue();
                if (handler != null) {
                    handler.sendRequest(notification);
                    System.out.println("Sent shutdown notification to player " + entry.getKey());
                }
            } catch (Exception e) {
                System.err.println(
                        "Failed to send shutdown notification to player " + entry.getKey() + ": " + e.getMessage());
            }
        }
    }

    // === Challenge Logic ===
    public void sendChallenge(int challengerId, String challengerName, int opponentId) {
        PlayerHandler opponent = onlinePlayers.get(opponentId);
        if (opponent != null) {
            // Updated model usage
            opponent.sendRequest(new ReceiveChallengeRequestModel(challengerId, opponentId, challengerName));
        } else {
            /// TODO handle
        }
    }

    public void handleChallengeResponse(int opponentId, String opponentName, int challengerId, boolean isAccepted) {
        PlayerHandler challenger = onlinePlayers.get(challengerId);
        PlayerHandler opponent = onlinePlayers.get(opponentId);

        if (challenger == null)
            return;

        if (isAccepted && opponent != null) {
            // Start Game
            GameSession session = new GameSession(challengerId, challenger.getPlayerName(), opponentId, opponentName);
            GameHandler gameHandler = new GameHandler(session);

            String gameId = session.getGameId().toString();
            activeGames.put(gameId, gameHandler);
            playerGameMap.put(challengerId, gameId);
            playerGameMap.put(opponentId, gameId);

            try {
                // Set Players to In Game
                playerService.updatePlayerStatus(challengerId, true, false);
                playerService.updatePlayerStatus(opponentId, true, false);
            } catch (SQLException e) {
                e.printStackTrace();
            }

            // Notify both - using ReceiveChallengeResponseModel as Start Game signal
            // Sender in Model = Challenger (Player 1)
            // Receiver in Model = Opponent (Player 2)
            // Notify both - using ReceiveChallengeResponseModel as Start Game signal
            // Sender in Model = Challenger (Player 1)
            // Receiver in Model = Opponent (Player 2)
            String challengerName = challenger.getPlayerName(); // Safe access, checked above

            long challengerScore = 0;
            long opponentScore = 0;
            // Fetch latest scores
            try {
                Player chP = playerService.getPlayerById(challengerId);
                if (chP != null)
                    challengerScore = chP.getScore();
                Player opP = playerService.getPlayerById(opponentId);
                if (opP != null)
                    opponentScore = opP.getScore();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }

            ReceiveChallengeResponseModel responseToChallenger = new ReceiveChallengeResponseModel(
                    opponentId, challengerId, true, false, gameId, challengerName, opponentName, challengerScore,
                    opponentScore);
            ReceiveChallengeResponseModel responseToOpponent = new ReceiveChallengeResponseModel(
                    challengerId, opponentId, true, false, gameId, challengerName, opponentName, challengerScore,
                    opponentScore);

            challenger.sendRequest(responseToChallenger);
            opponent.sendRequest(responseToOpponent);

        } else {
            // Notify rejection
            challenger.sendRequest(new ReceiveChallengeResponseModel(opponentId, challengerId, false, false));
        }
    }

    // === Game Logic ===
    public GameHandler getGame(String gameId) {
        return activeGames.get(gameId);
    }

    public GameHandler getGameByPlayerId(int playerId) {
        String gameId = playerGameMap.get(playerId);
        return (gameId != null) ? activeGames.get(gameId) : null;
    }

    public void removeGame(String gameId) {
        GameHandler handler = activeGames.remove(gameId);
        if (handler != null) {
            playerGameMap.remove(handler.getSession().getPlayer1Id());
            playerGameMap.remove(handler.getSession().getPlayer2Id());
        }
    }

    public void broadcastMove(String gameId, MakeMoveResponseModel moveResponse) {
        GameHandler game = activeGames.get(gameId);
        if (game != null) {
            int p1 = game.getSession().getPlayer1Id();
            int p2 = game.getSession().getPlayer2Id();

            PlayerHandler h1 = onlinePlayers.get(p1);
            PlayerHandler h2 = onlinePlayers.get(p2);

            if (h1 != null)
                h1.sendRequest(moveResponse);
            if (h2 != null)
                h2.sendRequest(moveResponse);

            if (moveResponse.isGameOver()) {
                saveGameResult(game.getSession(), moveResponse.getWinner());
                removeGame(gameId);
            }
        }
    }

    // Forfeit Handler
    public void handleForfeit(int loserId, EndGameSessionRequestModel endGameSessionRequestModel) {
        GameHandler game = getGameByPlayerId(loserId);
        if (game != null) {
            String gameId = game.getSession().getGameId().toString();
            int p1 = game.getSession().getPlayer1Id();
            int p2 = game.getSession().getPlayer2Id();

            int winnerId = (loserId == p1) ? p2 : p1;
            String winnerSymbol = (loserId == p1) ? "O" : "X"; // P1 is usually X, so P2 wins

            // Update Database and Scores
            saveGameResult(game.getSession(), winnerSymbol);

            // Notify Winner (Opponent)
            // We use EndGameSessionRequestModel or MakeMoveResponseModel?
            // User requested explicit "Show alert that opponent disconnected"
            // MakeMoveResponseModel with isGameOver=true and winnerSymbol might trigger
            // "You Won" in client,
            // but we need specific message "Opponent Disconnected".
            // Let's use EndGameSessionRequestModel since Client listens for it.

            PlayerHandler winnerHandler = onlinePlayers.get(winnerId);
            if (winnerHandler != null) {
                // GameStatus.WIN? Model uses GameStatus enum?
                // RequestModel: EndGameSessionRequestModel(p1, p2, status)
                // Let's assume sending a special status or just relying on "Game End" event
                // and client checks active status.
                // Or send EndGameSessionRequestModel with winnerId.
                winnerHandler.sendRequest(new com.mycompany.model.requestModel.EndGameSessionRequestModel(winnerId,
                        loserId, GameStatus.WIN));
            } else {
                PlayerHandler winnerHandler2 = onlinePlayers.get(endGameSessionRequestModel.getPlayer2Id());
                winnerHandler2.sendRequest(new com.mycompany.model.requestModel.EndGameSessionRequestModel(
                        endGameSessionRequestModel.getPlayer2Id(),
                        loserId, GameStatus.LOSE));
            }

            removeGame(gameId);

            // Loser is likely already gone or in process of leaving.
            // Ensure they are reset to Available if still connected (handled in
            // saveGameResult).
        }
    }

    private void saveGameResult(GameSession session, String winnerSymbol) {
        try {
            Game game = new Game();

            // Set Players (We need Player objects, but we only have IDs/names here.
            // GameDAO logic relies on Player objects to get ID.
            // We can create dummy players with just ID for the DAO to extract ID.
            Player p1 = new Player(session.getPlayer1Id(), session.getPlayer1Name(), "", "", 0, true, true);
            Player p2 = new Player(session.getPlayer2Id(), session.getPlayer2Name(), "", "", 0, true, true);
            game.setPlayer1(p1);
            game.setPlayer2(p2);

            int statusInt = 3; // Default Draw
            if (winnerSymbol != null) {
                if (winnerSymbol.equals("X")) {
                    statusInt = 1; // P1
                } else if (winnerSymbol.equals("O")) {
                    statusInt = 2; // P2
                }
            }

            // To be proper: Update DAO to take (p1, p2, winnerStatus) or similar.
            // But I must match existing DAO pattern taking a Game object.
            // I will update the DAO to accept the status int directly if I can overload it?
            // No, user wants interface consistency.
            // I'll leave as is for now, it saves connection.
            gameDAO.insertGame(game, statusInt);

            // Update Scores and Status
            long p1ScoreUpdate = 0;
            long p2ScoreUpdate = 0;

            if (winnerSymbol != null && !winnerSymbol.isEmpty()) {
                if ("X".equals(winnerSymbol)) {
                    p1ScoreUpdate = 30;
                    p2ScoreUpdate = -30;
                } else if ("O".equals(winnerSymbol)) {
                    p1ScoreUpdate = -30;
                    p2ScoreUpdate = 30;
                }
            }

            // Fetch and Update
            Player p1Full = playerService.getPlayerById(session.getPlayer1Id());
            Player p2Full = playerService.getPlayerById(session.getPlayer2Id());

            if (p1Full != null)
                playerService.updateScore(p1Full.getId(), p1Full.getScore() + p1ScoreUpdate);
            if (p2Full != null)
                playerService.updateScore(p2Full.getId(), p2Full.getScore() + p2ScoreUpdate);

            // Reset Status to Available
            playerService.updatePlayerStatus(session.getPlayer1Id(), true, true);
            playerService.updatePlayerStatus(session.getPlayer2Id(), true, true);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
