package es.franciscorodalf.saveinvestor.frontend.controller;

import es.franciscorodalf.saveinvestor.backend.dao.TareaDAO;
import es.franciscorodalf.saveinvestor.backend.model.Usuario;
import es.franciscorodalf.saveinvestor.backend.model.tarea;
import es.franciscorodalf.saveinvestor.frontend.model.Movimiento;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class HistorialController {

    @FXML private TableView<Movimiento> tablaHistorial;
    @FXML private TableColumn<Movimiento, LocalDate> colFecha;
    @FXML private TableColumn<Movimiento, String> colTipo;
    @FXML private TableColumn<Movimiento, String> colConcepto;
    @FXML private TableColumn<Movimiento, Double> colCantidad;
    @FXML private Label lblError;

    private Usuario usuarioActual;
    private TareaDAO tareaDAO;

    @FXML 
    private void initialize() {
        tareaDAO = new TareaDAO();
        
        // Configurar celdas
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        colFecha.setCellFactory(col -> new javafx.scene.control.TableCell<Movimiento, LocalDate>() {
            @Override
            protected void updateItem(LocalDate fecha, boolean empty) {
                super.updateItem(fecha, empty);
                if (empty || fecha == null) {
                    setText(null);
                } else {
                    setText(fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                }
            }
        });
        
        colTipo.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getTipo().toString()));
        
        colConcepto.setCellValueFactory(new PropertyValueFactory<>("concepto"));
        
        colCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colCantidad.setCellFactory(col -> new javafx.scene.control.TableCell<Movimiento, Double>() {
            @Override
            protected void updateItem(Double cantidad, boolean empty) {
                super.updateItem(cantidad, empty);
                if (empty || cantidad == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f$", cantidad));
                }
            }
        });
        
        if (lblError != null) {
            lblError.setVisible(false);
        }
    }
    
    /**
     * Asigna el usuario actual y carga sus movimientos
     */
    public void setUsuario(Usuario usuario) {
        this.usuarioActual = usuario;
        cargarMovimientos();
    }
    
    /**
     * Carga los movimientos del usuario desde la base de datos
     */
    private void cargarMovimientos() {
        if (usuarioActual == null || usuarioActual.getId() == null) {
            mostrarError("No hay usuario actual para mostrar historial");
            return;
        }
        
        try {
            List<tarea> tareas = tareaDAO.obtenerPorUsuario(usuarioActual.getId());
            ObservableList<Movimiento> movimientos = FXCollections.observableArrayList();
            
            for (tarea t : tareas) {
                movimientos.add(Movimiento.fromTarea(t));
            }
            
            tablaHistorial.setItems(movimientos);
            
        } catch (SQLException e) {
            mostrarError("Error al cargar historial: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void mostrarError(String mensaje) {
        System.err.println(mensaje);
        if (lblError != null) {
            lblError.setText(mensaje);
            lblError.setVisible(true);
        }
    }

    @FXML
    private void onVolver(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/es/franciscorodalf/saveinvestor/main.fxml"));
            Parent root = loader.load();
            
            // Pasar el usuario al controlador principal
            if (usuarioActual != null) {
                MainController mainController = loader.getController();
                mainController.setUsuario(usuarioActual);
            }
            
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            mostrarError("Error al volver: " + e.getMessage());
            e.printStackTrace();
        }
    }
}