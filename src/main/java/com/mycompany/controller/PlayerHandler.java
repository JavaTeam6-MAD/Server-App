package com.mycompany.controller;

import com.mycompany.model.app.Player;
import com.mycompany.model.requestModel.LoginRequestModel;
import com.mycompany.model.requestModel.RegisterRequestModel;
import com.mycompany.service.PlayerService;
import com.mycompany.service.GameService;
import com.mycompany.model.requestModel.ChangeNameRequestModel;
import com.mycompany.model.requestModel.ChangePasswordRequestModel;
import com.mycompany.model.requestModel.ChangeAvatarRequestModel;
import com.mycompany.model.requestModel.getGameHistoryRequestModel;
import com.mycompany.model.app.RecordedGame;

import java.net.Socket;

import java.io.*;
import java.sql.SQLException;
import java.util.List;

public class PlayerHandler extends Thread {
    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private PlayerService playerService;
    private GameService gameService;

    public PlayerHandler(Socket socket) {
        this.socket = socket;
        playerService = new PlayerService();
        gameService = new GameService();
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
            closeResources();
        }
    }

    private synchronized void handleRequest(Object req) throws SQLException, IOException {

        if (req instanceof LoginRequestModel) {
            Player player = playerService.handleLogin((LoginRequestModel) req);
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
        } else if (req instanceof getGameHistoryRequestModel) {
            List<RecordedGame> gameHistory = gameService.handleGameHistoryRequest((getGameHistoryRequestModel) req);
            out.writeObject(gameHistory);
            out.flush();
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
