package com.mycompany.config;
import com.mycompany.controller.PlayerHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server implements Runnable {

    private ServerSocket serverSocket;
    private boolean running;//false
    private final int port;

    public Server(int port) {
        this.port = port;
    }

    public void startServer() throws IOException {
        if (running) return;

        serverSocket = new ServerSocket(port);
        running = true;

        Thread serverThread = new Thread(this);
        serverThread.setDaemon(true);
        serverThread.start();

        System.out.println("Server started on port " + port);
    }

    public void stopServer() throws IOException {
        running = false;

        if (serverSocket != null && !serverSocket.isClosed()) {
            serverSocket.close();
        }

        System.out.println("Server stopped");
    }

    @Override
    public void run() {
        try {
            while (running) {
                Socket socket = serverSocket.accept();
                System.out.println("New client connected");

                PlayerHandler handler = new PlayerHandler(socket);
                handler.start();
            }
        } catch (IOException e) {
            if (running) {
                e.printStackTrace();
            }
        }
    }
}