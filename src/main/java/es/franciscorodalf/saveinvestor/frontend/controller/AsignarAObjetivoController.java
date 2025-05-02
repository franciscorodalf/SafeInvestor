package es.franciscorodalf.saveinvestor.frontend.controller;

import java.sql.SQLException;
import java.util.List;

import es.franciscorodalf.saveinvestor.backend.dao.ObjetivoDAO;
import es.franciscorodalf.saveinvestor.backend.model.Objetivo;
import es.franciscorodalf.saveinvestor.backend.model.Usuario;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class AsignarAObjetivoController {
    
    @FXML
    private ComboBox<Objetivo> comboObjetivos;
    
    @FXML
    private Label lblCantidad;
    
    @FXML
    private Label lblMensaje;
    
    @FXML
    private Button btnAsignar;
    
    @FXML
    private Button btnCancelar;
    
    private Usuario usuario;
    private Double cantidad;
    private ObjetivoDAO objetivoDAO;
    private boolean asignacionExitosa = false;
    
    @FXML
    private void initialize() {
        objetivoDAO = new ObjetivoDAO();
        
        if (lblMensaje != null) {
            lblMensaje.setVisible(false);
        }
        
        // Configurar el formato de visualización para el ComboBox
        configuraComboBox();
    }
    
    private void configuraComboBox() {
        if (comboObjetivos == null) return;
        
        comboObjetivos.setCellFactory(param -> new javafx.scene.control.ListCell<Objetivo>() {
            @Override
            protected void updateItem(Objetivo item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%s - %.2f$ de %.2f$ (%.1f%%)", 
                            item.getDescripcion(), 
                            item.getCantidadActual(),
                            item.getCantidadObjetivo(),
                            item.calcularPorcentaje()));
                }
            }
        });
        
        comboObjetivos.setButtonCell(new javafx.scene.control.ListCell<Objetivo>() {
            @Override
            protected void updateItem(Objetivo item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("Seleccionar objetivo");
                } else {
                    setText(item.getDescripcion());
                }
            }
        });
    }
    
    public void setDatos(Usuario usuario, Double cantidad) {
        this.usuario = usuario;
        this.cantidad = cantidad;
        
        if (lblCantidad != null) {
            lblCantidad.setText(String.format("%.2f$", cantidad));
        }
        
        cargarObjetivos();
    }
    
    private void cargarObjetivos() {
        try {
            if (usuario != null && usuario.getId() != null && comboObjetivos != null) {
                List<Objetivo> objetivos = objetivoDAO.obtenerObjetivosActivos(usuario.getId());
                
                if (objetivos.isEmpty()) {
                    mostrarMensaje("No hay objetivos activos", true);
                    if (btnAsignar != null) {
                        btnAsignar.setDisable(true);
                    }
                } else {
                    comboObjetivos.setItems(FXCollections.observableArrayList(objetivos));
                    // Seleccionar el primer objetivo por defecto
                    comboObjetivos.getSelectionModel().select(0);
                }
            }
        } catch (SQLException e) {
            mostrarMensaje("Error al cargar objetivos: " + e.getMessage(), true);
            System.err.println("Error al cargar objetivos: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void onAsignar(ActionEvent event) {
        Objetivo seleccionado = comboObjetivos.getValue();
        if (seleccionado == null) {
            mostrarMensaje("Debe seleccionar un objetivo", true);
            return;
        }
        
        try {
            // Contribuir al objetivo seleccionado
            seleccionado.contribuir(cantidad);
            objetivoDAO.actualizar(seleccionado);
            
            asignacionExitosa = true;
            
            if (seleccionado.isCompletado()) {
                mostrarMensaje("¡Objetivo completado!", false);
            } else {
                mostrarMensaje("Contribución asignada correctamente", false);
            }
            
            // Deshabilitar el botón para evitar doble asignación
            if (btnAsignar != null) {
                btnAsignar.setDisable(true);
            }
            
        } catch (Exception e) {
            mostrarMensaje("Error al asignar contribución: " + e.getMessage(), true);
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void onCancelar(ActionEvent event) {
        cerrarVentana();
    }
    
    private void mostrarMensaje(String mensaje, boolean esError) {
        if (lblMensaje != null) {
            lblMensaje.setText(mensaje);
            lblMensaje.setVisible(true);
            
            if (esError) {
                lblMensaje.setStyle("-fx-text-fill: red;");
            } else {
                lblMensaje.setStyle("-fx-text-fill: green;");
            }
        } else {
            System.out.println(mensaje);
        }
    }
    
    private void cerrarVentana() {
        if (btnCancelar != null && btnCancelar.getScene() != null && 
            btnCancelar.getScene().getWindow() instanceof Stage) {
            Stage stage = (Stage) btnCancelar.getScene().getWindow();
            stage.close();
        }
    }
    
    public boolean isAsignacionExitosa() {
        return asignacionExitosa;
    }
}
