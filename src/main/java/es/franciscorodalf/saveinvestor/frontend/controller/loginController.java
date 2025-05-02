package es.franciscorodalf.saveinvestor.frontend.controller;

import es.franciscorodalf.saveinvestor.backend.dao.UsuarioDAO;
import es.franciscorodalf.saveinvestor.backend.model.Usuario;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import java.io.IOException;

public class loginController {
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

    public void initialize() {
        usuarioDAO = new UsuarioDAO();
        textFieldMensaje.setVisible(false);
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
            // Errores críticos o no controlados mostrados en terminal
            System.err.println("ERROR al iniciar sesión: " + e.getMessage());
            e.printStackTrace();
            
            // También mostrar mensaje genérico al usuario
            mostrarMensajeError("Error al iniciar sesión. Inténtelo de nuevo");
        }
    }

    @FXML
    private void clickButtonRegistrar(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/es/franciscorodalf/saveinvestor/registro.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            System.err.println("ERROR: Error al cargar pantalla de registro: " + e.getMessage());
            e.printStackTrace();
            mostrarMensajeError("No se pudo abrir la pantalla de registro");
        }
    }

    @FXML
    private void clickLinkOlvidarContrasenia(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/es/franciscorodalf/saveinvestor/olvidarContrasenia.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            System.err.println("ERROR: Error al cargar pantalla de recuperación: " + e.getMessage());
            e.printStackTrace();
            mostrarMensajeError("No se pudo abrir la pantalla de recuperación");
        }
    }

    /**
     * Muestra un mensaje de error en la interfaz
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

    /**
     * Oculta el mensaje de error
     */
    private void ocultarMensajeError() {
        if (textFieldMensaje != null) {
            textFieldMensaje.setVisible(false);
        }
    }

    public void cargarPantallaPrincipal(ActionEvent event, Usuario usuario) throws IOException {
        try {
            // Obtener la URL correctamente usando getClass().getResource()
            java.net.URL url = getClass().getResource("/es/franciscorodalf/saveinvestor/main.fxml");
            if (url == null) {
                throw new IOException("No se pudo encontrar el archivo main.fxml");
            }
            
            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();

            // Pasar el usuario al controlador principal
            MainController controller = loader.getController();
            controller.setUsuario(usuario);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            System.err.println("ERROR al cargar pantalla principal: " + e.getMessage());
            e.printStackTrace();
            mostrarMensajeError("Error al iniciar la aplicación: " + e.getMessage());
            throw e; // Re-lanzamos para conservar el comportamiento original
        }
    }
}
