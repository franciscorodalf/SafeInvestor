package es.franciscorodalf.saveinvestor.frontend.controller;

import java.io.IOException;

import es.franciscorodalf.saveinvestor.backend.model.UsuarioEntity;
import es.franciscorodalf.saveinvestor.backend.controller.AbstractController;
import javafx.animation.*;
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
import javafx.util.Duration;
import javafx.scene.input.MouseEvent;

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
        aplicarAnimacionesEntrada();
        aplicarAnimacionesHover(buttonAceptarlLogin);
        aplicarAnimacionesPresion(buttonAceptarlLogin);
        aplicarAnimacionesPresion(buttonLoginRegistrar);
    }

    @FXML
    protected void buttonAceptarLogin() {
        textFieldMensaje.setVisible(true);

        if (textFieldUsuario == null || textFieldUsuario.getText().isEmpty() ||
                textFieldContrasenia == null || textFieldContrasenia.getText().isEmpty()) {
            textFieldMensaje.setText("Credenciales null o vacias");
            return;
        }

        UsuarioEntity usuarioEntity = getUsuarioServiceModel().obtenerDatosUsuario(textFieldUsuario.getText());

        if (usuarioEntity == null) {
            textFieldMensaje.setText("El usuario no existe");
            return;
        }

        if ((textFieldUsuario.getText().equals(usuarioEntity.getEmail())
                || textFieldUsuario.getText().equals(usuarioEntity.getNombre()))
                && textFieldContrasenia.getText().equals(usuarioEntity.getContrasenia())) {
            textFieldMensaje.setText("Usuario validado correctamente");
            return;

        }
        textFieldMensaje.setText("Credenciales invalidas");
    }

    private void aplicarAnimacionesEntrada() {
        FadeTransition fadeTransition = new FadeTransition(Duration.millis(1500), rootVBox);
        fadeTransition.setFromValue(0);
        fadeTransition.setToValue(1);
        fadeTransition.play();

        TranslateTransition translateTransition = new TranslateTransition(Duration.millis(2200), imageLogoLogin);
        translateTransition.setFromY(-50);
        translateTransition.setToY(0);
        translateTransition.setInterpolator(Interpolator.EASE_OUT);
        translateTransition.play();

    }

    private void aplicarAnimacionesHover(Button boton) {
        boton.setOnMouseEntered(e -> {
            ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(300), boton);
            scaleTransition.setToX(1.07);
            scaleTransition.setToY(1.07);
            scaleTransition.play();
        });

        boton.setOnMouseExited(e -> {
            ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(300), boton);
            scaleTransition.setToX(1.0);
            scaleTransition.setToY(1.0);
            scaleTransition.play();
        });
    }

    // --- Animación de presión al hacer clic
    private void aplicarAnimacionesPresion(Button boton) {
        boton.addEventHandler(MouseEvent.MOUSE_PRESSED, e -> {
            ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(80), boton);
            scaleTransition.setToX(0.95);
            scaleTransition.setToY(0.95);
            scaleTransition.play();
        });

        boton.addEventHandler(MouseEvent.MOUSE_RELEASED, e -> {
            ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(150), boton);
            scaleTransition.setToX(1.0);
            scaleTransition.setToY(1.0);
            scaleTransition.play();
        });
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
            System.err.println("❌ Error al volver a la pantalla de login: " + e.getMessage());
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
