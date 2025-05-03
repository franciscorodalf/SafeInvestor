package es.franciscorodalf.saveinvestor.frontend.controller;

import java.io.IOException;

import es.franciscorodalf.saveinvestor.backend.dao.UsuarioDAO;
import es.franciscorodalf.saveinvestor.backend.model.Usuario;
import es.franciscorodalf.saveinvestor.frontend.util.ValidationUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * Controlador para la edición de perfil.
 */
public class EditarPerfilController {

    @FXML
    private TextField txtNombre;
    
    @FXML
    private TextField txtEmail;
    
    @FXML
    private PasswordField txtPassword;
    
    @FXML
    private PasswordField txtConfirmPassword;
    
    @FXML
    private Label lblMensaje;
    
    @FXML
    private Button btnGuardar;
    
    @FXML
    private Button btnVolver;
    
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
        if (ValidationUtils.isEmptyField(txtNombre)) {
            mostrarMensajeError("El nombre de usuario no puede estar vacío");
            return false;
        }
        
        // Validar formato de email
        if (ValidationUtils.isEmptyField(txtEmail) || !ValidationUtils.isValidEmail(txtEmail.getText())) {
            mostrarMensajeError("Debe ingresar un correo electrónico válido");
            return false;
        }
        
        // Validar que las contraseñas coincidan si se han ingresado
        if (!txtPassword.getText().isEmpty() && !txtPassword.getText().equals(txtConfirmPassword.getText())) {
            mostrarMensajeError("Las contraseñas no coinciden");
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
            
            mostrarMensajeExito("Perfil actualizado correctamente");
        } catch (Exception e) {
            mostrarMensajeError("Error al actualizar perfil: " + e.getMessage());
        }
    }
    
    /**
     * Muestra un mensaje de error en la interfaz
     * @param mensaje El mensaje a mostrar
     */
    private void mostrarMensajeError(String mensaje) {
        if (lblMensaje != null) {
            lblMensaje.setText(mensaje);
            lblMensaje.setStyle("-fx-text-fill: red;");
            lblMensaje.setVisible(true);
        }
    }
    
    /**
     * Muestra un mensaje de éxito en la interfaz
     * @param mensaje El mensaje a mostrar
     */
    private void mostrarMensajeExito(String mensaje) {
        if (lblMensaje != null) {
            lblMensaje.setText(mensaje);
            lblMensaje.setStyle("-fx-text-fill: green;");
            lblMensaje.setVisible(true);
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
            mostrarMensajeError("Error al volver a la pantalla de perfil");
        }
    }
}