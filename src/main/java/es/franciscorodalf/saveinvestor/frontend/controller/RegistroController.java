package es.franciscorodalf.saveinvestor.frontend.controller;

import java.io.IOException;
import java.sql.SQLException;

import es.franciscorodalf.saveinvestor.backend.controller.AbstractController;
import es.franciscorodalf.saveinvestor.backend.model.UsuarioEntity;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class RegistroController extends AbstractController {

    @FXML
    private Button buttonVolver;

    @FXML
    private Button ButtonRegistrarse;

    @FXML
    private Label textMensajeRegistro;

    @FXML
    private TextField textFieldUsuarioRegistro;

    @FXML
    private PasswordField textFieldContraseniaRegistro;

    @FXML
    private PasswordField textFieldRepetirContraseniaRegistro;

    @FXML
    private TextField textFieldEmailRegistro;

    @FXML
    private TextField textFieldRepetirEmail;

    @FXML
    private void initialize() {
    
    }

    @FXML
    protected void clickRegistrarse() throws SQLException {
        textMensajeRegistro.getStyleClass().removeAll("text-error", "text-success");

        if (textFieldContraseniaRegistro.getText().isEmpty()
                || textFieldRepetirContraseniaRegistro.getText().isEmpty()) {
            mostrarMensaje("La contraseña no puede estar vacía", false);
            return;
        }

        if (!textFieldContraseniaRegistro.getText().equals(textFieldRepetirContraseniaRegistro.getText())) {
            mostrarMensaje("Las contraseñas deben coincidir", false);
            return;
        }

        String email = textFieldEmailRegistro.getText();
        String emailRepetido = textFieldRepetirEmail.getText();

        String emailRegex = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$";
        if (!email.matches(emailRegex)) {
            mostrarMensaje("El formato del correo es inválido", false);
            return;
        }

        if (!email.equals(emailRepetido)) {
            mostrarMensaje("Los correos electrónicos deben coincidir", false);
            return;
        }

        UsuarioEntity usuarioNuevo = new UsuarioEntity(
                email,
                textFieldUsuarioRegistro.getText(),
                textFieldContraseniaRegistro.getText());

        if (!getUsuarioServiceModel().agregarUsuario(usuarioNuevo)) {
            mostrarMensaje("Usuario ya registrado o inválido", false);
            return;
        }

        mostrarMensaje("Registro exitoso. Redirigiendo...", true);
    }

    private void mostrarMensaje(String mensaje, boolean esExito) {
        textMensajeRegistro.getStyleClass().removeAll("text-error", "text-success");
        textMensajeRegistro.setText(mensaje);
        textMensajeRegistro.setVisible(true);

        if (esExito) {
            textMensajeRegistro.getStyleClass().add("text-success");
        } else {
            textMensajeRegistro.getStyleClass().add("text-error");

        }
    }

    @FXML
    private void clickButtonVolver(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/es/franciscorodalf/saveinvestor/login.fxml"));
            Scene scene = new Scene(loader.load());

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Login");
            stage.show();
        } catch (IOException e) {
            System.err.println("❌ Error al volver a la pantalla de login: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
