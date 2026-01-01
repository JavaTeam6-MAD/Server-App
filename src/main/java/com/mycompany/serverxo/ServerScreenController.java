package com.mycompany.serverxo;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;

/**
 * Controller for the Server Dashboard Screen
 * Manages server start/stop and displays user information
 */
public class ServerScreenController {

    @FXML
    private Button btnToggleServer;

    @FXML
    private Label lblServerStatus;

    @FXML
    private Label lblToggleButton;

    @FXML
    private SVGPath toggleIcon;

    @FXML
    private HBox serverStatusBadge;

    @FXML
    private VBox onlineUsersList;

    @FXML
    private VBox offlineUsersList;

    @FXML
    private Label lblOnlineCount;

    @FXML
    private Label lblOfflineCount;

    @FXML
    private Label lblUsersCount;

    private boolean serverRunning = false;

    @FXML
    public void initialize() {
        // Load sample user data
        loadSampleUsers();
        updateServerStatus();
    }

    @FXML
    private void onToggleServer() {
        serverRunning = !serverRunning;
        updateServerStatus();

        if (serverRunning) {
            System.out.println("Server started!");
            // Add your server start logic here
        } else {
            System.out.println("Server stopped!");
            // Add your server stop logic here
        }
    }

    private void updateServerStatus() {
        if (serverRunning) {
            lblServerStatus.setText("RUNNING");
            lblToggleButton.setText("Stop Server");
            serverStatusBadge.getStyleClass().remove("server-status-stopped");
            serverStatusBadge.getStyleClass().add("server-status-running");
            // Update icon to stop icon
            toggleIcon.setContent("M6 4h4v16H6V4zm8 0h4v16h-4V4z");
        } else {
            lblServerStatus.setText("STOPPED");
            lblToggleButton.setText("Start Server");
            serverStatusBadge.getStyleClass().remove("server-status-running");
            serverStatusBadge.getStyleClass().add("server-status-stopped");
            // Update icon to play icon
            toggleIcon.setContent("M8 5v14l11-7z");
        }
    }

    private void loadSampleUsers() {
        // Sample online users
        addUserToList(onlineUsersList, "Alice", 1250, true, true);
        addUserToList(onlineUsersList, "Bob", 980, true, false);
        addUserToList(onlineUsersList, "Charlie", 1420, true, false);

        // Sample offline users
        addUserToList(offlineUsersList, "David", 760, false, false);
        addUserToList(offlineUsersList, "Emma", 1100, false, false);

        // Update counts
        updateUserCounts();
    }

    private void addUserToList(VBox container, String username, int score, boolean isOnline, boolean isInGame) {
        // Create player item container
        HBox playerItem = new HBox(12);
        playerItem.getStyleClass().add("player-item");
        playerItem.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        // Avatar
        StackPane avatar = new StackPane();
        avatar.getStyleClass().add("player-avatar");
        Label avatarText = new Label(username.substring(0, 1).toUpperCase());
        avatarText.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");
        avatar.getChildren().add(avatarText);

        // User info VBox
        VBox userInfo = new VBox(4);
        HBox.setHgrow(userInfo, javafx.scene.layout.Priority.ALWAYS);

        // Username
        Label nameLabel = new Label(username);
        nameLabel.getStyleClass().add("player-name");

        // Score
        Label scoreLabel = new Label("Score: " + score);
        scoreLabel.getStyleClass().add("player-score");

        userInfo.getChildren().addAll(nameLabel, scoreLabel);

        // Status badge
        HBox statusBadge = new HBox();
        statusBadge.getStyleClass().add("status-badge");

        Label statusText = new Label();
        statusText.getStyleClass().add("status-badge-text");

        if (isOnline) {
            if (isInGame) {
                statusBadge.getStyleClass().add("status-badge-ingame");
                statusText.setText("IN GAME");
            } else {
                statusBadge.getStyleClass().add("status-badge-available");
                statusText.setText("AVAILABLE");
            }
        } else {
            statusBadge.getStyleClass().addAll("status-badge");
            statusBadge.setStyle("-fx-background-color: rgba(107, 114, 128, 0.2);");
            statusText.setText("OFFLINE");
            statusText.setStyle("-fx-text-fill: #6b7280;");
        }

        statusBadge.getChildren().add(statusText);

        // Add all components to player item
        playerItem.getChildren().addAll(avatar, userInfo, statusBadge);

        // Add to container
        container.getChildren().add(playerItem);
    }

    private void updateUserCounts() {
        int onlineCount = onlineUsersList.getChildren().size();
        int offlineCount = offlineUsersList.getChildren().size();
        int totalCount = onlineCount + offlineCount;

        lblOnlineCount.setText("Online (" + onlineCount + ")");
        lblOfflineCount.setText("Offline (" + offlineCount + ")");
        lblUsersCount.setText("Users (" + totalCount + ")");
    }

    /**
     * Add a new user to the appropriate list
     * 
     * @param username The username
     * @param score    The user's score
     * @param isOnline Whether the user is online
     * @param isInGame Whether the user is currently in a game
     */
    public void addUser(String username, int score, boolean isOnline, boolean isInGame) {
        VBox targetList = isOnline ? onlineUsersList : offlineUsersList;
        addUserToList(targetList, username, score, isOnline, isInGame);
        updateUserCounts();
    }

    /**
     * Remove a user from the lists
     * 
     * @param username The username to remove
     */
    public void removeUser(String username) {
        removeUserFromList(onlineUsersList, username);
        removeUserFromList(offlineUsersList, username);
        updateUserCounts();
    }

    private void removeUserFromList(VBox container, String username) {
        container.getChildren().removeIf(node -> {
            if (node instanceof HBox) {
                HBox playerItem = (HBox) node;
                if (playerItem.getChildren().size() >= 2 && playerItem.getChildren().get(1) instanceof VBox) {
                    VBox userInfo = (VBox) playerItem.getChildren().get(1);
                    if (!userInfo.getChildren().isEmpty() && userInfo.getChildren().get(0) instanceof Label) {
                        Label nameLabel = (Label) userInfo.getChildren().get(0);
                        return nameLabel.getText().equals(username);
                    }
                }
            }
            return false;
        });
    }

    /**
     * Clear all users from the lists
     */
    public void clearUsers() {
        onlineUsersList.getChildren().clear();
        offlineUsersList.getChildren().clear();
        updateUserCounts();
    }
}
