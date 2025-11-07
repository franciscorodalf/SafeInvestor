package es.franciscorodalf.saveinvestor.frontend.controller;

import es.franciscorodalf.saveinvestor.backend.model.Usuario;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;
/**
 * Main dashboard controller that orchestrates navigation between all application views.
 */
public class DashboardController implements UsuarioAware {

    private static final String RUTA_RESUMEN = "/es/franciscorodalf/saveinvestor/main.fxml";
    private static final String RUTA_HISTORIAL = "/es/franciscorodalf/saveinvestor/historial.fxml";
    private static final String RUTA_OBJETIVOS = "/es/franciscorodalf/saveinvestor/objetivos.fxml";
    private static final String RUTA_ESTADISTICAS = "/es/franciscorodalf/saveinvestor/estadisticas.fxml";
    private static final String RUTA_PORTAFOLIO = "/es/franciscorodalf/saveinvestor/portafolio.fxml";
    private static final String RUTA_PERFIL = "/es/franciscorodalf/saveinvestor/perfil.fxml";

    @FXML
    private StackPane contentContainer;

    @FXML
    private Label lblUsuario;

    @FXML
    private Label lblEstado;

    @FXML
    private Button btnResumen;

    @FXML
    private Button btnHistorial;

    @FXML
    private Button btnObjetivos;

    @FXML
    private Button btnEstadisticas;

    @FXML
    private Button btnPortafolio;

    @FXML
    private Button btnPerfil;

    private Usuario usuarioActual;
    private Button botonActivo;

    @FXML
    private void initialize() {
        if (lblEstado != null) {
            lblEstado.setVisible(false);
        }
    }

    @Override
    public void setUsuario(Usuario usuario) {
        this.usuarioActual = usuario;
        if (lblUsuario != null) {
            lblUsuario.setText(usuario != null ? usuario.getNombre() : "Invitado");
        }
        mostrarVistaResumen();
    }

    @FXML
    private void onMostrarResumen(ActionEvent event) {
        mostrarVistaResumen();
    }

    @FXML
    private void onMostrarHistorial(ActionEvent event) {
        mostrarVistaHistorial();
    }

    @FXML
    private void onMostrarObjetivos(ActionEvent event) {
        mostrarVistaObjetivos();
    }

    @FXML
    private void onMostrarEstadisticas(ActionEvent event) {
        mostrarVistaEstadisticas();
    }

    @FXML
    private void onMostrarPortafolio(ActionEvent event) {
        mostrarVistaPortafolio();
    }

    @FXML
    private void onMostrarPerfil(ActionEvent event) {
        mostrarVistaPerfil();
    }

    @FXML
    private void onCerrarSesion(ActionEvent event) {
        cerrarSesion();
    }

    public void mostrarVistaResumen() {
        cargarVistaInterno(btnResumen, RUTA_RESUMEN);
    }

    public void mostrarVistaHistorial() {
        cargarVistaInterno(btnHistorial, RUTA_HISTORIAL);
    }

    public void mostrarVistaObjetivos() {
        cargarVistaInterno(btnObjetivos, RUTA_OBJETIVOS);
    }

    public void mostrarVistaEstadisticas() {
        cargarVistaInterno(btnEstadisticas, RUTA_ESTADISTICAS);
    }

    public void mostrarVistaPortafolio() {
        cargarVistaInterno(btnPortafolio, RUTA_PORTAFOLIO);
    }

    public void mostrarVistaPerfil() {
        cargarVistaInterno(btnPerfil, RUTA_PERFIL);
    }

    public void navegarA(String rutaFXML) {
        cargarVistaInterno(null, rutaFXML);
    }

    private void cargarVistaInterno(Button boton, String rutaFXML) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(rutaFXML));
            Parent vista = loader.load();

            Object controller = loader.getController();
            if (controller instanceof UsuarioAware && usuarioActual != null) {
                ((UsuarioAware) controller).setUsuario(usuarioActual);
            }
            if (controller instanceof DashboardNavigable) {
                ((DashboardNavigable) controller).setDashboardController(this);
            }

            contentContainer.getChildren().setAll(vista);
            actualizarBotonActivo(boton);
            mostrarEstado(null);
        } catch (IOException e) {
            mostrarEstado("No se pudo cargar la vista solicitada.");
            e.printStackTrace();
        }
    }

    private void actualizarBotonActivo(Button nuevoActivo) {
        if (botonActivo != null) {
            botonActivo.getStyleClass().remove("nav-button-active");
        }
        if (nuevoActivo != null && !nuevoActivo.getStyleClass().contains("nav-button-active")) {
            nuevoActivo.getStyleClass().add("nav-button-active");
        }
        botonActivo = nuevoActivo;
    }

    private void mostrarEstado(String mensaje) {
        if (lblEstado == null) {
            return;
        }
        if (mensaje == null || mensaje.isBlank()) {
            lblEstado.setVisible(false);
            lblEstado.setText("");
        } else {
            lblEstado.setText(mensaje);
            lblEstado.setVisible(true);
        }
    }

    public void cerrarSesion() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/es/franciscorodalf/saveinvestor/login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) contentContainer.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            mostrarEstado("No se pudo cerrar la sesión.");
            e.printStackTrace();
        }
    }
}
