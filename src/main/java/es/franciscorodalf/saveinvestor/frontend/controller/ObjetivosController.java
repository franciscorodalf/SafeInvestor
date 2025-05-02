package es.franciscorodalf.saveinvestor.frontend.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import es.franciscorodalf.saveinvestor.backend.model.Objetivo;
import es.franciscorodalf.saveinvestor.backend.model.Usuario;
import es.franciscorodalf.saveinvestor.backend.dao.ObjetivoDAO;

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

    @FXML
    private DatePicker dateFechaObjetivo;
    private Usuario usuarioActual;

    private ObjetivoDAO objetivoDAO;

    @FXML
    public void initialize() {
        // Inicializar componentes
        if (lblMensaje != null) {
            lblMensaje.setVisible(false);
        }

        objetivoDAO = new ObjetivoDAO();
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
     * 
     * @param usuario El usuario actual de la aplicación
     */
    public void setUsuario(Usuario usuario) {
        this.usuarioActual = usuario;
        // En futuro: cargar objetivos del usuario desde la base de datos
    }

    /**
     * Maneja el evento de agregar un nuevo objetivo
     * 
     * @param event El evento de acción
     */
    @FXML
    private void onAgregarObjetivo(ActionEvent event) {
        if (validarCampos()) {
            try {
                String descripcion = txtDescripcion.getText().trim();
                double cantidad = Double.parseDouble(txtCantidad.getText().trim());

                // Crear nuevo objetivo
                Objetivo nuevoObjetivo = new Objetivo(descripcion, cantidad, usuarioActual.getId());

                // Establecer fecha límite si se proporcionó
                if (dateFechaObjetivo != null && dateFechaObjetivo.getValue() != null) {
                    LocalDate fechaLocal = dateFechaObjetivo.getValue();
                    // Convertir LocalDate a Date
                    Date fecha = Date.from(fechaLocal.atStartOfDay(ZoneId.systemDefault()).toInstant());
                    nuevoObjetivo.setFechaObjetivo(fecha);
                }

                // Guardar en base de datos
                objetivoDAO.insertar(nuevoObjetivo);

                // Cargar lista actualizada de objetivos
                cargarObjetivos();

                // Limpiar campos
                txtDescripcion.clear();
                txtCantidad.clear();
                if (dateFechaObjetivo != null) {
                    dateFechaObjetivo.setValue(null);
                }

                lblMensaje.setVisible(false);
            } catch (Exception e) {
                mostrarMensaje("Error al guardar objetivo: " + e.getMessage());
                System.err.println("Error: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Valida los campos del formulario
     * 
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

    private void mostrarMensaje(String mensaje) {
        if (lblMensaje != null) {
            lblMensaje.setText(mensaje);
            lblMensaje.setVisible(true);
        } else {
            System.err.println(mensaje);
        }
    }

    /**
     * Carga la lista de objetivos del usuario actual
     */
    private void cargarObjetivos() {
        if (usuarioActual != null && listObjetivos != null) {
            listObjetivos.getItems().clear();
            // Obtener objetivos del usuario desde la base de datos
            try {
                for (Objetivo objetivo : objetivoDAO.obtenerPorUsuario(usuarioActual.getId())) {
                    listObjetivos.getItems().add(objetivo.getDescripcion() + " - $" + objetivo.getMonto());
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Maneja el evento del botón volver
     * 
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
