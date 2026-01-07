package com.mycompany.service;

import com.mycompany.DAO.PlayerDAO;

import com.mycompany.model.app.Player;
import com.mycompany.model.requestModel.LoginRequestModel;
import com.mycompany.model.requestModel.RegisterRequestModel;

import java.sql.SQLException;

public class LoginService {
    private PlayerDAO playerDAO;

    public LoginService() {
        playerDAO = new PlayerDAO();
    }

    public Player handleLogin(LoginRequestModel req) throws SQLException {
       return playerDAO.getPlayerByUsernameAndPassword(req.getName(), req.getPassword());

    }
    public Player handleRegistration(RegisterRequestModel req) throws SQLException {
        return playerDAO.insertPlayer(new Player(req.getName(), req.getPassword(), "dragon",0,false,false));

    }
}
