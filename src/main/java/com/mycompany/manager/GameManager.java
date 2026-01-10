package com.mycompany.manager;

import com.mycompany.DAO.GameDAO;
import com.mycompany.controller.PlayerHandler;
import com.mycompany.model.app.Game;
import com.mycompany.model.app.Player;
import com.mycompany.model.requestModel.ReceiveChallengeRequestModel;
import com.mycompany.model.responseModel.ReceiveChallengeResponseModel;
import com.mycompany.model.responseModel.MakeMoveResponseModel;
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

    private GameManager() {
        onlinePlayers = new ConcurrentHashMap<>();
        activeGames = new ConcurrentHashMap<>();
        playerGameMap = new ConcurrentHashMap<>();
        gameDAO = new GameDAO();
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

    // === Challenge Logic ===
    public void sendChallenge(int challengerId, String challengerName, int opponentId) {
        PlayerHandler opponent = onlinePlayers.get(opponentId);
        if (opponent != null) {
            // Updated model usage
            opponent.sendRequest(new ReceiveChallengeRequestModel(challengerId, opponentId, challengerName));
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

            // Notify both - using ReceiveChallengeResponseModel as Start Game signal
            // Sender in Model = Challenger (Player 1)
            // Receiver in Model = Opponent (Player 2)
            String challengerName = challenger.getPlayerName(); // Safe access, checked above

            ReceiveChallengeResponseModel responseToChallenger = new ReceiveChallengeResponseModel(
                    opponentId, challengerId, true, false, gameId, challengerName, opponentName);
            ReceiveChallengeResponseModel responseToOpponent = new ReceiveChallengeResponseModel(
                    challengerId, opponentId, true, false, gameId, challengerName, opponentName);

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
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
