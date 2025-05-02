package es.franciscorodalf.saveinvestor.frontend.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

import es.franciscorodalf.saveinvestor.backend.model.Usuario;

/**
 * Controlador de la vista de Objetivos financieros.
 */
public class ObjetivosController {

    @FXML
    private TextField txtDescripcion;
    
    @FXML
    private TextField txtCantidad;
    
    @FXML
    private Label lblMensaje;
    
    @FXML
    private ListView<String> listObjetivos;
    
    @FXML
    private Button btnAgregarObjetivo;
    
    @FXML
    private Button btnVolver;

    private Usuario usuarioActual;

    /**
     * Inicializa el controlador
     */
    @FXML
    public void initialize() {
        // Inicializar componentes
        if (lblMensaje != null) {
            lblMensaje.setVisible(false);
        }
        
        // Conectar eventos
        if (btnAgregarObjetivo != null) {
            btnAgregarObjetivo.setOnAction(this::onAgregarObjetivo);
        }
        
        if (btnVolver != null) {
            btnVolver.setOnAction(this::onVolver);
        }
    }
    
    /**
     * Establece el usuario actual
     * @param usuario El usuario actual de la aplicación
     */
    public void setUsuario(Usuario usuario) {
        this.usuarioActual = usuario;
        // En futuro: cargar objetivos del usuario desde la base de datos
    }

    /**
     * Maneja el evento de agregar un nuevo objetivo
     * @param event El evento de acción
     */
    @FXML
    private void onAgregarObjetivo(ActionEvent event) {
        if (validarCampos()) {
            String descripcion = txtDescripcion.getText().trim();
            double cantidad = Double.parseDouble(txtCantidad.getText().trim());
            
            // Futura implementación: guardar en BD
            // Por ahora solo lo añadimos a la lista visual
            listObjetivos.getItems().add(descripcion + ": $" + String.format("%.2f", cantidad));
            
            // Limpiar campos
            txtDescripcion.clear();
            txtCantidad.clear();
            lblMensaje.setVisible(false);
        }
    }

    /**
     * Valida los campos del formulario
     * @return true si los campos son válidos, false en caso contrario
     */
    private boolean validarCampos() {
        if (txtDescripcion == null || txtCantidad == null) {
            return false;
        }
        
        if (txtDescripcion.getText() == null || txtDescripcion.getText().trim().isEmpty()) {
            mostrarMensaje("La descripción no puede estar vacía");
            return false;
        }
        
        try {
            double cantidad = Double.parseDouble(txtCantidad.getText().trim());
            if (cantidad <= 0) {
                mostrarMensaje("La cantidad debe ser mayor que cero");
                return false;
            }
        } catch (NumberFormatException e) {
            mostrarMensaje("La cantidad debe ser un número válido");
            return false;
        }
        
        return true;
    }

    /**
     * Muestra un mensaje en la interfaz
     * @param mensaje El mensaje a mostrar
     */
    private void mostrarMensaje(String mensaje) {
        if (lblMensaje != null) {
            lblMensaje.setText(mensaje);
            lblMensaje.setVisible(true);
        } else {
            System.err.println(mensaje);
        }
    }

    /**
     * Maneja el evento del botón volver
     * @param event El evento de acción
     */
    @FXML
    private void onVolver(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/es/franciscorodalf/saveinvestor/main.fxml"));
            Parent root = loader.load();
            
            // Pasar el usuario al controlador principal
            if (usuarioActual != null) {
                MainController controller = loader.getController();
                controller.setUsuario(usuarioActual);
            }
            
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            System.err.println("Error al cargar vista principal: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
