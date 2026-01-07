package com.mycompany.service;

import com.mycompany.DAO.PlayerDAO;

import com.mycompany.model.app.Player;
import com.mycompany.model.requestModel.LoginRequestModel;
import com.mycompany.model.requestModel.RegisterRequestModel;
import com.mycompany.model.requestModel.ChangeNameRequestModel;
import com.mycompany.model.requestModel.ChangePasswordRequestModel;
import com.mycompany.model.requestModel.ChangeAvatarRequestModel;

import java.sql.SQLException;

public class PlayerService {
    private PlayerDAO playerDAO;

    public PlayerService() {
        playerDAO = new PlayerDAO();
    }

    public Player handleLogin(LoginRequestModel req) throws SQLException {
        return playerDAO.getPlayerByUsernameAndPassword(req.getName(), req.getPassword());

    }

    public Player handleRegistration(RegisterRequestModel req) throws SQLException {
        return playerDAO.insertPlayer(new Player(req.getName(), req.getPassword(), "dragon", 0, false, false));

    }

    public Player handleUpdateName(ChangeNameRequestModel req) throws SQLException {
        playerDAO.updateUserName(req.getId(), req.getUserName());
        return playerDAO.getPlayerById(req.getId());
    }

    public Player handleUpdatePassword(ChangePasswordRequestModel req) throws SQLException {
        playerDAO.updatePassword(req.getId(), req.getPassword());
        return playerDAO.getPlayerById(req.getId());
    }

    public Player handleUpdateAvatar(ChangeAvatarRequestModel req) throws SQLException {
        playerDAO.updateAvatar(req.getId(), req.getAvatarName());
        return playerDAO.getPlayerById(req.getId());
    }
}
