package es.franciscorodalf.saveinvestor.frontend.controller;

import es.franciscorodalf.saveinvestor.backend.dao.UsuarioDAO;
import es.franciscorodalf.saveinvestor.backend.model.Usuario;
import es.franciscorodalf.saveinvestor.backend.service.ServiceFactory;
import es.franciscorodalf.saveinvestor.util.AppConstants;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class RegistroController {

    private static final Logger logger = LoggerFactory.getLogger(RegistroController.class);

    @FXML
    private TextField textFieldUsuarioRegistro;
    @FXML
    private TextField textFieldEmailRegistro;
    @FXML
    private TextField textFieldRepetirEmail;
    @FXML
    private PasswordField textFieldContraseniaRegistro;
    @FXML
    private PasswordField textFieldRepetirContraseniaRegistro;
    @FXML
    private Label textMensajeRegistro;

    private UsuarioDAO usuarioDAO;

    public void initialize() {
        usuarioDAO = ServiceFactory.getInstance().getUsuarioDAO();
        textMensajeRegistro.setVisible(false);
    }

    // Setters para testing
    public void setUsuarioDAO(UsuarioDAO usuarioDAO) {
        this.usuarioDAO = usuarioDAO;
    }

    public void setTextFieldUsuarioRegistro(TextField textFieldUsuarioRegistro) {
        this.textFieldUsuarioRegistro = textFieldUsuarioRegistro;
    }

    public void setTextFieldEmailRegistro(TextField textFieldEmailRegistro) {
        this.textFieldEmailRegistro = textFieldEmailRegistro;
    }

    public void setTextFieldRepetirEmail(TextField textFieldRepetirEmail) {
        this.textFieldRepetirEmail = textFieldRepetirEmail;
    }

    public void setTextFieldContraseniaRegistro(PasswordField textFieldContraseniaRegistro) {
        this.textFieldContraseniaRegistro = textFieldContraseniaRegistro;
    }

    public void setTextFieldRepetirContraseniaRegistro(PasswordField textFieldRepetirContraseniaRegistro) {
        this.textFieldRepetirContraseniaRegistro = textFieldRepetirContraseniaRegistro;
    }

    public void setTextMensajeRegistro(Label textMensajeRegistro) {
        this.textMensajeRegistro = textMensajeRegistro;
    }

    @FXML
    public void clickRegistrarse(ActionEvent event) {
        try {
            if (!validarCampos()) {
                return;
            }

            Usuario nuevoUsuario = new Usuario(
                    textFieldEmailRegistro.getText(),
                    textFieldUsuarioRegistro.getText(),
                    textFieldContraseniaRegistro.getText());

            usuarioDAO.insertar(nuevoUsuario);
            mostrarMensaje("Usuario registrado correctamente", false);

            if (event != null) {
                // Pequeña pausa o transición podría ir aquí, pero por ahora volvemos directo
                volverALogin(event);
            }
        } catch (Exception e) {
            logger.error("Error al registrar usuario", e);
            mostrarMensaje("Error al registrar usuario: " + e.getMessage(), true);
        }
    }

    @FXML
    private void clickButtonVolver(ActionEvent event) {
        try {
            volverALogin(event);
        } catch (IOException e) {
            logger.error("Error al volver a login", e);
            mostrarMensaje("Error al volver a la pantalla de login", true);
        }
    }

    private boolean validarCampos() {
        if (!textFieldEmailRegistro.getText().equals(textFieldRepetirEmail.getText())) {
            mostrarMensaje("Los emails no coinciden", true);
            return false;
        }

        if (!textFieldContraseniaRegistro.getText().equals(textFieldRepetirContraseniaRegistro.getText())) {
            mostrarMensaje("Las contraseñas no coinciden", true);
            return false;
        }

        if (textFieldUsuarioRegistro.getText().isEmpty() || textFieldEmailRegistro.getText().isEmpty()
                || textFieldContraseniaRegistro.getText().isEmpty()) {
            mostrarMensaje("Todos los campos son obligatorios", true);
            return false;
        }

        return true;
    }

    private void volverALogin(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(AppConstants.FXML_LOGIN));
        Scene scene = new Scene(loader.load());
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.show();
    }

    private void mostrarMensaje(String mensaje, boolean error) {
        textMensajeRegistro.setText(mensaje);
        textMensajeRegistro.setVisible(true);
        if (error) {
            textMensajeRegistro.setStyle("-fx-text-fill: -fx-error-color;");
        } else {
            textMensajeRegistro.setStyle("-fx-text-fill: -fx-secondary-color;");
        }
    }
}
