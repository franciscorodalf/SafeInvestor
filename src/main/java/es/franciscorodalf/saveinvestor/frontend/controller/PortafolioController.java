package es.franciscorodalf.saveinvestor.frontend.controller;

import es.franciscorodalf.saveinvestor.backend.model.CuentaInversion.TipoActivo;
import es.franciscorodalf.saveinvestor.backend.model.Usuario;
import es.franciscorodalf.saveinvestor.backend.model.ValorHistoricoInversion;
import es.franciscorodalf.saveinvestor.backend.service.PortafolioService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PortafolioController {

    @FXML
    private LineChart<String, Number> lineRendimiento;

    @FXML
    private PieChart pieDistribucion;

    @FXML
    private Label lblValorTotal;

    @FXML
    private Label lblMensaje;

    private Usuario usuarioActual;
    private PortafolioService portafolioService;

    @FXML
    private void initialize() {
        portafolioService = new PortafolioService();
        lineRendimiento.setAnimated(false);
        pieDistribucion.setAnimated(false);
    }

    public void setUsuario(Usuario usuario) {
        this.usuarioActual = usuario;
        cargarDatosPortafolio();
    }

    private void cargarDatosPortafolio() {
        if (usuarioActual == null || usuarioActual.getId() == null) {
            lblMensaje.setText("No se pudo determinar el usuario activo.");
            return;
        }

        try {
            double valorTotal = portafolioService.obtenerValorActualTotal(usuarioActual.getId());
            lblValorTotal.setText(String.format(Locale.getDefault(), "Valor total actual: %.2f", valorTotal));

            cargarRendimientoHistorico(portafolioService.obtenerRendimientoHistorico(usuarioActual.getId()));
            cargarDistribucion(portafolioService.calcularDistribucionPorTipo(usuarioActual.getId()));

            if (!lineRendimiento.isVisible() && !pieDistribucion.isVisible()) {
                lblMensaje.setText("No hay datos de inversiones disponibles. Registra aportes para visualizar el portafolio.");
            } else {
                lblMensaje.setText("");
            }
        } catch (SQLException e) {
            lblMensaje.setText("Error al cargar el portafolio: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void cargarRendimientoHistorico(List<ValorHistoricoInversion> valores) {
        lineRendimiento.getData().clear();
        if (valores == null || valores.isEmpty()) {
            lineRendimiento.setVisible(false);
            lineRendimiento.setManaged(false);
            return;
        }

        lineRendimiento.setVisible(true);
        lineRendimiento.setManaged(true);
        XYChart.Series<String, Number> serie = new XYChart.Series<>();
        serie.setName("Valor del portafolio");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM", Locale.getDefault());
        for (ValorHistoricoInversion valor : valores) {
            serie.getData().add(new XYChart.Data<>(valor.getFecha().format(formatter), valor.getValorTotal()));
        }
        lineRendimiento.getData().add(serie);
    }

    private void cargarDistribucion(Map<TipoActivo, Double> distribucion) {
        pieDistribucion.getData().clear();
        if (distribucion == null || distribucion.isEmpty()) {
            pieDistribucion.setVisible(false);
            pieDistribucion.setManaged(false);
            return;
        }

        double total = distribucion.values().stream().mapToDouble(Double::doubleValue).sum();
        if (total <= 0) {
            pieDistribucion.setVisible(false);
            pieDistribucion.setManaged(false);
            return;
        }

        ObservableList<PieChart.Data> datos = FXCollections.observableArrayList();
        distribucion.forEach((tipo, valor) -> {
            double porcentaje = (valor / total) * 100;
            String etiqueta = String.format(Locale.getDefault(), "%s (%.1f%%)", formatearTipo(tipo), porcentaje);
            datos.add(new PieChart.Data(etiqueta, valor));
        });

        pieDistribucion.setData(datos);
        pieDistribucion.setVisible(true);
        pieDistribucion.setManaged(true);
    }

    private String formatearTipo(TipoActivo tipo) {
        switch (tipo) {
            case AHORRO:
                return "Ahorro";
            case FONDO:
                return "Fondos";
            case CRYPTO:
                return "Criptomonedas";
            default:
                return tipo.name();
        }
    }

    @FXML
    private void onVolver(ActionEvent event) {
        cambiarEscena(event, "/es/franciscorodalf/saveinvestor/main.fxml");
    }

    private void cambiarEscena(ActionEvent event, String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();

            if (fxml.contains("main.fxml") && usuarioActual != null) {
                MainController controller = loader.getController();
                controller.setUsuario(usuarioActual);
            }

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            lblMensaje.setText("No se pudo cambiar la vista: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
