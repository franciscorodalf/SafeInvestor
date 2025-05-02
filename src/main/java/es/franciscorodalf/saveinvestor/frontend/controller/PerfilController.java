package es.franciscorodalf.saveinvestor.frontend.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Controlador para la vista de perfil de usuario.
 * Actualmente solo permite navegar entre pantallas.
 */
public class PerfilController {

    @FXML
    private Label lblNombre;
    @FXML
    private Label lblApellidos;
    @FXML
    private Label lblEmail;
    @FXML
    private Label lblFechaRegistro;

    @FXML
    private void clickVolver(ActionEvent event) {
        cambiarEscena(event, "/es/franciscorodalf/saveinvestor/main.fxml");
    }

    @FXML
    private void clickEditar(ActionEvent event) {
        cambiarEscena(event, "/es/franciscorodalf/saveinvestor/editarPerfil.fxml");
    }

    /**
     * Método genérico para cambiar la escena actual.
     */
    private void cambiarEscena(ActionEvent event, String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            System.err.println("Error al cargar la vista: " + e.getMessage());
        }
    }
}
