package es.franciscorodalf.saveinvestor.frontend.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;

import es.franciscorodalf.saveinvestor.backend.dao.EstadisticaDAO;
import es.franciscorodalf.saveinvestor.backend.dao.TareaDAO;
import es.franciscorodalf.saveinvestor.backend.model.tarea;
import es.franciscorodalf.saveinvestor.backend.model.Usuario;
import es.franciscorodalf.saveinvestor.backend.model.estadistica;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class RegistrarGastoController {

    @FXML
    private TextField txtCantidad;
    
    @FXML
    private TextField txtConcepto;
    
    @FXML
    private Label lblMensaje;

    private Usuario usuarioActual;
    private TareaDAO tareaDAO;
    private EstadisticaDAO estadisticaDAO;

    @FXML
    private void initialize() {
        tareaDAO = new TareaDAO();
        estadisticaDAO = new EstadisticaDAO();
        if (lblMensaje != null) {
            lblMensaje.setVisible(false);
        }
    }

    public void setUsuario(Usuario usuario) {
        this.usuarioActual = usuario;
    }

    @FXML
    private void onVolver(ActionEvent event) {
        volverAMain(event);
    }

    @FXML
    private void onAceptar(ActionEvent event) {
        if (validarCampos()) {
            try {
                double cantidad = Double.parseDouble(txtCantidad.getText().trim());
                String concepto = txtConcepto.getText().trim();
                
                // Crear nueva tarea de tipo GASTO
                tarea nuevoGasto = new tarea(
                    concepto,
                    cantidad,
                    new Date(),
                    tarea.ESTADO_GASTO,
                    usuarioActual.getId()
                );
                
                // Guardar en la base de datos sin activar triggers
                tareaDAO.insertarSinTrigger(nuevoGasto);
                
                // Actualizar estadísticas manualmente
                actualizarEstadisticas(cantidad);
                
                mostrarMensaje("Gasto registrado correctamente");
                
                // Volver a la pantalla principal
                volverAMain(event);
                
            } catch (NumberFormatException e) {
                mostrarMensaje("La cantidad debe ser un número válido");
            } catch (Exception e) {
                mostrarMensaje("Error al registrar gasto");
                System.err.println("Error al registrar gasto: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    private boolean validarCampos() {
        if (txtConcepto.getText().isEmpty()) {
            mostrarMensaje("Debe ingresar un concepto");
            return false;
        }
        
        if (txtCantidad.getText().isEmpty()) {
            mostrarMensaje("Debe ingresar una cantidad");
            return false;
        }
        
        try {
            double cantidad = Double.parseDouble(txtCantidad.getText());
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

    private void volverAMain(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/es/franciscorodalf/saveinvestor/main.fxml"));
            Parent root = loader.load();
            
            // Pasar el usuario al controlador principal
            MainController controller = loader.getController();
            controller.setUsuario(usuarioActual);
            
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            System.err.println("Error al volver a la pantalla principal: " + e.getMessage());
        }
    }

    // Implementación del método para actualizar estadísticas manualmente
    private void actualizarEstadisticas(double cantidad) throws SQLException {
        // Buscamos la estadística del usuario
        estadistica stats = estadisticaDAO.obtenerPorUsuario(usuarioActual.getId());
        
        if (stats != null) {
            // Actualizamos total de gastos usando el método correcto
            stats.setTotalGasto(stats.getTotalGasto() + cantidad);
            // Guardamos los cambios
            estadisticaDAO.actualizar(stats);
        } else {
            // Si no existe estadística para este usuario, la creamos
            estadistica nuevaEstadistica = new estadistica(0.0, cantidad, usuarioActual.getId());
            estadisticaDAO.insertar(nuevaEstadistica);
        }
    }
}