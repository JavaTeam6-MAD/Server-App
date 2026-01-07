package com.mycompany;

import com.mycompany.config.Server;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;

import java.io.IOException;

/**
 * Controller for the Server Dashboard Screen
 * Manages server start/stop, displays user information, and statistics charts
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

    @FXML
    private PieChart userStatusChart;

    @FXML
    private BarChart<String, Number> gamesChart;

    @FXML
    private CategoryAxis gamesXAxis;

    @FXML
    private NumberAxis gamesYAxis;

    private boolean serverRunning = false;
    private int activeGamesCount = 0;
    /// server Socket
    private final Server server;


    @FXML
    public void initialize() {
        // Initialize charts first
        initializeCharts();

        // Load sample user data
        loadSampleUsers();
        updateServerStatus();

        // Update all charts with initial data
        updateAllCharts();

        /// set server default running

    }

    public ServerScreenController() {
        server = new Server(12345);

    }

    /**
     * Initialize the charts with default data and styling
     */
    private void initializeCharts() {
        // Initialize Pie Chart for User Status
        userStatusChart.setTitle("");
        userStatusChart.setLegendVisible(true);

        // Initialize Bar Chart for Games
        gamesChart.setTitle("");
        gamesXAxis.setLabel("");
        gamesYAxis.setLabel("Count");
        gamesYAxis.setTickUnit(1);

        // Set initial empty data
        updateUserStatusChart();
        updateGamesChart();
    }

    /**
     * Update the user status pie chart with current online/offline counts
     */
    private void updateUserStatusChart() {
        int onlineCount = onlineUsersList.getChildren().size();
        int offlineCount = offlineUsersList.getChildren().size();

        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
                new PieChart.Data("Online (" + onlineCount + ")", onlineCount),
                new PieChart.Data("Offline (" + offlineCount + ")", offlineCount));

        userStatusChart.setData(pieChartData);
    }

    /**
     * Update the games bar chart with current statistics
     */
    private void updateGamesChart() {
        int totalUsers = onlineUsersList.getChildren().size() + offlineUsersList.getChildren().size();
        int onlineUsers = onlineUsersList.getChildren().size();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.getData().add(new XYChart.Data<>("Total Users", totalUsers));
        series.getData().add(new XYChart.Data<>("Online", onlineUsers));
        series.getData().add(new XYChart.Data<>("Active Games", activeGamesCount));

        ObservableList<XYChart.Series<String, Number>> chartData = FXCollections.observableArrayList();
        chartData.add(series);
        gamesChart.setData(chartData);
    }

    /**
     * Update all charts with current data
     */
    private void updateAllCharts() {
        updateUserStatusChart();
        updateGamesChart();
    }

    @FXML
    private void onToggleServer() {
        serverRunning = !serverRunning;
        updateServerStatus();
        try {
            if (serverRunning) {
                System.out.println("Server started!");
                // Add your server start logic here
                server.startServer();

            } else {
                System.out.println("Server stopped!");
                // Add your server stop logic here
                server.stopServer();
            }
        } catch (IOException e) {
            e.printStackTrace();
            /// TODO Alert
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

        // Set sample active games count
        activeGamesCount = 2;
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

        // Update charts when counts change
        updateAllCharts();
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

    /**
     * Increment the active games count and update the chart
     */
    public void incrementGameCount() {
        activeGamesCount++;
        updateGamesChart();
    }

    /**
     * Decrement the active games count and update the chart
     */
    public void decrementGameCount() {
        if (activeGamesCount > 0) {
            activeGamesCount--;
            updateGamesChart();
        }
    }

    /**
     * Set the active games count directly and update the chart
     *
     * @param count The new games count
     */
    public void setActiveGamesCount(int count) {
        activeGamesCount = Math.max(0, count);
        updateGamesChart();
    }

    /**
     * Get the current active games count
     *
     * @return The current number of active games
     */
    public int getActiveGamesCount() {
        return activeGamesCount;
    }
}
