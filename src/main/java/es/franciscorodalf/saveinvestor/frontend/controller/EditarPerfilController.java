package es.franciscorodalf.saveinvestor.frontend.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Controlador para la edición de perfil.
 * Actualmente solo navega entre pantallas.
 */
public class EditarPerfilController {

    @FXML
    private TextField txtNombre;
    @FXML
    private TextField txtApellidos;
    @FXML
    private TextField txtEmail;

    @FXML
    private void clickVolver(ActionEvent event) {
        cambiarEscena(event, "/es/franciscorodalf/saveinvestor/perfil.fxml");
    }

    @FXML
    private void clickGuardar(ActionEvent event) {
        cambiarEscena(event, "/es/franciscorodalf/saveinvestor/perfil.fxml");
    }

    /**
     * Cambia la escena actual.
     */
    private void cambiarEscena(ActionEvent event, String fxml) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxml));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            System.err.println("Error al cargar vista: " + e.getMessage());
        }
    }
}