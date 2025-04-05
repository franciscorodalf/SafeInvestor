package es.franciscorodalf.saveinvestor.frontend.controller;

import java.io.IOException;

import es.franciscorodalf.saveinvestor.backend.controller.AbstractController;
import es.franciscorodalf.saveinvestor.backend.model.UsuarioEntity;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class RecuperarContraseniaController extends AbstractController {

    @FXML
    Button buttonVolverRecuperarContrasenia;

    @FXML
    TextField emailField;

    @FXML
    Button enviarButton;

    @FXML
    Label mensajeLabel;

    @FXML
    private void clickButtonEnviar() {

        String email = emailField.getText();

        if (email.isBlank() || email.isEmpty()) {
            mostrarMensaje("No se ha escrito un correo valido", false);
        }

        String emailRegex = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$";
        if (!email.matches(emailRegex)) {
            mostrarMensaje("El formato del correo es inválido", false);
            return;
        }
        UsuarioEntity usuarioEntity = getUsuarioServiceModel().obtenerUsuarioPorEmail(email);
        if (usuarioEntity == null) {
            mostrarMensaje("No existe ninguna cuenta con ese correo", false);
            return;
        }

        mostrarMensaje("Correo válido. Trasladando a la siguiente pagina...", true);
    }

    private void mostrarMensaje(String mensaje, boolean esExito) {
        mensajeLabel.getStyleClass().removeAll("text-error", "text-success");
        mensajeLabel.setText(mensaje);
        mensajeLabel.setVisible(true);

        if (esExito) {
            mensajeLabel.getStyleClass().add("text-success");
        } else {
            mensajeLabel.getStyleClass().add("text-error");
        }
    }

    @FXML
    private void clickVolverRecuperarContrasenia(ActionEvent event) {
        try {
            System.out.println("Volviendo a la pantalla de login...");

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
