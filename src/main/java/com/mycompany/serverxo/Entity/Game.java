package com.mycompany.serverxo.Entity;

import java.sql.Date;

public class Game {
    private int id;
    private long score;
    private Player player1;
    private Player player2;
    private int status;
    private Date date;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Game(int id, long score, Player player1, Player player2, int status, Date date) {
        this.id = id;
        this.score = score;
        this.player1 = player1;
        this.player2 = player2;
        this.status = status;
        this.date = date;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getScore() {
        return score;
    }

    public void setScore(long score) {
        this.score = score;
    }

    public Player getPlayer1() {
        return player1;
    }

    public void setPlayer1(Player player1) {
        this.player1 = player1;
    }

    public Player getPlayer2() {
        return player2;
    }

    public void setPlayer2(Player player2) {
        this.player2 = player2;
    }


}
