package es.franciscorodalf.saveinvestor.frontend.controller;

import java.io.IOException;
import java.util.Date;

import es.franciscorodalf.saveinvestor.backend.dao.TareaDAO;
import es.franciscorodalf.saveinvestor.backend.model.tarea;
import es.franciscorodalf.saveinvestor.backend.model.Usuario;

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

    @FXML
    private void initialize() {
        tareaDAO = new TareaDAO();
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
                double cantidad = Double.parseDouble(txtCantidad.getText());
                String concepto = txtConcepto.getText();
                
                tarea nuevoGasto = new tarea(
                    concepto,
                    cantidad,  // La cantidad se guarda como positiva
                    new Date(),
                    tarea.ESTADO_GASTO,
                    usuarioActual.getId()
                );
                
                tareaDAO.insertar(nuevoGasto);
                mostrarMensaje("Gasto registrado correctamente");
                
                // Volver a la pantalla principal
                volverAMain(event);
                
            } catch (NumberFormatException e) {
                mostrarMensaje("La cantidad debe ser un número válido");
            } catch (Exception e) {
                mostrarMensaje("Error al registrar gasto: " + e.getMessage());
                System.err.println("Error: " + e.getMessage());
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
}