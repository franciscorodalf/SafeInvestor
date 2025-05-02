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
import java.time.format.DateTimeFormatter;

import es.franciscorodalf.saveinvestor.backend.model.Usuario;

/**
 * Controlador para la vista de perfil de usuario.
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
    private Label lblContrasenia;

    private Usuario usuarioActual;

    /**
     * Establece el usuario y actualiza la interfaz
     * @param usuario El usuario cuyo perfil se va a mostrar
     */
    public void setUsuario(Usuario usuario) {
        this.usuarioActual = usuario;
        actualizarDatosUsuario();
    }

    /**
     * Actualiza los campos de la interfaz con los datos del usuario
     */
    private void actualizarDatosUsuario() {
        if (usuarioActual == null) return;

        if (lblNombre != null) {
            lblNombre.setText(usuarioActual.getNombre());
        }
        
        if (lblEmail != null) {
            lblEmail.setText(usuarioActual.getEmail());
        }
        
        if (lblContrasenia != null) {
            lblContrasenia.setText(usuarioActual.getContrasenia());
        }
    }

    @FXML
    private void clickVolver(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/es/franciscorodalf/saveinvestor/main.fxml"));
            Parent root = loader.load();
            
            // Pasar usuario al controlador principal
            if (usuarioActual != null) {
                MainController controller = loader.getController();
                controller.setUsuario(usuarioActual);
            }
            
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            System.err.println("Error al cargar la vista: " + e.getMessage());
        }
    }

    @FXML
    private void clickEditar(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/es/franciscorodalf/saveinvestor/editarPerfil.fxml"));
            Parent root = loader.load();

            // Pasar usuario al controlador de edición
            if (usuarioActual != null) {
                EditarPerfilController controller = loader.getController();
                controller.setUsuario(usuarioActual);
            }
            
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            System.err.println("Error al cargar la vista: " + e.getMessage());
        }
    }
}
