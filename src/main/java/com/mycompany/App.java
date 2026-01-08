package com.mycompany;

import com.mycompany.DAO.PlayerDAO;
import com.mycompany.config.DBSingleton;
import com.mycompany.model.app.Player;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Objects;

/**
 * JavaFX App
 */
public class App extends Application {

    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {
        scene = new Scene(loadFXML("ServerScreen"), 900, 600);
        stage.setScene(scene);
        stage.setTitle("XO Game Server Dashboard");
        stage.setMaximized(true); // Start maximized (fullscreen)
        stage.show();
        Image appIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("images/logo.png")));
        stage.getIcons().add(appIcon);
    }

    static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    @Override
    public void stop() throws SQLException {
        DBSingleton.getConnection().close();
    }

    public static void main(String[] args) {
        launch();

    }

}