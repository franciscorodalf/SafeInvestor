package es.franciscorodalf.saveinvestor.frontend.controller;

import es.franciscorodalf.saveinvestor.backend.dao.UsuarioDAO;
import es.franciscorodalf.saveinvestor.frontend.util.ValidationUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.io.IOException;

public class RecuperarContraseniaController {
    @FXML
    private TextField emailField;
    @FXML
    private Label mensajeLabel;
    @FXML
    private Button buttonVolverRecuperarContrasenia;
    
    public void initialize() {
        new UsuarioDAO();
        mensajeLabel.setVisible(false);
    }

    @FXML
    private void clickButtonEnviar(ActionEvent event) {
        if (ValidationUtils.isEmptyField(emailField) || 
            !ValidationUtils.isValidEmail(emailField.getText())) {
            mostrarMensaje("Por favor, ingrese un email válido");
            return;
        }
        // Aquí iría la lógica de envío de email
        mostrarMensaje("Se ha enviado un correo con las instrucciones");
    }

    @FXML
    private void clickVolverRecuperarContrasenia(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/es/franciscorodalf/saveinvestor/login.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            mostrarMensaje("Error al volver a la pantalla de login");
        }
    }

    private void mostrarMensaje(String mensaje) {
        mensajeLabel.setText(mensaje);
        mensajeLabel.setVisible(true);
    }
}
