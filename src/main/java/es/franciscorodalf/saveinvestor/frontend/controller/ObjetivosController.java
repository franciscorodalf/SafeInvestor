package es.franciscorodalf.saveinvestor.frontend.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;

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
    private GridPane gridObjetivos;
    
    @FXML
    private Label lblSinObjetivos;

    @FXML
    private Button btnAgregarObjetivo;
    
    @FXML
    private Button btnActualizarObjetivo;
    
    @FXML
    private Button btnCancelarEdicion;

    @FXML
    private Button btnVolver;

    @FXML
    private DatePicker dateFechaObjetivo;
    
    private Usuario usuarioActual;
    private ObjetivoDAO objetivoDAO;
    private Objetivo objetivoEnEdicion;

    @FXML
    public void initialize() {
        // Inicializar componentes
        if (lblMensaje != null) {
            lblMensaje.setVisible(false);
        }

        objetivoDAO = new ObjetivoDAO();
        
        // Ocultar botones de edición inicialmente
        if (btnActualizarObjetivo != null) {
            btnActualizarObjetivo.setVisible(false);
        }
        
        if (btnCancelarEdicion != null) {
            btnCancelarEdicion.setVisible(false);
        }
        
        if (lblSinObjetivos != null) {
            lblSinObjetivos.setVisible(true);
        }
    }

    /**
     * Establece el usuario actual
     * 
     * @param usuario El usuario actual de la aplicación
     */
    public void setUsuario(Usuario usuario) {
        this.usuarioActual = usuario;
        // Cargar objetivos del usuario desde la base de datos
        cargarObjetivos();
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
                limpiarCampos();
                
                // Mostrar mensaje de éxito
                mostrarMensaje("Objetivo agregado correctamente", false);

            } catch (Exception e) {
                mostrarMensaje("Error al guardar objetivo: " + e.getMessage(), true);
                System.err.println("Error: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Maneja el evento de actualizar un objetivo existente
     * 
     * @param event El evento de acción
     */
    @FXML
    private void onActualizarObjetivo(ActionEvent event) {
        if (objetivoEnEdicion == null) {
            mostrarMensaje("No hay objetivo seleccionado para actualizar", true);
            return;
        }
        
        if (validarCampos()) {
            try {
                // Actualizar datos del objetivo
                objetivoEnEdicion.setDescripcion(txtDescripcion.getText().trim());
                objetivoEnEdicion.setCantidadObjetivo(Double.parseDouble(txtCantidad.getText().trim()));
                
                // Actualizar fecha si se proporcionó
                if (dateFechaObjetivo != null && dateFechaObjetivo.getValue() != null) {
                    LocalDate fechaLocal = dateFechaObjetivo.getValue();
                    Date fecha = Date.from(fechaLocal.atStartOfDay(ZoneId.systemDefault()).toInstant());
                    objetivoEnEdicion.setFechaObjetivo(fecha);
                } else {
                    objetivoEnEdicion.setFechaObjetivo(null);
                }
                
                // Actualizar en la base de datos
                objetivoDAO.actualizar(objetivoEnEdicion);
                
                // Cargar lista actualizada y limpiar campos
                cargarObjetivos();
                limpiarCampos();
                
                // Volver al modo de agregar
                cambiarAModoAgregar();
                
                // Mostrar mensaje de éxito
                mostrarMensaje("Objetivo actualizado correctamente", false);
                
            } catch (Exception e) {
                mostrarMensaje("Error al actualizar objetivo: " + e.getMessage(), true);
                System.err.println("Error: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Maneja el evento de cancelar la edición
     * 
     * @param event El evento de acción
     */
    @FXML
    private void onCancelarEdicion(ActionEvent event) {
        limpiarCampos();
        cambiarAModoAgregar();
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
            mostrarMensaje("La descripción no puede estar vacía", true);
            return false;
        }

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

        return true;
    }

    private void mostrarMensaje(String mensaje, boolean esError) {
        if (lblMensaje != null) {
            lblMensaje.setText(mensaje);
            lblMensaje.setVisible(true);
            lblMensaje.setStyle("-fx-text-fill: " + (esError ? "red" : "green") + ";");
        } else {
            System.err.println(mensaje);
        }
    }
    
    private void limpiarCampos() {
        if (txtDescripcion != null) {
            txtDescripcion.clear();
        }
        
        if (txtCantidad != null) {
            txtCantidad.clear();
        }
        
        if (dateFechaObjetivo != null) {
            dateFechaObjetivo.setValue(null);
        }
        
        if (lblMensaje != null) {
            lblMensaje.setVisible(false);
        }
        
        objetivoEnEdicion = null;
    }
    
    private void cambiarAModoEdicion(Objetivo objetivo) {
        if (objetivo == null) return;
        
        // Guardar referencia al objetivo en edición
        objetivoEnEdicion = objetivo;
        
        // Cambiar visibilidad de botones
        if (btnAgregarObjetivo != null) {
            btnAgregarObjetivo.setVisible(false);
        }
        
        if (btnActualizarObjetivo != null) {
            btnActualizarObjetivo.setVisible(true);
        }
        
        if (btnCancelarEdicion != null) {
            btnCancelarEdicion.setVisible(true);
        }
        
        // Cargar datos del objetivo en los campos
        if (txtDescripcion != null) {
            txtDescripcion.setText(objetivo.getDescripcion());
        }
        
        if (txtCantidad != null) {
            txtCantidad.setText(String.format("%.2f", objetivo.getCantidadObjetivo()));
        }
        
        if (dateFechaObjetivo != null && objetivo.getFechaObjetivo() != null) {
            LocalDate fecha = objetivo.getFechaObjetivo().toInstant()
                              .atZone(ZoneId.systemDefault())
                              .toLocalDate();
            dateFechaObjetivo.setValue(fecha);
        }
    }
    
    private void cambiarAModoAgregar() {
        // Cambiar visibilidad de botones
        if (btnAgregarObjetivo != null) {
            btnAgregarObjetivo.setVisible(true);
        }
        
        if (btnActualizarObjetivo != null) {
            btnActualizarObjetivo.setVisible(false);
        }
        
        if (btnCancelarEdicion != null) {
            btnCancelarEdicion.setVisible(false);
        }
        
        // Eliminar referencia al objetivo en edición
        objetivoEnEdicion = null;
    }

    /**
     * Carga la lista de objetivos del usuario actual
     */
    private void cargarObjetivos() {
        if (usuarioActual != null && gridObjetivos != null) {
            try {
                // Limpiar el grid
                gridObjetivos.getChildren().clear();
                
                // Obtener objetivos del usuario desde la base de datos
                List<Objetivo> listaObjetivos = objetivoDAO.obtenerPorUsuario(usuarioActual.getId());
                
                // Mostrar u ocultar el mensaje de "sin objetivos"
                if (lblSinObjetivos != null) {
                    lblSinObjetivos.setVisible(listaObjetivos.isEmpty());
                }
                
                // Si no hay objetivos, salir
                if (listaObjetivos.isEmpty()) {
                    return;
                }
                
                // Añadir encabezados al grid
                Label lblDescripcionHeader = new Label("Descripción");
                lblDescripcionHeader.setStyle("-fx-font-weight: bold;");
                gridObjetivos.add(lblDescripcionHeader, 0, 0);
                
                Label lblCantidadHeader = new Label("Progreso");
                lblCantidadHeader.setStyle("-fx-font-weight: bold;");
                gridObjetivos.add(lblCantidadHeader, 1, 0);
                
                Label lblAccionesHeader = new Label("Acciones");
                lblAccionesHeader.setStyle("-fx-font-weight: bold;");
                gridObjetivos.add(lblAccionesHeader, 2, 0);
                
                // Añadir cada objetivo al grid
                int fila = 1;
                for (Objetivo objetivo : listaObjetivos) {
                    // Columna 1: Descripción y detalles
                    VBox vboxInfo = new VBox(5);
                    Label lblDescripcion = new Label(objetivo.getDescripcion());
                    lblDescripcion.setStyle("-fx-font-weight: bold;");
                    
                    String detalles = "";
                    if (objetivo.getFechaObjetivo() != null) {
                        long diasRestantes = objetivo.calcularDiasRestantes();
                        if (diasRestantes >= 0) {
                            detalles += diasRestantes + " días restantes";
                        } else {
                            detalles += "Vencido";
                        }
                    }
                    
                    Label lblDetalles = new Label(detalles);
                    lblDetalles.setStyle("-fx-font-size: 11px; -fx-font-style: italic;");
                    
                    vboxInfo.getChildren().addAll(lblDescripcion);
                    if (!detalles.isEmpty()) {
                        vboxInfo.getChildren().add(lblDetalles);
                    }
                    
                    gridObjetivos.add(vboxInfo, 0, fila);
                    
                    // Columna 2: Progreso
                    VBox vboxProgreso = new VBox(5);
                    vboxProgreso.setAlignment(Pos.CENTER_LEFT);
                    
                    // Barra de progreso
                    javafx.scene.control.ProgressBar progressBar = new javafx.scene.control.ProgressBar();
                    progressBar.setPrefWidth(150);
                    progressBar.setProgress(objetivo.calcularPorcentaje() / 100.0);
                    
                    // Color de la barra según el estado
                    String barColor;
                    if (objetivo.isCompletado()) {
                        barColor = "-fx-accent: green;";
                    } else if (objetivo.estaVencido()) {
                        barColor = "-fx-accent: red;";
                    } else {
                        barColor = "-fx-accent: #27ae60;";
                    }
                    progressBar.setStyle(barColor);
                    
                    // Etiqueta con detalles del progreso
                    String progresoTexto = String.format("%.2f$ de %.2f$ (%.1f%%)",
                            objetivo.getCantidadActual(),
                            objetivo.getCantidadObjetivo(),
                            objetivo.calcularPorcentaje());
                    Label lblProgreso = new Label(progresoTexto);
                    lblProgreso.setStyle("-fx-font-size: 11px;");
                    
                    vboxProgreso.getChildren().addAll(progressBar, lblProgreso);
                    gridObjetivos.add(vboxProgreso, 1, fila);
                    
                    // Columna 3: Botones de acción
                    HBox hboxBotones = new HBox(5);
                    
                    Button btnEditar = new Button("Editar");
                    btnEditar.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
                    btnEditar.setOnAction(e -> cambiarAModoEdicion(objetivo));
                    
                    Button btnEliminar = new Button("Eliminar");
                    btnEliminar.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
                    btnEliminar.setOnAction(e -> confirmarEliminarObjetivo(objetivo));
                    
                    hboxBotones.getChildren().addAll(btnEditar, btnEliminar);
                    gridObjetivos.add(hboxBotones, 2, fila);
                    
                    // Incrementar fila
                    fila++;
                }
                
            } catch (SQLException e) {
                mostrarMensaje("Error al cargar objetivos: " + e.getMessage(), true);
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Muestra un diálogo de confirmación para eliminar un objetivo
     */
    private void confirmarEliminarObjetivo(Objetivo objetivo) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Confirmar eliminación");
        alert.setHeaderText("¿Está seguro que desea eliminar este objetivo?");
        alert.setContentText("Descripción: " + objetivo.getDescripcion());
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            eliminarObjetivo(objetivo);
        }
    }
    
    /**
     * Elimina un objetivo de la base de datos
     */
    private void eliminarObjetivo(Objetivo objetivo) {
        try {
            objetivoDAO.eliminar(objetivo.getId());
            cargarObjetivos();
            mostrarMensaje("Objetivo eliminado correctamente", false);
        } catch (SQLException e) {
            mostrarMensaje("Error al eliminar objetivo: " + e.getMessage(), true);
            e.printStackTrace();
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
