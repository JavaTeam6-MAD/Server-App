package com.mycompany.controller;

import com.mycompany.model.app.Player;
import com.mycompany.model.requestModel.LoginRequestModel;
import com.mycompany.model.requestModel.RegisterRequestModel;
import com.mycompany.service.PlayerService;
import com.mycompany.model.requestModel.ChangeNameRequestModel;
import com.mycompany.model.requestModel.ChangePasswordRequestModel;
import com.mycompany.model.requestModel.ChangeAvatarRequestModel;
import com.mycompany.model.requestModel.LogoutRequestModel;
import com.mycompany.model.requestModel.MakeUnavailableRequestModel;

import java.net.Socket;

import java.io.*;
import java.sql.SQLException;

public class PlayerHandler extends Thread {
    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private PlayerService playerService;
    private int currentPlayerId = -1;

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
                Object request = in.readObject();
                handleRequest(request);
            }

        } catch (EOFException e) {
            System.out.println("Client disconnected normally");

        } catch (IOException e) {
            System.out.println("Connection error: " + e.getMessage());

        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();

        } finally {
            if (currentPlayerId != -1) {
                try {
                    playerService.updatePlayerActiveStatus(currentPlayerId, false);
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
                currentPlayerId = player.getId();
                playerService.updatePlayerStatus(currentPlayerId, true, true);
            }
            out.writeObject(player);
            out.flush();
        } else if (req instanceof RegisterRequestModel) {
            Player player = playerService.handleRegistration((RegisterRequestModel) req);
            out.writeObject(player);
            out.flush();
        } else if (req instanceof ChangeNameRequestModel) {
            Player player = playerService.handleUpdateName((ChangeNameRequestModel) req);
            out.writeObject(player);
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
                currentPlayerId = -1; // Reset current player
            }
        } else if (req instanceof MakeUnavailableRequestModel) {
            int id = ((MakeUnavailableRequestModel) req).getPlayerId();
            if (id != -1) {
                // Unavailable: Active (still connected), Not Available (busy)
                playerService.updatePlayerStatus(id, true, false);
            }
        }
    }

    private void closeResources() {
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

}
