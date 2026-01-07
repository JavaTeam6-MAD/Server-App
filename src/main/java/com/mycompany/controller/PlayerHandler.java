package com.mycompany.controller;

import com.mycompany.model.app.Player;
import com.mycompany.model.requestModel.LoginRequestModel;
import com.mycompany.model.requestModel.RegisterRequestModel;
import com.mycompany.service.LoginService;

import java.net.Socket;


import java.io.*;
import java.sql.SQLException;

public class PlayerHandler extends Thread {
    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private LoginService loginService;

    public PlayerHandler(Socket socket) {
        this.socket = socket;
        loginService = new LoginService();
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

    private void handleRequest(Object req) throws SQLException, IOException {

        if (req instanceof LoginRequestModel) {
            Player player = loginService.handleLogin((LoginRequestModel) req);
            out.writeObject(player);
            out.flush();
        } else if (req instanceof RegisterRequestModel) {
            Player player = loginService.handleRegistration((RegisterRequestModel) req);
            out.writeObject(player);
            out.flush();
        }
    }

    private void closeResources() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
