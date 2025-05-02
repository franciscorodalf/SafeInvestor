package es.franciscorodalf.saveinvestor.frontend.controller;

import java.io.IOException;

import es.franciscorodalf.saveinvestor.backend.model.Usuario;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class MainController {

    @FXML
    private Button btnEstadisticas;
    
    @FXML
    private Button btnObjetivos; // Añadimos referencia al botón de objetivos
    
    @FXML
    private Button btnGasto;
    
    @FXML
    private Button btnAhorro;

    @FXML
    private Button btnCerrarSesion;
    
    private Usuario usuarioActual;

    @FXML
    private void initialize() {
        // Inicialización de componentes
    }

    @FXML
    private void onPerfil(ActionEvent event) {
        cambiarEscena(event, "/es/franciscorodalf/saveinvestor/perfil.fxml");
    }
    
    @FXML
    private void onEstadisticas(ActionEvent event) {
        cambiarEscena(event, "/es/franciscorodalf/saveinvestor/estadisticas.fxml");
    }
    
    @FXML
    private void onObjetivos(ActionEvent event) {
        // Cambiamos la ruta para ir a una nueva vista de objetivos
        cambiarEscena(event, "/es/franciscorodalf/saveinvestor/objetivos.fxml");
    }
    
    @FXML
    private void onRegistrarGasto(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/es/franciscorodalf/saveinvestor/registrarGasto.fxml"));
            Parent root = loader.load();
            
            // Pasar el usuario al controlador de registro de gasto
            RegistrarGastoController controller = loader.getController();
            controller.setUsuario(usuarioActual);
            
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            System.err.println("Error al cargar vista de registro de gasto: " + e.getMessage());
        }
    }
    
    @FXML
    private void onRegistrarAhorro(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/es/franciscorodalf/saveinvestor/registrarAhorro.fxml"));
            Parent root = loader.load();
            
            // Pasar el usuario al controlador de registro de ahorro
            RegistrarAhorroController controller = loader.getController();
            controller.setUsuario(usuarioActual);
            
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            System.err.println("Error al cargar vista de registro de ahorro: " + e.getMessage());
        }
    }

    /**
     * Maneja el evento de cierre de sesión
     * @param event El evento de acción
     */
    @FXML
    private void onCerrarSesion(ActionEvent event) {
        try {
            // Simplemente carga la vista de login
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/es/franciscorodalf/saveinvestor/login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
            
            // Opcionalmente, podemos eliminar la referencia al usuario actual 
            this.usuarioActual = null;
        } catch (IOException e) {
            System.err.println("Error al volver a la pantalla de login: " + e.getMessage());
        }
    }

    @FXML
    private void onHistorial(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/es/franciscorodalf/saveinvestor/historial.fxml"));
            Parent root = loader.load();
            
            // Pasar el usuario al controlador
            HistorialController controller = loader.getController();
            controller.setUsuario(usuarioActual);
            
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            System.err.println("Error al cargar vista de historial: " + e.getMessage());
        }
    }

    public void setUsuario(Usuario usuario) {
        this.usuarioActual = usuario;
        // Aquí puedes inicializar la interfaz con los datos del usuario
        // Por ejemplo, mostrar su nombre o cargar sus estadísticas
    }

    private void cambiarEscena(ActionEvent event, String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            System.err.println("Error al cargar vista: " + e.getMessage());
        }
    }
}
