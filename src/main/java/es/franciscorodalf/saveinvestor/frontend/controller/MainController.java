package es.franciscorodalf.saveinvestor.frontend.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import es.franciscorodalf.saveinvestor.backend.dao.EstadisticaDAO;
import es.franciscorodalf.saveinvestor.backend.dao.ObjetivoDAO;
import es.franciscorodalf.saveinvestor.backend.dao.TareaDAO;
import es.franciscorodalf.saveinvestor.backend.model.Movimiento;
import es.franciscorodalf.saveinvestor.backend.model.Objetivo;
import es.franciscorodalf.saveinvestor.backend.model.Usuario;
import es.franciscorodalf.saveinvestor.backend.model.estadistica;
import es.franciscorodalf.saveinvestor.backend.model.tarea;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

public class MainController {

    @FXML
    private Button btnEstadisticas;
    
    @FXML
    private Button btnObjetivos;
    
    @FXML
    private Button btnGasto;
    
    @FXML
    private Button btnAhorro;

    @FXML
    private Button btnCerrarSesion;
    
    @FXML
    private Label lblTotalAhorro;
    
    @FXML
    private ListView<Movimiento> listMovimientos;

    @FXML
    private ListView<Objetivo> listObjetivos;
    
    private Usuario usuarioActual;
    private TareaDAO tareaDAO;
    private EstadisticaDAO estadisticaDAO;
    private ObjetivoDAO objetivoDAO;
    
    // Número de movimientos a mostrar en la lista
    private static final int LIMITE_MOVIMIENTOS = 10;

    @FXML
    private void initialize() {
        // Inicializar DAOs
        tareaDAO = new TareaDAO();
        estadisticaDAO = new EstadisticaDAO();
        objetivoDAO = new ObjetivoDAO();
        
        // Configurar la celda personalizada para la lista de movimientos
        configurarCeldaPersonalizada();
        
        // Configurar la celda personalizada para la lista de objetivos
        configurarCeldaObjetivos();
    }
    
    /**
     * Configura cómo se muestran los movimientos en la lista
     */
    private void configurarCeldaPersonalizada() {
        listMovimientos.setCellFactory(param -> new ListCell<Movimiento>() {
            @Override
            protected void updateItem(Movimiento item, boolean empty) {
                super.updateItem(item, empty);
                
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }
                
                String colorStyle = item.getTipo() == Movimiento.TipoMovimiento.INGRESO ? 
                        "-fx-text-fill: green;" : "-fx-text-fill: red;";
                
                // Formato: "Concepto: $Cantidad (Fecha)"
                String formattedText = String.format("%s: $%.2f (%s)", 
                        item.getConcepto(), 
                        item.getCantidad(),
                        item.getFecha().toString());
                
                setText(formattedText);
                setStyle(colorStyle);
            }
        });
    }

    /**
     * Configura cómo se muestran los objetivos en la lista
     */
    private void configurarCeldaObjetivos() {
        listObjetivos.setCellFactory(param -> new javafx.scene.control.ListCell<Objetivo>() {
            private javafx.scene.control.ProgressBar progressBar = new javafx.scene.control.ProgressBar();
            private javafx.scene.control.Label lblDescripcion = new javafx.scene.control.Label();
            private javafx.scene.control.Label lblDetalle = new javafx.scene.control.Label();
            private javafx.scene.layout.VBox vbox = new javafx.scene.layout.VBox(5);
            
            {
                progressBar.setPrefWidth(200);
                lblDescripcion.setStyle("-fx-font-weight: bold;");
                lblDetalle.setStyle("-fx-font-size: 10px;");
                vbox.getChildren().addAll(lblDescripcion, progressBar, lblDetalle);
                vbox.setPadding(new javafx.geometry.Insets(5, 0, 5, 0));
            }
            
            @Override
            protected void updateItem(Objetivo item, boolean empty) {
                super.updateItem(item, empty);
                
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }
                
                // Configurar descripción
                lblDescripcion.setText(item.getDescripcion());
                
                // Configurar barra de progreso
                double progreso = item.calcularPorcentaje() / 100.0;
                progressBar.setProgress(progreso);
                
                // Configurar color de la barra según el estado
                String barColor;
                if (item.isCompletado()) {
                    barColor = "-fx-accent: green;";
                } else if (item.estaVencido()) {
                    barColor = "-fx-accent: red;";
                } else {
                    barColor = "-fx-accent: #27ae60;";
                }
                progressBar.setStyle(barColor);
                
                // Configurar detalle
                StringBuilder detalle = new StringBuilder();
                detalle.append(String.format("%.2f$ de %.2f$ (%.1f%%)", 
                        item.getCantidadActual(), 
                        item.getCantidadObjetivo(),
                        item.calcularPorcentaje()));
                
                // Añadir info de fecha si existe
                if (item.getFechaObjetivo() != null) {
                    long diasRestantes = item.calcularDiasRestantes();
                    if (diasRestantes >= 0) {
                        detalle.append(" - ").append(diasRestantes).append(" días restantes");
                    } else {
                        detalle.append(" - Vencido");
                    }
                }
                
                lblDetalle.setText(detalle.toString());
                
                setGraphic(vbox);
            }
        });
    }

    /**
     * Actualiza la interfaz con los datos del usuario actual
     */
    private void actualizarInterfaz() {
        if (usuarioActual == null || usuarioActual.getId() == null) {
            return;
        }
        
        try {
            // Cargar estadísticas
            actualizarEstadisticas();
            
            // Cargar objetivos activos
            cargarObjetivos();
            
            // Cargar últimos movimientos
            cargarUltimosMovimientos();
            
        } catch (Exception e) {
            System.err.println("Error al cargar datos del usuario: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Actualiza las estadísticas mostradas en pantalla
     */
    private void actualizarEstadisticas() throws SQLException {
        estadistica stats = estadisticaDAO.obtenerPorUsuario(usuarioActual.getId());
        
        if (stats != null) {
            // Calcular balance (ingresos - gastos)
            double balance = stats.getTotalIngreso() - stats.getTotalGasto();
            lblTotalAhorro.setText(String.format("%.2f$", balance));
        } else {
            lblTotalAhorro.setText("0.00$");
        }
    }
    
    /**
     * Carga los últimos movimientos del usuario
     */
    private void cargarUltimosMovimientos() throws SQLException {
        try {
            List<tarea> tareas = tareaDAO.obtenerUltimasTareas(usuarioActual.getId(), LIMITE_MOVIMIENTOS);
            ObservableList<Movimiento> movimientos = FXCollections.observableArrayList();
            
            for (tarea t : tareas) {
                try {
                    Movimiento m = Movimiento.fromTarea(t);
                    movimientos.add(m);
                } catch (Exception e) {
                    System.err.println("Error al convertir tarea a movimiento: " + e.getMessage());
                    // Continuar con la siguiente tarea
                }
            }
            
            listMovimientos.setItems(movimientos);
        } catch (Exception e) {
            System.err.println("Error al cargar movimientos: " + e.getMessage());
            throw e; // Re-lanzar para manejo externo
        }
    }

    /**
     * Carga los objetivos activos del usuario
     */
    private void cargarObjetivos() {
        try {
            List<Objetivo> objetivos = objetivoDAO.obtenerObjetivosActivos(usuarioActual.getId());
            listObjetivos.setItems(FXCollections.observableArrayList(objetivos));
        } catch (SQLException e) {
            System.err.println("Error al cargar objetivos: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void onPerfil(ActionEvent event) {
        cambiarEscena(event, "/es/franciscorodalf/saveinvestor/perfil.fxml");
    }
    
    @FXML
    private void onEstadisticas(ActionEvent event) {
        cambiarEscena(event, "/es/franciscorodalf/saveinvestor/estadisticas.fxml");
    }
    
    @FXML
    private void onObjetivos(ActionEvent event) {
        cambiarEscena(event, "/es/franciscorodalf/saveinvestor/objetivos.fxml");
    }
    
    @FXML
    private void onRegistrarGasto(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/es/franciscorodalf/saveinvestor/registrarGasto.fxml"));
            Parent root = loader.load();
            
            // Pasar el usuario al controlador de registro de gasto
            RegistrarGastoController controller = loader.getController();
            controller.setUsuario(usuarioActual);
            
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            System.err.println("Error al cargar vista de registro de gasto: " + e.getMessage());
        }
    }
    
    @FXML
    private void onRegistrarAhorro(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/es/franciscorodalf/saveinvestor/registrarAhorro.fxml"));
            Parent root = loader.load();
            
            // Pasar el usuario al controlador de registro de ahorro
            RegistrarAhorroController controller = loader.getController();
            controller.setUsuario(usuarioActual);
            
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            System.err.println("Error al cargar vista de registro de ahorro: " + e.getMessage());
        }
    }

    @FXML
    private void onCerrarSesion(ActionEvent event) {
        try {
            // Simplemente carga la vista de login
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/es/franciscorodalf/saveinvestor/login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
            
            // Limpiar la referencia al usuario actual
            this.usuarioActual = null;
        } catch (IOException e) {
            System.err.println("Error al volver a la pantalla de login: " + e.getMessage());
        }
    }

    @FXML
    private void onHistorial(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/es/franciscorodalf/saveinvestor/historial.fxml"));
            Parent root = loader.load();
            
            // Pasar el usuario al controlador
            HistorialController controller = loader.getController();
            controller.setUsuario(usuarioActual);
            
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            System.err.println("Error al cargar vista de historial: " + e.getMessage());
        }
    }

    public void setUsuario(Usuario usuario) {
        this.usuarioActual = usuario;
        
        // Actualizar interfaz con los datos del usuario
        if (usuario != null) {
            actualizarInterfaz();
        }
    }

    private void cambiarEscena(ActionEvent event, String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            
            // Obtener el controlador y pasar el usuario si es necesario
            Object controller = loader.getController();
            
            // Comprobar si el controlador implementa la capacidad de recibir un usuario
            if (controller != null && usuarioActual != null) {
                if (controller instanceof PerfilController) {
                    ((PerfilController) controller).setUsuario(usuarioActual);
                } else if (controller instanceof EditarPerfilController) {
                    ((EditarPerfilController) controller).setUsuario(usuarioActual);
                } else if (controller instanceof ObjetivosController) {
                    ((ObjetivosController) controller).setUsuario(usuarioActual);
                } else if (controller instanceof EstadisticasController) {
                    ((EstadisticasController) controller).setUsuario(usuarioActual);
                }
            }
            
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            System.err.println("Error al cargar vista: " + e.getMessage());
        }
    }
}
