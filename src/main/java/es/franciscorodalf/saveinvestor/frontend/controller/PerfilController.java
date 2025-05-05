package es.franciscorodalf.saveinvestor.frontend.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Optional;

import es.franciscorodalf.saveinvestor.backend.dao.UsuarioDAO;
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

    // Añade una referencia al botón eliminar para poder manejarlo en el código
    @FXML
    private Button btnEliminarCuenta;

    private Usuario usuarioActual;
    private UsuarioDAO usuarioDAO;

    @FXML
    private void initialize() {
        usuarioDAO = new UsuarioDAO();
    }

    /**
     * Establece el usuario y actualiza la interfaz
     * 
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
        if (usuarioActual == null)
            return;

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
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/es/franciscorodalf/saveinvestor/editarPerfil.fxml"));
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

    @FXML
    private void clickEliminarCuenta(ActionEvent event) {
        // Mostrar diálogo de confirmación
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Eliminar cuenta");
        confirmacion.setHeaderText("¿Estás seguro de que quieres eliminar tu cuenta?");
        confirmacion.setContentText("Esta acción no se puede deshacer y perderás todos tus datos.");

        Optional<ButtonType> resultado = confirmacion.showAndWait();

        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            try {
                // Eliminar usuario de la base de datos
                boolean eliminado = usuarioDAO.eliminarCompleto(usuarioActual.getId());

                if (eliminado) {
                    // Mostrar mensaje de éxito
                    Alert exito = new Alert(Alert.AlertType.INFORMATION);
                    exito.setTitle("Cuenta eliminada");
                    exito.setHeaderText(null);
                    exito.setContentText("Tu cuenta ha sido eliminada correctamente.");
                    exito.showAndWait();

                    // Volver a la pantalla de login
                    FXMLLoader loader = new FXMLLoader(
                            getClass().getResource("/es/franciscorodalf/saveinvestor/login.fxml"));
                    Parent root = loader.load();
                    Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                    stage.setScene(new Scene(root));
                    stage.show();
                } else {
                    throw new Exception("No se pudo eliminar la cuenta.");
                }
            } catch (Exception e) {
                Alert error = new Alert(Alert.AlertType.ERROR);
                error.setTitle("Error");
                error.setHeaderText(null);
                error.setContentText("No se pudo eliminar la cuenta: " + e.getMessage());
                error.showAndWait();
            }
        }
    }
}
