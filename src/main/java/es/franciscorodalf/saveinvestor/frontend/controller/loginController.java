package es.franciscorodalf.saveinvestor.frontend.controller;

import es.franciscorodalf.saveinvestor.backend.dao.UsuarioDAO;
import es.franciscorodalf.saveinvestor.backend.model.Usuario;
import es.franciscorodalf.saveinvestor.backend.service.ServiceFactory;
import es.franciscorodalf.saveinvestor.util.AppConstants;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class LoginController {

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @FXML
    private TextField textFieldUsuario;
    @FXML
    private PasswordField textFieldContrasenia;
    @FXML
    private Text textFieldMensaje;
    @FXML
    private Button buttonAceptarlLogin;
    @FXML
    private Button buttonLoginRegistrar;
    @FXML
    private Hyperlink textLinkOlvidarContrasenia;

    private UsuarioDAO usuarioDAO;

    @FXML
    private VBox rootVBox;

    @FXML
    public void initialize() {
        usuarioDAO = ServiceFactory.getInstance().getUsuarioDAO();
        textFieldMensaje.setVisible(false);

        // Preparar animación de entrada
        if (rootVBox != null) {
            rootVBox.setOpacity(0);
            javafx.application.Platform.runLater(this::animateEntry);
        }
    }

    public void animateEntry() {
        if (rootVBox != null) {
            javafx.animation.FadeTransition fadeOut = new javafx.animation.FadeTransition(
                    javafx.util.Duration.millis(1000), rootVBox);
            fadeOut.setFromValue(0.0);
            fadeOut.setToValue(1.0);
            fadeOut.setCycleCount(1);
            fadeOut.play();
        }
    }

    // Setters para testing
    void setUsuarioDAO(UsuarioDAO usuarioDAO) {
        this.usuarioDAO = usuarioDAO;
    }

    void setTextFieldUsuario(TextField textField) {
        this.textFieldUsuario = textField;
    }

    void setTextFieldContrasenia(PasswordField textField) {
        this.textFieldContrasenia = textField;
    }

    void setTextFieldMensaje(Text textFieldMensaje) {
        this.textFieldMensaje = textFieldMensaje;
    }

    @FXML
    public void buttonAceptarLogin(ActionEvent event) {
        try {
            // Obtener el texto del campo (podría ser usuario o email)
            String usuarioOEmail = textFieldUsuario.getText();
            String contrasenia = textFieldContrasenia.getText();

            if (usuarioOEmail.isEmpty() || contrasenia.isEmpty()) {
                mostrarMensajeError("Por favor complete todos los campos");
                return;
            }

            Usuario usuario = usuarioDAO.autenticar(usuarioOEmail, contrasenia);

            if (usuario != null) {
                if (event != null) {
                    cargarPantallaPrincipal(event, usuario);
                }
            } else {
                mostrarMensajeError("Credenciales inválidas. Revise usuario y contraseña");
            }
        } catch (Exception e) {
            logger.error("Error al iniciar sesión", e);
            mostrarMensajeError("Error al iniciar sesión. Inténtelo de nuevo");
        }
    }

    @FXML
    private void clickButtonRegistrar(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(AppConstants.FXML_REGISTRO));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            logger.error("Error al cargar pantalla de registro", e);
            mostrarMensajeError("No se pudo abrir la pantalla de registro");
        }
    }

    @FXML
    private void clickLinkOlvidarContrasenia(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(AppConstants.FXML_OLVIDAR_CONTRASENIA));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            logger.error("Error al cargar pantalla de recuperación", e);
            mostrarMensajeError("No se pudo abrir la pantalla de recuperación");
        }
    }

    /**
     * Muestra un mensaje de error en la interfaz
     * 
     * @param mensaje El mensaje de error a mostrar
     */
    private void mostrarMensajeError(String mensaje) {
        if (textFieldMensaje != null) {
            textFieldMensaje.setText(mensaje);
            textFieldMensaje.setVisible(true);

            // Color rojo para el mensaje de error
            textFieldMensaje.setStyle("-fx-fill: #e74c3c;");
        }
    }

    public void cargarPantallaPrincipal(ActionEvent event, Usuario usuario) throws IOException {
        try {
            java.net.URL url = getClass().getResource(AppConstants.FXML_DASHBOARD);
            if (url == null) {
                throw new IOException("No se pudo encontrar el archivo dashboard.fxml");
            }

            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();

            // Pasar el usuario al controlador principal
            Object controller = loader.getController();
            if (controller instanceof DashboardController) {
                ((DashboardController) controller).setUsuario(usuario);
            }

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            logger.error("Error al cargar pantalla principal", e);
            mostrarMensajeError("Error al iniciar la aplicación: " + e.getMessage());
            throw e; // Re-lanzamos para conservar el comportamiento original
        }
    }
}
