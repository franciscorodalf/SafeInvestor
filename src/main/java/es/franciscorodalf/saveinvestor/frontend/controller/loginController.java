package es.franciscorodalf.saveinvestor.frontend.controller;

import es.franciscorodalf.saveinvestor.backend.dao.UsuarioDAO;
import es.franciscorodalf.saveinvestor.backend.model.Usuario;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
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
            Usuario usuario = usuarioDAO.autenticar(
                textFieldUsuario.getText(),
                textFieldContrasenia.getText()
            );

            if (usuario != null) {
                if (event != null) {
                    cargarPantallaPrincipal(event, usuario);
                }
            } else {
                textFieldMensaje.setText("Credenciales inválidas");
                textFieldMensaje.setVisible(true);
            }
        } catch (Exception e) {
            mostrarError("Error al iniciar sesión: " + e.getMessage());
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
            mostrarError("Error al cargar pantalla de registro");
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
            mostrarError("Error al cargar pantalla de recuperación");
        }
    }

    public void cargarPantallaPrincipal(ActionEvent event, Usuario usuario) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/es/franciscorodalf/saveinvestor/principal.fxml"));
        Scene scene = new Scene(loader.load());
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.show();
    }

    private void mostrarError(String mensaje) {
        textFieldMensaje.setText(mensaje);
        textFieldMensaje.setVisible(true);
    }
}
