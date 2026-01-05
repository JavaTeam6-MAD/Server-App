package com.mycompany.serverxo.Entity;

public class Player {
    private int id;
    private String userName;
    private String password;
    private int character;
    private long score;
    private boolean isActive;
    private boolean isAvailable;

    public Player(int id, String userName, String password, int character, long score, boolean isActive, boolean isAvailable) {
        this.id = id;
        this.userName = userName;
        this.password = password;
        this.character = character;
        this.score = score;
        this.isActive = isActive;
        this.isAvailable = isAvailable;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getCharacter() {
        return character;
    }

    public void setCharacter(int character) {
        this.character = character;
    }

    public long getScore() {
        return score;
    }

    public void setScore(long score) {
        this.score = score;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }
}
