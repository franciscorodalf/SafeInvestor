package es.franciscorodalf.saveinvestor.frontend.controller;

import es.franciscorodalf.saveinvestor.backend.dao.UsuarioDAO;
import es.franciscorodalf.saveinvestor.backend.model.Usuario;
import es.franciscorodalf.saveinvestor.frontend.util.ValidationUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;

public class cambiarContraseniaController {
    
    @FXML
    private PasswordField contraseniaActualField;
    @FXML
    private PasswordField nuevaContraseniaField;
    @FXML
    private PasswordField repetirContraseniaField;
    @FXML
    private Label mensajeLabel;
    @FXML
    private Button botonCambiar;
    @FXML
    private Button botonVolver;

    UsuarioDAO usuarioDAO;
    private Usuario usuarioActual;

    public void initialize() {
        usuarioDAO = new UsuarioDAO();
        mensajeLabel.setVisible(false);
    }

    public void setUsuario(Usuario usuario) {
        this.usuarioActual = usuario;
    }

    @FXML
    public void clickCambiarContrasenia(ActionEvent event) {
        if (!validarCampos()) {
            return;
        }

        try {
            usuarioActual.setContrasenia(nuevaContraseniaField.getText());
            usuarioDAO.actualizar(usuarioActual);
            mostrarMensaje("Contraseña actualizada correctamente");
            volverALogin(event);
        } catch (SQLException | IOException e) {
            mostrarMensaje("Error al cambiar la contraseña: " + e.getMessage());
        }
    }

    @FXML
    public void clickVolver(ActionEvent event) {
        try {
            volverALogin(event);
        } catch (IOException e) {
            mostrarMensaje("Error al volver a la pantalla anterior");
        }
    }

    private boolean validarCampos() {
        if (ValidationUtils.isEmptyField(contraseniaActualField)) {
            mostrarMensaje("Ingrese su contraseña actual");
            return false;
        }

        if (ValidationUtils.isEmptyField(nuevaContraseniaField)) {
            mostrarMensaje("Ingrese la nueva contraseña");
            return false;
        }

        if (!ValidationUtils.doPasswordsMatch(nuevaContraseniaField, repetirContraseniaField)) {
            mostrarMensaje("Las contraseñas no coinciden");
            return false;
        }

        return true;
    }
    public void volverALogin(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/es/franciscorodalf/saveinvestor/login.fxml"));
        Scene scene = new Scene(loader.load());
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.show();
    }

    private void mostrarMensaje(String mensaje) {
        mensajeLabel.setText(mensaje);
        mensajeLabel.setVisible(true);
    }
}
