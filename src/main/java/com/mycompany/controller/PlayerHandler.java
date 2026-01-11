package com.mycompany.controller;

import com.mycompany.DAO.PlayerDAO;
import com.mycompany.model.app.Player;
import com.mycompany.model.requestModel.LoginRequestModel;
import com.mycompany.model.requestModel.*;
import com.mycompany.service.PlayerService;
import com.mycompany.model.requestModel.ChangeNameRequestModel;
import com.mycompany.model.requestModel.ChangePasswordRequestModel;
import com.mycompany.model.requestModel.ChangeAvatarRequestModel;
import com.mycompany.model.requestModel.LogoutRequestModel;
import com.mycompany.model.requestModel.MakeUnavailableRequestModel;
import com.mycompany.manager.GameManager;
import com.mycompany.manager.GameHandler;
import com.mycompany.model.responseModel.*;
import java.net.Socket;

import java.io.*;
import java.sql.SQLException;
import java.util.List;

public class PlayerHandler extends Thread {
    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private PlayerService playerService;
    private Player currentPlayer; // Changed from int to object to hold full state

    public PlayerHandler(Socket socket) {
        this.socket = socket;
        playerService = new PlayerService();
    }

    /// handel each player and sign it in the server
    @Override
    public void run() {
        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(socket.getInputStream());

            while (true) {
                try {
                    Object request = in.readObject();
                    handleRequest(request);
                } catch (ClassNotFoundException | SQLException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    // Logic error in handling request?
                    System.err.println("Error processing request: " + e.getMessage());
                    e.printStackTrace();
                    // If e is IOException/EOF, we must break.
                    if (e instanceof IOException) {
                        break;
                    }
                }
            }

        } catch (EOFException e) {
            System.out.println("Client disconnected normally");
        } catch (IOException e) {
            System.out.println("Connection error: " + e.getMessage());

        }  catch (Exception e) {
            System.err.println("PlayerHandler Error: " + e.getMessage());
            e.printStackTrace();
            // Do NOT break/disconnect for logic errors strictly, but loop continues if
            // possible
            // However, if streaming is broken, we can't continue.
            // If readObject failed, we break. If handleRequest failed, we continue.
            // We need to move try-catch block INSIDE the while loop for robustness against
            // handleRequest errors.
        } finally {
            if (currentPlayer != null && currentPlayer.getId() != -1) {
                try {
                    playerService.updatePlayerActiveStatus(currentPlayer.getId(), false);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            closeResources();
        }
    }

    private synchronized void handleRequest(Object req) throws SQLException, IOException {

        if (req instanceof LoginRequestModel) {
            Player player = playerService.handleLogin((LoginRequestModel) req);
            if (player != null) {
                currentPlayer = player;
                playerService.updatePlayerStatus(currentPlayer.getId(), true, true);
                GameManager.getInstance().addPlayer(currentPlayer.getId(), this);// add player in a online list

                // Update object sent to client to reflect new status
                player.setIsActive(true);
                player.setIsAvailable(true);
            }
            out.writeObject(player);
            out.flush();
        } else if (req instanceof RegisterRequestModel) {
            Player player = playerService.handleRegistration((RegisterRequestModel) req);
            if (player != null) {
                currentPlayer = player;
                // Auto-login: Set status to Active/Available
                playerService.updatePlayerStatus(currentPlayer.getId(), true, true);
                GameManager.getInstance().addPlayer(currentPlayer.getId(), this);

                player.setIsActive(true);
                player.setIsAvailable(true);
            }
            out.writeObject(player);
            out.flush();
        } else if (req instanceof ChangeNameRequestModel) {
            Player player = playerService.handleUpdateName((ChangeNameRequestModel) req);
            out.writeObject(player);
            out.flush();
        } else if (req instanceof getFriendsRequestModel) {
            PlayerDAO dao = new PlayerDAO();
            List<Player> p = dao.getAllPlayers();
            out.writeObject(p);
            out.flush();
        } else if (req instanceof ChangePasswordRequestModel) {
            Player player = playerService.handleUpdatePassword((ChangePasswordRequestModel) req);
            out.writeObject(player);
            out.flush();
        } else if (req instanceof ChangeAvatarRequestModel) {
            Player player = playerService.handleUpdateAvatar((ChangeAvatarRequestModel) req);
            out.writeObject(player);
            out.flush();
        } else if (req instanceof LogoutRequestModel) {
            int id = ((LogoutRequestModel) req).getPlayerId();
            if (id != -1) {
                // Logout: Not Active, Not Available
                playerService.updatePlayerStatus(id, false, false);
                GameManager.getInstance().removePlayer(id);
                currentPlayer = null;
            }
        } else if (req instanceof MakeUnavailableRequestModel) {
            int id = ((MakeUnavailableRequestModel) req).getPlayerId();
            if (id != -1) {
                // Unavailable: Active (still connected), Not Available (busy)
                playerService.updatePlayerStatus(id, false, false);
            }
        } else if (req instanceof SendChallengeRequestModel) {
            SendChallengeRequestModel model = (SendChallengeRequestModel) req;
            GameManager.getInstance().sendChallenge(currentPlayer.getId(), currentPlayer.getUserName(),
                    model.getReceiverPlayer2Id());
        } else if (req instanceof SendChallengeResponseModel) {
            SendChallengeResponseModel model = (SendChallengeResponseModel) req;
            // The challengerId in the model is who challenged US.
            GameManager.getInstance().handleChallengeResponse(currentPlayer.getId(), currentPlayer.getUserName(),
                    model.getChallengerId(), model.isAccepted());
        } else if (req instanceof MakeMoveRequestModel) {
            MakeMoveRequestModel model = (MakeMoveRequestModel) req;
            // Use gameId from model. Logic in Handler should support finding game by ID or
            // Player.
            // Using gameId is safer.
            GameHandler game = GameManager.getInstance().getGame(model.getGameId());
            if (game != null) {
                Object moveResponse = game.processMove(currentPlayer.getId(), model.getRow(), model.getCol());
                if (moveResponse != null) {
                    GameManager.getInstance().broadcastMove(model.getGameId(), (MakeMoveResponseModel) moveResponse);
                }
            }
        } else if (req instanceof EndGameSessionRequestModel) {
            EndGameSessionRequestModel model = (EndGameSessionRequestModel) req;
            // Can be used for explicit Forfeit
            // We assume sender is the one forfeiting if status implies it, or just
            // generally Ending the session
            // For now, mapping EndGame request to Forfeit logic
            GameManager.getInstance().handleForfeit(currentPlayer.getId(),model);
        }
    }

    private synchronized void closeResources() {
        try {
            if (in != null)
                in.close();
            if (out != null)
                out.close();
            if (socket != null && !socket.isClosed())
                socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void sendRequest(Object request) {
        try {
            out.writeObject(request);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized String getPlayerName() {
        return (currentPlayer != null) ? currentPlayer.getUserName() : null;
    }

    public int getPlayerId() {
        return (currentPlayer != null) ? currentPlayer.getId() : -1;
    }

}
