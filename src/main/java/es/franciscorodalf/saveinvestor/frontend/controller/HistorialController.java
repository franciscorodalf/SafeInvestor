package es.franciscorodalf.saveinvestor.frontend.controller;

import es.franciscorodalf.saveinvestor.backend.dao.EstadisticaDAO;
import es.franciscorodalf.saveinvestor.backend.dao.TareaDAO;
import es.franciscorodalf.saveinvestor.backend.model.Movimiento;
import es.franciscorodalf.saveinvestor.backend.model.Usuario;
import es.franciscorodalf.saveinvestor.backend.model.estadistica;
import es.franciscorodalf.saveinvestor.backend.model.tarea;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class HistorialController {

    @FXML
    private TableView<Movimiento> tablaHistorial;
    @FXML
    private TableColumn<Movimiento, LocalDate> colFecha;
    @FXML
    private TableColumn<Movimiento, String> colTipo;
    @FXML
    private TableColumn<Movimiento, String> colConcepto;
    @FXML
    private TableColumn<Movimiento, Double> colCantidad;
    @FXML
    private Label lblError;

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

        colTipo.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTipo().toString()));

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

        // Configurar evento de doble clic en la tabla
        tablaHistorial.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Movimiento movimientoSeleccionado = tablaHistorial.getSelectionModel().getSelectedItem();
                if (movimientoSeleccionado != null) {
                    mostrarOpcionesMovimiento(movimientoSeleccionado);
                }
            }
        });
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

    /**
     * Muestra un diálogo con opciones para editar o eliminar un movimiento
     */
    private void mostrarOpcionesMovimiento(Movimiento movimiento) {
        try {
            // Buscar la tarea asociada al movimiento seleccionado
            Integer tareaId = movimiento.getTareaId();
            tarea tareaSeleccionada = null;

            if (tareaId != null) {
                // Si tenemos el ID directamente, obtener la tarea
                tareaSeleccionada = tareaDAO.obtenerPorId(tareaId);
            } else {
                // Si no tenemos ID, buscar por criterios
                List<tarea> tareas = tareaDAO.obtenerPorUsuario(usuarioActual.getId());
                for (tarea t : tareas) {
                    // Comparar atributos para encontrar la tarea correcta
                    if (t.getConcepto().equals(movimiento.getConcepto()) &&
                            Math.abs(t.getCantidad() - movimiento.getCantidad()) < 0.01 &&
                            ((t.getEstado().equals(tarea.ESTADO_INGRESO)
                                    && movimiento.getTipo() == Movimiento.TipoMovimiento.INGRESO) ||
                                    (t.getEstado().equals(tarea.ESTADO_GASTO)
                                            && movimiento.getTipo() == Movimiento.TipoMovimiento.GASTO))) {
                        tareaSeleccionada = t;
                        break;
                    }
                }
            }

            if (tareaSeleccionada == null) {
                mostrarError("No se pudo encontrar el movimiento en la base de datos.");
                return;
            }

            // Crear el diálogo de opciones
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Opciones de movimiento");
            alert.setHeaderText("¿Qué desea hacer con este movimiento?");
            alert.setContentText("Concepto: " + movimiento.getConcepto() + "\nCantidad: " + movimiento.getCantidad());

            ButtonType btnEditar = new ButtonType("Editar");
            ButtonType btnEliminar = new ButtonType("Eliminar");
            ButtonType btnCancelar = new ButtonType("Cancelar");

            alert.getButtonTypes().setAll(btnEditar, btnEliminar, btnCancelar);

            Optional<ButtonType> resultado = alert.showAndWait();
            if (resultado.isPresent()) {
                if (resultado.get() == btnEditar) {
                    abrirEditorMovimiento(tareaSeleccionada);
                } else if (resultado.get() == btnEliminar) {
                    confirmarEliminarMovimiento(tareaSeleccionada);
                }
            }
        } catch (SQLException e) {
            mostrarError("Error al acceder a la base de datos: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Abre el editor de movimientos con los datos existentes
     */
    private void abrirEditorMovimiento(tarea tareaParaEditar) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/es/franciscorodalf/saveinvestor/editarMovimiento.fxml"));
            Parent root = loader.load();

            EditarMovimientoController controller = loader.getController();
            controller.inicializar(tareaParaEditar, usuarioActual, () -> cargarMovimientos());

            Stage stage = new Stage();
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(tablaHistorial.getScene().getWindow());
            stage.setScene(new Scene(root));
            stage.setTitle("Editar Movimiento");
            stage.showAndWait();

        } catch (IOException e) {
            mostrarError("Error al abrir editor: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Confirma y procesa la eliminación de un movimiento
     */
    private void confirmarEliminarMovimiento(tarea tareaParaEliminar) {
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar eliminación");
        confirmacion.setHeaderText("¿Está seguro de eliminar este movimiento?");
        confirmacion.setContentText("Esta acción no se puede deshacer");

        Optional<ButtonType> resultado = confirmacion.showAndWait();
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            try {
                // Actualizar estadísticas antes de eliminar
                EstadisticaDAO estadisticaDAO = new EstadisticaDAO();
                estadistica stats = estadisticaDAO.obtenerPorUsuario(usuarioActual.getId());
                if (stats != null) {
                    if (tareaParaEliminar.getEstado().equals(tarea.ESTADO_INGRESO)) {
                        stats.setTotalIngreso(stats.getTotalIngreso() - tareaParaEliminar.getCantidad());
                    } else if (tareaParaEliminar.getEstado().equals(tarea.ESTADO_GASTO)) {
                        stats.setTotalGasto(stats.getTotalGasto() - tareaParaEliminar.getCantidad());
                    }
                    estadisticaDAO.actualizar(stats);
                }

                // Eliminar la tarea
                tareaDAO.eliminar(tareaParaEliminar.getId());

                // Recargar los movimientos
                cargarMovimientos();

                // Mostrar confirmación
                Alert alerta = new Alert(Alert.AlertType.INFORMATION);
                alerta.setTitle("Información");
                alerta.setHeaderText(null);
                alerta.setContentText("Movimiento eliminado correctamente");
                alerta.showAndWait();

            } catch (SQLException e) {
                mostrarError("Error al eliminar movimiento: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}