package es.franciscorodalf.saveinvestor.frontend.controller;

import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import javafx.scene.input.MouseEvent;

public class loginController {

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
    private TextField textLabelUsuario;
    @FXML
    private PasswordField textLabelContrasenia;
    @FXML
    private Hyperlink textLinkOlvidarContrasenia;

    @FXML
    public void initialize() {
        aplicarAnimacionesEntrada();
        aplicarAnimacionesHover(buttonAceptarlLogin);
        aplicarAnimacionesPresion(buttonAceptarlLogin);
        aplicarAnimacionesPresion(buttonLoginRegistrar);

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

}
