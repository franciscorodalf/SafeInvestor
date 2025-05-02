package es.franciscorodalf.saveinvestor.frontend.controller;

import java.io.IOException;
import java.util.Date;

import es.franciscorodalf.saveinvestor.backend.dao.TareaDAO;
import es.franciscorodalf.saveinvestor.backend.model.Usuario;
import es.franciscorodalf.saveinvestor.backend.model.tarea;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class RegistrarAhorroController {

    @FXML
    private TextField txtCantidad;
    
    @FXML
    private TextField txtConcepto;
    
    @FXML
    private Label lblError;

    private Usuario usuarioActual;
    private TareaDAO tareaDAO;

    @FXML
    public void initialize() {
        tareaDAO = new TareaDAO();
        if (lblError != null) {
            lblError.setVisible(false);
        }
    }

    @FXML
    private void onVolver(ActionEvent event) {
        volverAMain(event);
    }

    @FXML
    private void onAceptar(ActionEvent event) {
        if (validarEntrada()) {
            guardarAhorro();
            volverAMain(event);
        }
    }

    private boolean validarEntrada() {
        if (txtConcepto.getText().trim().isEmpty()) {
            mostrarError("El concepto no puede estar vacío");
            return false;
        }
        
        try {
            double cantidad = Double.parseDouble(txtCantidad.getText().trim());
            if (cantidad <= 0) {
                mostrarError("La cantidad debe ser mayor que cero");
                return false;
            }
        } catch (NumberFormatException e) {
            mostrarError("La cantidad debe ser un número válido");
            return false;
        }
        
        return true;
    }

    private void guardarAhorro() {
        try {
            double cantidad = Double.parseDouble(txtCantidad.getText().trim());
            String concepto = txtConcepto.getText().trim();
            
            // Crear nueva tarea de tipo INGRESO
            tarea nuevoAhorro = new tarea(
                concepto,
                cantidad,
                new Date(),
                tarea.ESTADO_INGRESO,
                usuarioActual.getId()
            );
            
            // Guardar en la base de datos
            tareaDAO.insertar(nuevoAhorro);
            
        } catch (Exception e) {
            mostrarError("Error al guardar el ahorro: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void mostrarError(String mensaje) {
        if (lblError != null) {
            lblError.setText(mensaje);
            lblError.setVisible(true);
        } else {
            System.err.println(mensaje);
        }
    }

    private void volverAMain(ActionEvent event) {
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
            System.err.println("Error al volver a la pantalla principal: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void setUsuario(Usuario usuario) {
        this.usuarioActual = usuario;
    }
}