package es.franciscorodalf.saveinvestor.frontend.controller;

import es.franciscorodalf.saveinvestor.backend.dao.ObjetivoDAO;
import es.franciscorodalf.saveinvestor.backend.model.Objetivo;
import es.franciscorodalf.saveinvestor.backend.model.Usuario;
import es.franciscorodalf.saveinvestor.backend.service.PuntoProyeccion;
import es.franciscorodalf.saveinvestor.backend.service.SimulacionMetaResultado;
import es.franciscorodalf.saveinvestor.backend.service.SimuladorMetasService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Controlador encargado de la vista del simulador de metas financieras.
 */
public class SimuladorMetasController {

    @FXML
    private ComboBox<Objetivo> comboObjetivos;
    @FXML
    private TextField txtMetaObjetivo;
    @FXML
    private TextField txtMontoInicial;
    @FXML
    private TextField txtAportacionPeriodica;
    @FXML
    private TextField txtTasaInteres;
    @FXML
    private Spinner<Integer> spinnerHorizonte;
    @FXML
    private TextField txtDescripcionEscenario;
    @FXML
    private LineChart<Number, Number> chartProyeccion;
    @FXML
    private NumberAxis axisMeses;
    @FXML
    private NumberAxis axisSaldo;
    @FXML
    private ProgressBar progressComparativa;
    @FXML
    private Label lblResumen;
    @FXML
    private Label lblComparativa;
    @FXML
    private Label lblEstado;
    @FXML
    private Button btnGuardarEscenario;
    @FXML
    private TextArea txtDetalles;

    private final SimuladorMetasService simuladorMetasService = new SimuladorMetasService();
    private final ObjetivoDAO objetivoDAO = new ObjetivoDAO();

    private Usuario usuarioActual;
    private Objetivo objetivoSeleccionado;
    private SimulacionMetaResultado resultadoActual;
    private String rutaRetorno;

    @FXML
    private void initialize() {
        if (spinnerHorizonte != null) {
            spinnerHorizonte.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 600, 60));
        }

        if (comboObjetivos != null) {
            comboObjetivos.setButtonCell(new ObjetivoListCell());
            comboObjetivos.setCellFactory(listView -> new ObjetivoListCell());
        }

        if (chartProyeccion != null) {
            chartProyeccion.setAnimated(false);
        }

        if (progressComparativa != null) {
            progressComparativa.setProgress(0);
        }
    }

    public void setUsuario(Usuario usuario) {
        this.usuarioActual = usuario;
        cargarObjetivosDelUsuario();
    }

    public void setRutaRetorno(String rutaRetorno) {
        this.rutaRetorno = rutaRetorno;
    }

    @FXML
    private void onSeleccionarObjetivo(ActionEvent event) {
        objetivoSeleccionado = comboObjetivos.getValue();
        if (objetivoSeleccionado == null) {
            return;
        }

        txtMetaObjetivo.setText(String.format("%.2f", objetivoSeleccionado.getCantidadObjetivo()));
        txtMontoInicial.setText(String.format("%.2f", objetivoSeleccionado.getCantidadActual()));

        if (objetivoSeleccionado.getFechaObjetivo() != null) {
            long meses = calcularMesesRestantes(objetivoSeleccionado.getFechaObjetivo());
            spinnerHorizonte.getValueFactory().setValue((int) Math.max(1, meses));
        }

        txtDescripcionEscenario.setText(objetivoSeleccionado.getDescripcion() + " - Escenario");
        actualizarComparativaActual();
    }

    @FXML
    private void onSimular(ActionEvent event) {
        try {
            double montoInicial = parseDouble(txtMontoInicial.getText(), "monto inicial");
            double aportacion = parseDouble(txtAportacionPeriodica.getText(), "aportación periódica");
            double tasa = parseDouble(txtTasaInteres.getText(), "tasa de interés") / 100.0;
            int horizonte = Optional.ofNullable(spinnerHorizonte.getValue()).orElse(1);
            Double meta = txtMetaObjetivo.getText().isBlank() ? null : parseDouble(txtMetaObjetivo.getText(), "meta objetivo");

            resultadoActual = simuladorMetasService.simular(montoInicial, aportacion, tasa, horizonte, meta);
            actualizarGrafica(resultadoActual);
            actualizarResumen(resultadoActual);
            actualizarComparativaActual();
            lblEstado.setText("Simulación generada correctamente");
        } catch (NumberFormatException ex) {
            mostrarError("Formato inválido", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            mostrarError("Datos inválidos", ex.getMessage());
        }
    }

    @FXML
    private void onCalcularAportacion(ActionEvent event) {
        try {
            double montoInicial = parseDouble(txtMontoInicial.getText(), "monto inicial");
            double tasa = parseDouble(txtTasaInteres.getText(), "tasa de interés") / 100.0;
            int horizonte = Optional.ofNullable(spinnerHorizonte.getValue()).orElse(1);
            double meta = parseDouble(txtMetaObjetivo.getText(), "meta objetivo");

            double aportacion = simuladorMetasService.calcularAportacionNecesaria(montoInicial, tasa, horizonte, meta);
            txtAportacionPeriodica.setText(String.format("%.2f", aportacion));
            lblEstado.setText("Aportación sugerida actualizada");
        } catch (NumberFormatException ex) {
            mostrarError("Formato inválido", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            mostrarError("Datos inválidos", ex.getMessage());
        }
    }

    @FXML
    private void onGuardarEscenario(ActionEvent event) {
        if (usuarioActual == null) {
            mostrarError("Acción no disponible", "Debe iniciar sesión para guardar escenarios");
            return;
        }
        if (resultadoActual == null) {
            mostrarError("Sin simulación", "Primero debe generar una simulación para guardar");
            return;
        }

        String descripcion = txtDescripcionEscenario.getText() != null ? txtDescripcionEscenario.getText().trim() : "";
        if (descripcion.isEmpty()) {
            mostrarError("Descripción requerida", "Ingrese una descripción para el escenario");
            return;
        }

        try {
            double meta = parseDouble(txtMetaObjetivo.getText(), "meta objetivo");
            Objetivo nuevoObjetivo = new Objetivo(descripcion, meta, usuarioActual.getId());
            nuevoObjetivo.setCantidadActual(parseDouble(txtMontoInicial.getText(), "monto inicial"));

            if (spinnerHorizonte.getValue() != null) {
                LocalDate fechaObjetivo = LocalDate.now().plusMonths(spinnerHorizonte.getValue());
                Date fecha = Date.from(fechaObjetivo.atStartOfDay(ZoneId.systemDefault()).toInstant());
                nuevoObjetivo.setFechaObjetivo(fecha);
            }

            objetivoDAO.insertar(nuevoObjetivo);
            lblEstado.setText("Escenario guardado como objetivo");
            cargarObjetivosDelUsuario();
        } catch (NumberFormatException ex) {
            mostrarError("Formato inválido", ex.getMessage());
        } catch (SQLException ex) {
            mostrarError("Error de base de datos", "No se pudo guardar el escenario: " + ex.getMessage());
        }
    }

    @FXML
    private void onVolver(ActionEvent event) {
        String destino = rutaRetorno != null ? rutaRetorno : "/es/franciscorodalf/saveinvestor/main.fxml";
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(destino));
            Parent root = loader.load();

            Object controller = loader.getController();
            if (controller instanceof ObjetivosController objetivosController && usuarioActual != null) {
                objetivosController.setUsuario(usuarioActual);
            } else if (controller instanceof MainController mainController && usuarioActual != null) {
                mainController.setUsuario(usuarioActual);
            }

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            mostrarError("Error de navegación", "No se pudo abrir la vista solicitada");
        }
    }

    private void cargarObjetivosDelUsuario() {
        if (usuarioActual == null || usuarioActual.getId() == null || comboObjetivos == null) {
            return;
        }
        try {
            List<Objetivo> objetivos = objetivoDAO.obtenerPorUsuario(usuarioActual.getId());
            ObservableList<Objetivo> items = FXCollections.observableArrayList(objetivos);
            comboObjetivos.setItems(items);
        } catch (SQLException e) {
            mostrarError("Error al cargar objetivos", e.getMessage());
        }
    }

    private void actualizarGrafica(SimulacionMetaResultado resultado) {
        if (chartProyeccion == null) {
            return;
        }
        chartProyeccion.getData().clear();

        XYChart.Series<Number, Number> proyeccion = new XYChart.Series<>();
        proyeccion.setName("Proyección");
        for (PuntoProyeccion punto : resultado.getPuntos()) {
            proyeccion.getData().add(new XYChart.Data<>(punto.periodo(), punto.saldo()));
        }
        chartProyeccion.getData().add(proyeccion);

        if (objetivoSeleccionado != null) {
            XYChart.Series<Number, Number> actual = new XYChart.Series<>();
            actual.setName("Saldo actual");
            double saldoActual = objetivoSeleccionado.getCantidadActual();
            int horizonte = resultado.getPuntos().get(resultado.getPuntos().size() - 1).periodo();
            actual.getData().add(new XYChart.Data<>(0, saldoActual));
            actual.getData().add(new XYChart.Data<>(horizonte, saldoActual));
            chartProyeccion.getData().add(actual);
        }
    }

    private void actualizarResumen(SimulacionMetaResultado resultado) {
        if (resultado == null) {
            lblResumen.setText("");
            if (txtDetalles != null) {
                txtDetalles.clear();
            }
            return;
        }
        StringBuilder resumen = new StringBuilder();
        resumen.append(String.format("Saldo final proyectado: %.2f\n", resultado.getSaldoFinal()));
        resumen.append(String.format("Total aportado: %.2f\n", resultado.getTotalAportado()));
        resumen.append(String.format("Intereses generados: %.2f\n", resultado.getTotalIntereses()));
        if (resultado.getPeriodoAlcanceMeta() != null) {
            resumen.append(String.format("Meta alcanzada en %d meses.%n", resultado.getPeriodoAlcanceMeta()));
        } else {
            resumen.append("La meta no se alcanza en el horizonte simulado.\n");
        }
        lblResumen.setText(resumen.toString().strip());

        if (txtDetalles != null) {
            StringBuilder detalle = new StringBuilder();
            for (PuntoProyeccion punto : resultado.getPuntos()) {
                detalle.append(String.format("Mes %d - Saldo: %.2f | Aportado: %.2f | Intereses: %.2f%n",
                        punto.periodo(), punto.saldo(), punto.aporteAcumulado(), punto.interesAcumulado()));
            }
            txtDetalles.setText(detalle.toString());
        }
    }

    private void actualizarComparativaActual() {
        if (progressComparativa == null) {
            return;
        }
        double meta = 0;
        double actual = 0;

        if (objetivoSeleccionado != null) {
            meta = objetivoSeleccionado.getCantidadObjetivo();
            actual = objetivoSeleccionado.getCantidadActual();
        } else if (resultadoActual != null && resultadoActual.getMetaObjetivo() != null) {
            meta = resultadoActual.getMetaObjetivo();
            actual = resultadoActual.getPuntos().isEmpty() ? 0 : resultadoActual.getPuntos().get(0).saldo();
        }

        if (resultadoActual != null) {
            meta = resultadoActual.getMetaObjetivo() != null ? resultadoActual.getMetaObjetivo() : meta;
        }

        if (meta > 0) {
            double progreso = actual / meta;
            progressComparativa.setProgress(Math.min(1.0, progreso));
            lblComparativa.setText(String.format("Progreso actual: %.2f%% de %.2f", progreso * 100, meta));
        } else {
            progressComparativa.setProgress(0);
            lblComparativa.setText("Seleccione o defina una meta para comparar");
        }
    }

    private long calcularMesesRestantes(Date fechaObjetivo) {
        LocalDate hoy = LocalDate.now();
        LocalDate fecha = fechaObjetivo.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        long meses = ChronoUnit.MONTHS.between(hoy.withDayOfMonth(1), fecha.withDayOfMonth(1));
        return Math.max(1, meses);
    }

    private double parseDouble(String valor, String campo) {
        try {
            return Double.parseDouble(valor.replace(',', '.'));
        } catch (NumberFormatException ex) {
            throw new NumberFormatException("Ingrese un valor numérico válido para " + campo);
        }
    }

    private void mostrarError(String titulo, String detalle) {
        lblEstado.setText(detalle);
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(titulo);
        alert.setContentText(detalle);
        alert.showAndWait();
    }

    private static class ObjetivoListCell extends javafx.scene.control.ListCell<Objetivo> {
        @Override
        protected void updateItem(Objetivo item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
            } else {
                setText(item.getDescripcion() + " (" + String.format("%.0f%%", item.calcularPorcentaje()) + ")");
            }
        }
    }
}
