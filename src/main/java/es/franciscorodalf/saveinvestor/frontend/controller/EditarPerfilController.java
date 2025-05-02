package es.franciscorodalf.saveinvestor.frontend.controller;

import java.io.IOException;

import es.franciscorodalf.saveinvestor.backend.dao.UsuarioDAO;
import es.franciscorodalf.saveinvestor.backend.model.Usuario;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * Controlador para la edición de perfil.
 * Actualmente solo navega entre pantallas.
 */
public class EditarPerfilController {

    @FXML
    private TextField txtNombre;
    
    @FXML
    private TextField txtEmail;
    
    @FXML
    private TextField txtPassword;
    
    @FXML
    private TextField txtConfirmPassword;
    
    @FXML
    private Label lblMensaje;
    
    @FXML
    private Button btnGuardar;
    
    @FXML
    private Button btnCancelar;
    
    private Usuario usuarioActual;
    private UsuarioDAO usuarioDAO;
    
    @FXML
    private void initialize() {
        usuarioDAO = new UsuarioDAO();
        
        if (lblMensaje != null) {
            lblMensaje.setVisible(false);
        }
    }
    
    /**
     * Establece el usuario actual y carga sus datos en el formulario
     * @param usuario El usuario a editar
     */
    public void setUsuario(Usuario usuario) {
        this.usuarioActual = usuario;
        cargarDatosUsuario();
    }
    
    /**
     * Carga los datos del usuario en los campos del formulario
     */
    private void cargarDatosUsuario() {
        if (usuarioActual == null) return;
        
        if (txtNombre != null && usuarioActual.getNombre() != null) {
            txtNombre.setText(usuarioActual.getNombre());
        }
        
        if (txtEmail != null && usuarioActual.getEmail() != null) {
            txtEmail.setText(usuarioActual.getEmail());
        }
    }
    
    @FXML
    private void onGuardar(ActionEvent event) {
        if (validarFormulario()) {
            actualizarUsuario();
            volverAPerfil(event);
        }
    }
    
    @FXML
    private void onCancelar(ActionEvent event) {
        volverAPerfil(event);
    }
    
    /**
     * Valida los campos del formulario
     * @return true si todos los campos son válidos
     */
    private boolean validarFormulario() {
        // Validar que el nombre no esté vacío
        if (txtNombre.getText().trim().isEmpty()) {
            mostrarMensaje("El nombre no puede estar vacío");
            return false;
        }
        
        // Validar formato de email
        if (txtEmail.getText().trim().isEmpty() || !txtEmail.getText().contains("@")) {
            mostrarMensaje("Debe ingresar un email válido");
            return false;
        }
        
        // Validar que las contraseñas coincidan si se han ingresado
        if (!txtPassword.getText().isEmpty() && !txtPassword.getText().equals(txtConfirmPassword.getText())) {
            mostrarMensaje("Las contraseñas no coinciden");
            return false;
        }
        
        // Si todo está bien
        return true;
    }
    
    /**
     * Actualiza los datos del usuario con los valores ingresados
     */
    private void actualizarUsuario() {
        try {
            // Actualizar nombre y email
            usuarioActual.setNombre(txtNombre.getText().trim());
            usuarioActual.setEmail(txtEmail.getText().trim());
            
            // Actualizar contraseña solo si se ingresó una nueva
            if (!txtPassword.getText().isEmpty()) {
                usuarioActual.setContrasenia(txtPassword.getText());
            }
            
            // Guardar cambios en la base de datos
            usuarioDAO.actualizar(usuarioActual);
            
            mostrarMensaje("Perfil actualizado correctamente");
        } catch (Exception e) {
            mostrarMensaje("Error al actualizar perfil: " + e.getMessage());
            System.err.println("Error al actualizar usuario: " + e.getMessage());
            e.printStackTrace();
        }
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
            System.out.println(mensaje);
        }
    }
    
    /**
     * Vuelve a la pantalla de perfil
     * @param event Evento de acción
     */
    private void volverAPerfil(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/es/franciscorodalf/saveinvestor/perfil.fxml"));
            Parent root = loader.load();
            
            // Pasar el usuario actualizado al controlador de perfil
            if (usuarioActual != null) {
                PerfilController controller = loader.getController();
                controller.setUsuario(usuarioActual);
            }
            
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            System.err.println("Error al volver a la pantalla de perfil: " + e.getMessage());
            e.printStackTrace();
        }
    }
}