package es.franciscorodalf.saveinvestor.frontend.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import es.franciscorodalf.saveinvestor.backend.dao.EstadisticaDAO;
import es.franciscorodalf.saveinvestor.backend.dao.ObjetivoDAO;
import es.franciscorodalf.saveinvestor.backend.dao.TareaDAO;
import es.franciscorodalf.saveinvestor.backend.model.Movimiento;
import es.franciscorodalf.saveinvestor.backend.model.Objetivo;
import es.franciscorodalf.saveinvestor.backend.model.Usuario;
import es.franciscorodalf.saveinvestor.backend.model.estadistica;
import es.franciscorodalf.saveinvestor.backend.model.tarea;
import es.franciscorodalf.saveinvestor.util.AppConstants;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Alert;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainController implements UsuarioAware, DashboardNavigable {

    private static final Logger logger = LoggerFactory.getLogger(MainController.class);

    @FXML
    private Button btnGasto;

    @FXML
    private Button btnAhorro;

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
    private DashboardController dashboardController;

    // Número de movimientos a mostrar en la lista
    private static final int LIMITE_MOVIMIENTOS = 10;

    @FXML
    private void initialize() {
        // Inicializar DAOs usando Factory (o directamente si no están en el factory
        // aún, pero idealmente sí)
        // Por ahora asumimos que están disponibles o los instanciamos aquí si el
        // factory no los tiene todos
        // El ServiceFactory actual solo tiene UsuarioDAO y MovimientoInversionDAO en el
        // snippet anterior.
        // Deberíamos añadir los otros al Factory, pero para no romper, los instanciamos
        // aquí o actualizamos Factory.
        // Para ser consistentes con el plan, usaremos instanciación directa si no están
        // en Factory,
        // pero lo ideal es actualizar Factory.
        // Dado que no puedo editar Factory y MainController a la vez fácilmente sin
        // múltiples pasos,
        // usaré new por ahora para los que faltan, pero UsuarioDAO sí desde Factory si
        // lo necesitara.

        tareaDAO = new TareaDAO();
        estadisticaDAO = new EstadisticaDAO();
        objetivoDAO = new ObjetivoDAO();

        // Configurar la celda personalizada para la lista de movimientos
        configurarCeldaPersonalizada();

        // Configurar la celda personalizada para la lista de objetivos
        configurarCeldaObjetivos();

        // Configurar manejo de clics en la lista de movimientos
        if (listMovimientos != null) {
            listMovimientos.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2) { // Verificar doble clic
                    Movimiento movimientoSeleccionado = listMovimientos.getSelectionModel().getSelectedItem();
                    if (movimientoSeleccionado != null) {
                        mostrarOpcionesMovimiento(movimientoSeleccionado);
                    }
                }
            });
        }
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

                String colorStyle = item.getTipo() == Movimiento.TipoMovimiento.INGRESO
                        ? "-fx-text-fill: -fx-secondary-color;"
                        : "-fx-text-fill: -fx-error-color;";

                // Formato: "Concepto: $Cantidad (Fecha)"
                String formattedText = String.format("%s: $%.2f (%s)",
                        item.getConcepto(),
                        item.getCantidad(),
                        item.getFecha().toString());

                setText(formattedText);
                setStyle(colorStyle + "-fx-font-size: 14px;");
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
                lblDescripcion.setStyle("-fx-font-weight: bold; -fx-text-fill: -fx-text-color;");
                lblDetalle.setStyle("-fx-font-size: 11px; -fx-text-fill: -fx-text-color-light;");
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
                    barColor = "-fx-accent: -fx-secondary-color;";
                } else if (item.estaVencido()) {
                    barColor = "-fx-accent: -fx-error-color;";
                } else {
                    barColor = "-fx-accent: -fx-primary-color;";
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
            logger.error("Error al cargar datos del usuario", e);
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
                    logger.warn("Error al convertir tarea a movimiento: {}", e.getMessage());
                    // Continuar con la siguiente tarea
                }
            }

            listMovimientos.setItems(movimientos);
        } catch (Exception e) {
            logger.error("Error al cargar movimientos", e);
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
            logger.error("Error al cargar objetivos", e);
        }
    }

    @FXML
    private void onRegistrarGasto(ActionEvent event) {
        if (dashboardController != null) {
            dashboardController.navegarA(AppConstants.FXML_REGISTRAR_GASTO);
        } else {
            // Fallback legacy
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(AppConstants.FXML_REGISTRAR_GASTO));
                Parent root = loader.load();

                RegistrarGastoController controller = loader.getController();
                controller.setUsuario(usuarioActual);

                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.show();
            } catch (IOException e) {
                logger.error("Error al cargar vista de registro de gasto", e);
            }
        }
    }

    @FXML
    private void onRegistrarAhorro(ActionEvent event) {
        if (dashboardController != null) {
            dashboardController.navegarA(AppConstants.FXML_REGISTRAR_AHORRO);
        } else {
            // Fallback legacy
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(AppConstants.FXML_REGISTRAR_AHORRO));
                Parent root = loader.load();

                RegistrarAhorroController controller = loader.getController();
                controller.setUsuario(usuarioActual);

                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.show();
            } catch (IOException e) {
                logger.error("Error al cargar vista de registro de ahorro", e);
            }
        }
    }

    @Override
    public void setUsuario(Usuario usuario) {
        this.usuarioActual = usuario;

        // Actualizar interfaz con los datos del usuario
        if (usuario != null) {
            actualizarInterfaz();
        }
    }

    @Override
    public void setDashboardController(DashboardController dashboardController) {
        this.dashboardController = dashboardController;
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
                List<tarea> tareas = tareaDAO.obtenerUltimasTareas(usuarioActual.getId(), 100);
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
                mostrarAlerta("No se pudo encontrar el movimiento en la base de datos.", Alert.AlertType.WARNING);
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
            mostrarAlerta("Error al acceder a la base de datos: " + e.getMessage(), Alert.AlertType.ERROR);
            logger.error("Error DB", e);
        }
    }

    /**
     * Abre el editor de movimientos con los datos existentes
     */
    private void abrirEditorMovimiento(tarea tareaParaEditar) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(AppConstants.FXML_EDITAR_MOVIMIENTO));
            Parent root = loader.load();

            EditarMovimientoController controller = loader.getController();
            controller.inicializar(tareaParaEditar, usuarioActual, () -> actualizarInterfaz());

            Stage stage = new Stage();
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(listMovimientos.getScene().getWindow());
            stage.setScene(new Scene(root));
            stage.setTitle("Editar Movimiento");
            stage.showAndWait();

        } catch (IOException e) {
            mostrarAlerta("Error al abrir editor: " + e.getMessage(), Alert.AlertType.ERROR);
            logger.error("Error al abrir editor", e);
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
                // Actualizar estadísticas manualmente antes de eliminar
                estadistica stats = estadisticaDAO.obtenerPorUsuario(usuarioActual.getId());
                if (stats != null) {
                    if (tareaParaEliminar.getEstado().equals(tarea.ESTADO_INGRESO)) {
                        stats.setTotalIngreso(stats.getTotalIngreso() - tareaParaEliminar.getCantidad());
                    } else if (tareaParaEliminar.getEstado().equals(tarea.ESTADO_GASTO)) {
                        stats.setTotalGasto(stats.getTotalGasto() - tareaParaEliminar.getCantidad());
                    }
                    estadisticaDAO.actualizar(stats);
                }

                // Eliminar la tarea sin activar triggers
                tareaDAO.eliminarSinTrigger(tareaParaEliminar.getId());

                // Actualizar la interfaz
                actualizarInterfaz();

                // Mostrar confirmación
                mostrarAlerta("Movimiento eliminado correctamente", Alert.AlertType.INFORMATION);

            } catch (SQLException e) {
                mostrarAlerta("Error al eliminar movimiento: " + e.getMessage(), Alert.AlertType.ERROR);
                logger.error("Error al eliminar movimiento", e);
            }
        }
    }

    /**
     * Muestra una alerta con el mensaje indicado
     */
    private void mostrarAlerta(String mensaje, Alert.AlertType tipo) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle("Información");
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}
