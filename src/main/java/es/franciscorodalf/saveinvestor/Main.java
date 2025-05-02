package es.franciscorodalf.saveinvestor;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        try {
            java.net.URL loginUrl = getClass().getResource("login.fxml");
            if (loginUrl == null) {
                // Intentar con la ruta completa
                loginUrl = getClass().getResource("/es/franciscorodalf/saveinvestor/login.fxml");
                if (loginUrl == null) {
                    throw new IOException("No se puede encontrar el archivo login.fxml");
                }
            }
            
            FXMLLoader fxmlLoader = new FXMLLoader(loginUrl);
            Scene scene = new Scene(fxmlLoader.load());
            stage.setTitle("SafeInvestor");
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            System.err.println("Error al iniciar la aplicación: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    public static void main(String[] args) {
        launch();
    }
}