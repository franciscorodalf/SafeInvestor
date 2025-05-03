package es.franciscorodalf.saveinvestor.frontend.controller;

import es.franciscorodalf.saveinvestor.backend.dao.EstadisticaDAO;
import es.franciscorodalf.saveinvestor.backend.dao.TareaDAO;
import es.franciscorodalf.saveinvestor.backend.model.Usuario;
import es.franciscorodalf.saveinvestor.backend.model.estadistica;
import es.franciscorodalf.saveinvestor.backend.model.tarea;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;

import java.sql.SQLException;

public class EditarMovimientoController {

    @FXML private TextField txtConcepto;
    @FXML private TextField txtCantidad;
    @FXML private ComboBox<String> comboTipo;
    @FXML private Label lblMensaje;
    @FXML private Button btnGuardar;
    @FXML private Button btnCancelar;

    private tarea tareaActual;
    private Usuario usuarioActual;
    private TareaDAO tareaDAO;
    private EstadisticaDAO estadisticaDAO;
    private Runnable onCompletadoCallback;

    @FXML
    public void initialize() {
        tareaDAO = new TareaDAO();
        estadisticaDAO = new EstadisticaDAO();
        
        if (lblMensaje != null) {
            lblMensaje.setVisible(false);
        }
        
        // Configurar combobox de tipo
        if (comboTipo != null) {
            comboTipo.getItems().addAll("Ingreso", "Gasto");
        }
    }
    
    /**
     * Inicializa el controlador con los datos necesarios
     * @param tarea La tarea a editar
     * @param usuario El usuario actual
     * @param onCompletado Callback a ejecutar cuando se complete la operación
     */
    public void inicializar(tarea tarea, Usuario usuario, Runnable onCompletado) {
        this.tareaActual = tarea;
        this.usuarioActual = usuario;
        this.onCompletadoCallback = onCompletado;
        
        // Cargar datos en el formulario
        if (txtConcepto != null && tarea.getConcepto() != null) {
            txtConcepto.setText(tarea.getConcepto());
        }
        
        if (txtCantidad != null) {
            txtCantidad.setText(String.format("%.2f", tarea.getCantidad()));
        }
        
        if (comboTipo != null) {
            if (tarea.getEstado().equals(tarea.ESTADO_INGRESO)) {
                comboTipo.getSelectionModel().select("Ingreso");
            } else {
                comboTipo.getSelectionModel().select("Gasto");
            }
        }
    }

    @FXML
    private void onGuardar(ActionEvent event) {
        try {
            if (!validarCampos()) {
                return;
            }
            
            // Guardar valores antiguos para cálculos
            String estadoAntiguo = tareaActual.getEstado();
            double cantidadAntigua = tareaActual.getCantidad();
            
            // Actualizar datos de la tarea
            tareaActual.setConcepto(txtConcepto.getText().trim());
            tareaActual.setCantidad(Double.parseDouble(txtCantidad.getText().trim()));
            
            // Actualizar estado según el tipo seleccionado
            String nuevoEstado = comboTipo.getValue().equals("Ingreso") ? tarea.ESTADO_INGRESO : tarea.ESTADO_GASTO;
            tareaActual.setEstado(nuevoEstado);
            
            // Actualizar tarea en base de datos sin activar triggers
            tareaDAO.actualizarSinTrigger(tareaActual);
            
            // Actualizar estadísticas manualmente
            actualizarEstadisticas(estadoAntiguo, nuevoEstado, cantidadAntigua, tareaActual.getCantidad());
            
            // Mostrar mensaje de éxito
            mostrarMensaje("Movimiento actualizado correctamente", false);
            
            // Ejecutar callback para actualizar la interfaz principal
            if (onCompletadoCallback != null) {
                onCompletadoCallback.run();
            }
            
            // Cerrar ventana después de un breve retraso
            new java.util.Timer().schedule(
                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        javafx.application.Platform.runLater(() -> cerrarVentana());
                    }
                }, 
                1000 // esperar 1 segundo antes de cerrar
            );
            
        } catch (Exception e) {
            mostrarMensaje("Error al actualizar: " + e.getMessage(), true);
            e.printStackTrace();
        }
    }
    
    /**
     * Actualiza las estadísticas considerando los cambios en el movimiento
     */
    private void actualizarEstadisticas(String estadoAntiguo, String estadoNuevo, 
                                         double cantidadAntigua, double cantidadNueva) throws SQLException {
        
        if (usuarioActual == null || usuarioActual.getId() == null) return;
        
        estadistica stats = estadisticaDAO.obtenerPorUsuario(usuarioActual.getId());
        if (stats == null) {
            // Si no hay estadísticas, crear una nueva con los valores actuales
            double ingresos = estadoNuevo.equals(tarea.ESTADO_INGRESO) ? cantidadNueva : 0;
            double gastos = estadoNuevo.equals(tarea.ESTADO_GASTO) ? cantidadNueva : 0;
            estadisticaDAO.insertar(new estadistica(ingresos, gastos, usuarioActual.getId()));
            return;
        }
        
        // Ajustar estadísticas según los cambios
        // 1. Restar valores antiguos
        if (estadoAntiguo.equals(tarea.ESTADO_INGRESO)) {
            stats.setTotalIngreso(stats.getTotalIngreso() - cantidadAntigua);
        } else if (estadoAntiguo.equals(tarea.ESTADO_GASTO)) {
            stats.setTotalGasto(stats.getTotalGasto() - cantidadAntigua);
        }
        
        // 2. Sumar valores nuevos
        if (estadoNuevo.equals(tarea.ESTADO_INGRESO)) {
            stats.setTotalIngreso(stats.getTotalIngreso() + cantidadNueva);
        } else if (estadoNuevo.equals(tarea.ESTADO_GASTO)) {
            stats.setTotalGasto(stats.getTotalGasto() + cantidadNueva);
        }
        
        // 3. Actualizar en base de datos
        estadisticaDAO.actualizar(stats);
    }

    @FXML
    private void onCancelar(ActionEvent event) {
        cerrarVentana();
    }
    
    private boolean validarCampos() {
        // Validar concepto
        if (txtConcepto.getText().trim().isEmpty()) {
            mostrarMensaje("El concepto no puede estar vacío", true);
            return false;
        }
        
        // Validar cantidad
        try {
            double cantidad = Double.parseDouble(txtCantidad.getText().trim());
            if (cantidad <= 0) {
                mostrarMensaje("La cantidad debe ser mayor que cero", true);
                return false;
            }
        } catch (NumberFormatException e) {
            mostrarMensaje("La cantidad debe ser un número válido", true);
            return false;
        }
        
        // Validar tipo seleccionado
        if (comboTipo.getValue() == null) {
            mostrarMensaje("Debe seleccionar un tipo de movimiento", true);
            return false;
        }
        
        return true;
    }
    
    private void mostrarMensaje(String mensaje, boolean esError) {
        if (lblMensaje != null) {
            lblMensaje.setText(mensaje);
            lblMensaje.setStyle("-fx-text-fill: " + (esError ? "red" : "green") + ";");
            lblMensaje.setVisible(true);
        } else {
            // Si no hay label, mostrar alerta
            Alert alert = new Alert(esError ? AlertType.ERROR : AlertType.INFORMATION);
            alert.setTitle(esError ? "Error" : "Información");
            alert.setHeaderText(null);
            alert.setContentText(mensaje);
            alert.showAndWait();
        }
    }
    
    private void cerrarVentana() {
        if (btnCancelar != null && btnCancelar.getScene() != null && btnCancelar.getScene().getWindow() != null) {
            Stage stage = (Stage) btnCancelar.getScene().getWindow();
            stage.close();
        }
    }
}
