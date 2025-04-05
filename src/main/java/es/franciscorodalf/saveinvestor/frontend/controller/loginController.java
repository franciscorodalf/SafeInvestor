package es.franciscorodalf.saveinvestor.frontend.controller;

import java.io.IOException;

import es.franciscorodalf.saveinvestor.backend.model.UsuarioEntity;
import es.franciscorodalf.saveinvestor.backend.controller.AbstractController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class loginController extends AbstractController {

    @FXML
    private VBox rootVBox;

    @FXML
    private ImageView imageLogoLogin;
    @FXML
    private Button buttonAceptarlLogin;
    @FXML
    private Button buttonLoginRegistrar;

    @FXML
    private Label textBienvenidoLogin;
    @FXML
    private TextField textFieldUsuario;
    @FXML
    private TextField textFieldContrasenia;
    @FXML
    private PasswordField textLabelContrasenia;
    @FXML
    private Hyperlink textLinkOlvidarContrasenia;
    @FXML
    private Text textFieldMensaje;

    @FXML
    public void initialize() {
 
    }

    @FXML
    protected void buttonAceptarLogin() {
        textFieldMensaje.setVisible(true);

        if (textFieldUsuario == null || textFieldUsuario.getText().isEmpty() ||
                textFieldContrasenia == null || textFieldContrasenia.getText().isEmpty()) {
            textFieldMensaje.setText("Credenciales null o vacías");
            textFieldMensaje.setStyle("-fx-fill: red;");
            return;
        }

        UsuarioEntity usuarioEntity = getUsuarioServiceModel().obtenerDatosUsuario(textFieldUsuario.getText());

        if (usuarioEntity == null) {
            textFieldMensaje.setText("El usuario no existe");
            textFieldMensaje.setStyle("-fx-fill: red;");
            return;
        }

        if ((textFieldUsuario.getText().equals(usuarioEntity.getEmail())
                || textFieldUsuario.getText().equals(usuarioEntity.getNombre()))
                && textFieldContrasenia.getText().equals(usuarioEntity.getContrasenia())) {
            textFieldMensaje.setText("Usuario validado correctamente");
            textFieldMensaje.setStyle("-fx-fill: green;");

            return;
        }

        textFieldMensaje.setText("Credenciales inválidas");
        textFieldMensaje.setStyle("-fx-fill: red;");
    }

    @FXML
    private void clickButtonRegistrar(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(
                    getClass().getResource("/es/franciscorodalf/saveinvestor/registro.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            RegistroController registroController = fxmlLoader.getController();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Registro");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void clickLinkOlvidarContrasenia(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(
                    getClass().getResource("/es/franciscorodalf/saveinvestor/olvidarContrasenia.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            RecuperarContraseniaController recuperarContraseniaController = fxmlLoader.getController();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Recuperar Contraseña");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
