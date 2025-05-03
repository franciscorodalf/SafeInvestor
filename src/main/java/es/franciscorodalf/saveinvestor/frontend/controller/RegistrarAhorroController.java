package es.franciscorodalf.saveinvestor.frontend.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import es.franciscorodalf.saveinvestor.backend.dao.EstadisticaDAO;
import es.franciscorodalf.saveinvestor.backend.dao.ObjetivoDAO;
import es.franciscorodalf.saveinvestor.backend.dao.TareaDAO;
import es.franciscorodalf.saveinvestor.backend.model.Usuario;
import es.franciscorodalf.saveinvestor.backend.model.estadistica;
import es.franciscorodalf.saveinvestor.backend.model.tarea;
import es.franciscorodalf.saveinvestor.backend.model.Objetivo;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class RegistrarAhorroController {

    @FXML
    private TextField txtCantidad;
    
    @FXML
    private TextField txtConcepto;
    
    @FXML
    private Label lblMensaje;
    
    @FXML
    private CheckBox chkAsignarObjetivo;
    
    @FXML
    private ComboBox<Objetivo> comboObjetivos;
    
    @FXML
    private VBox vboxObjetivo;
    
    @FXML
    private Label lblNoObjetivos;

    private Usuario usuarioActual;
    private TareaDAO tareaDAO;
    private EstadisticaDAO estadisticaDAO;
    private ObjetivoDAO objetivoDAO;
    private boolean tieneObjetivos = false;

    @FXML
    public void initialize() {
        try {
            tareaDAO = new TareaDAO();
            estadisticaDAO = new EstadisticaDAO();
            objetivoDAO = new ObjetivoDAO();
            
            if (lblMensaje != null) {
                lblMensaje.setVisible(false);
            }
            
            // Si el vboxObjetivo existe, inicialmente lo ocultamos
            if (vboxObjetivo != null) {
                vboxObjetivo.setVisible(false);
            }
            
            // Configurar el formato de visualización para el ComboBox si existe
            if (comboObjetivos != null) {
                configurarComboBox();
            }
        } catch (Exception e) {
            System.err.println("Error en initialize de RegistrarAhorroController: " + e.getMessage());
            e.printStackTrace();
            // Mostrar el mensaje en la interfaz si es posible
            if (lblMensaje != null) {
                lblMensaje.setText("Error al inicializar: " + e.getMessage());
                lblMensaje.setVisible(true);
            }
        }
    }
    
    /**
     * Configura el formato de visualización del ComboBox de objetivos
     */
    private void configurarComboBox() {
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

    @FXML
    private void onVolver(ActionEvent event) {
        volverAMain(event);
    }
    
    @FXML
    private void onCheckAsignarObjetivo(ActionEvent event) {
        if (vboxObjetivo != null) {
            boolean seleccionado = chkAsignarObjetivo != null && chkAsignarObjetivo.isSelected();
            vboxObjetivo.setVisible(seleccionado);
            
            // Si se marca el checkbox, verificar que haya objetivos disponibles
            if (seleccionado && !tieneObjetivos) {
                if (lblNoObjetivos != null) {
                    lblNoObjetivos.setVisible(true);
                }
                if (comboObjetivos != null) {
                    comboObjetivos.setVisible(false);
                }
            } else {
                if (lblNoObjetivos != null) {
                    lblNoObjetivos.setVisible(false);
                }
                if (comboObjetivos != null) {
                    comboObjetivos.setVisible(true);
                }
            }
        }
    }

    @FXML
    private void onAceptar(ActionEvent event) {
        try {
            if (!validarEntrada()) {
                return;
            }
            
            double cantidad = Double.parseDouble(txtCantidad.getText().trim());
            
            // Guardar el ahorro en la base de datos
            tarea nuevoAhorro = guardarAhorro();
            
            // Verificar si se debe asignar a un objetivo
            if (chkAsignarObjetivo != null && chkAsignarObjetivo.isSelected() && tieneObjetivos) {
                Objetivo objetivoSeleccionado = comboObjetivos.getValue();
                if (objetivoSeleccionado != null) {
                    asignarAObjetivo(objetivoSeleccionado, cantidad);
                }
            }
            
            // Mostrar mensaje de éxito
            mostrarMensajePositivo("Ahorro registrado correctamente");
            
            // Esperar un momento para que el usuario lea el mensaje
            new java.util.Timer().schedule(
                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        javafx.application.Platform.runLater(() -> volverAMain(event));
                    }
                }, 
                1500 // esperar 1.5 segundos antes de volver a la pantalla principal
            );
            
        } catch (NumberFormatException e) {
            mostrarError("Error en el formato de la cantidad: " + e.getMessage());
            System.err.println("Error de formato: " + e.getMessage());
            e.printStackTrace();
        } catch (SQLException e) {
            mostrarError("Error en la base de datos: " + e.getMessage());
            System.err.println("Error SQL: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            mostrarError("Error al registrar ahorro: " + e.getMessage());
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Asigna el ahorro a un objetivo seleccionado
     */
    private void asignarAObjetivo(Objetivo objetivo, double cantidad) throws SQLException {
        try {
            // Añadir la contribución al objetivo
            double cantidadAnterior = objetivo.getCantidadActual();
            objetivo.setCantidadActual(cantidadAnterior + cantidad);
            
            // Verificar si se completó el objetivo
            boolean completado = objetivo.verificarCompletado();
            objetivo.setCompletado(completado);
            
            // Guardar los cambios en la base de datos
            objetivoDAO.actualizar(objetivo);
            
            if (completado) {
                mostrarMensajePositivo("¡Felicidades! Objetivo completado");
            }
        } catch (Exception e) {
            throw new SQLException("Error al asignar ahorro a objetivo: " + e.getMessage());
        }
    }
    
    /**
     * Valida los campos del formulario
     * @return true si todos los campos son válidos
     */
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
        
        // Verificar null antes de usar el componente para evitar NullPointerException
        if (chkAsignarObjetivo != null && chkAsignarObjetivo.isSelected() && tieneObjetivos && comboObjetivos.getValue() == null) {
            mostrarError("Debe seleccionar un objetivo");
            return false;
        }
        
        return true;
    }
    
    private tarea guardarAhorro() throws SQLException {
        if (usuarioActual == null || usuarioActual.getId() == null) {
            mostrarError("Usuario no válido");
            throw new SQLException("Usuario no válido");
        }
        
        double cantidad = Double.parseDouble(txtCantidad.getText().trim());
        String concepto = txtConcepto.getText().trim();
        
        // 1. Crear y guardar la tarea de ahorro
        tarea nuevoAhorro = new tarea(
            concepto,
            cantidad,
            new Date(),
            tarea.ESTADO_INGRESO,
            usuarioActual.getId()
        );
        
        // Guardar en la base de datos SIN usar los triggers
        // Usamos un método especial que solo inserta sin activar triggers
        tareaDAO.insertarSinTrigger(nuevoAhorro);
         
        // 2. Actualizar estadísticas del usuario MANUALMENTE
        actualizarEstadisticas(cantidad);
        
        return nuevoAhorro;
    }
    
    /**
     * Actualiza las estadísticas del usuario añadiendo el nuevo ingreso
     */
    private void actualizarEstadisticas(double cantidad) throws SQLException {
        // Obtener estadísticas actuales del usuario
        estadistica stats = estadisticaDAO.obtenerPorUsuario(usuarioActual.getId());
        
        if (stats != null) {
            // Actualizar el total de ingresos
            double nuevoTotal = stats.getTotalIngreso() + cantidad;
            stats.setTotalIngreso(nuevoTotal);
            estadisticaDAO.actualizar(stats);
        } else {
            // Crear nueva estadística si no existe
            estadistica nuevaEstadistica = new estadistica(cantidad, 0.0, usuarioActual.getId());
            estadisticaDAO.insertar(nuevaEstadistica);
        }
    }
    
    private void mostrarError(String mensaje) {
        if (lblMensaje != null) {
            lblMensaje.setText(mensaje);
            lblMensaje.setStyle("-fx-text-fill: red;");
            lblMensaje.setVisible(true);
        } else {
            System.err.println(mensaje);
        }
    }
    
    private void mostrarMensajePositivo(String mensaje) {
        if (lblMensaje != null) {
            lblMensaje.setText(mensaje);
            lblMensaje.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
            lblMensaje.setVisible(true);
        } else {
            System.out.println(mensaje);
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
        
        // Cargar objetivos disponibles
        cargarObjetivos();
    }
    
    /**
     * Carga los objetivos disponibles en el ComboBox
     */
    private void cargarObjetivos() {
        try {
            if (usuarioActual != null && usuarioActual.getId() != null && comboObjetivos != null) {
                List<Objetivo> objetivos = objetivoDAO.obtenerObjetivosActivos(usuarioActual.getId());
                
                if (objetivos == null || objetivos.isEmpty()) {
                    tieneObjetivos = false;
                    
                    // Si el checkbox está seleccionado, mostrar mensaje de no objetivos
                    if (chkAsignarObjetivo != null && chkAsignarObjetivo.isSelected()) {
                        lblNoObjetivos.setVisible(true);
                        comboObjetivos.setVisible(false);
                    }
                } else {
                    tieneObjetivos = true;
                    comboObjetivos.setItems(FXCollections.observableArrayList(objetivos));
                    comboObjetivos.getSelectionModel().selectFirst();
                    lblNoObjetivos.setVisible(false);
                    comboObjetivos.setVisible(true);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al cargar objetivos: " + e.getMessage());
            e.printStackTrace();
            tieneObjetivos = false;
        }
    }
}