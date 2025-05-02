package es.franciscorodalf.saveinvestor.frontend.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.Random;

import es.franciscorodalf.saveinvestor.backend.model.Usuario;

/**
 * Controlador de la vista de Estadísticas.
 */
public class EstadisticasController {

    @FXML
    private Button btnPrevMonth;
    
    @FXML
    private Button btnNextMonth;
    
    @FXML
    private Label lblMonthYear;
    
    @FXML
    private GridPane headerGrid;
    
    @FXML
    private GridPane calendarGrid;

    @FXML
    private Rectangle barAhorro;
    
    @FXML
    private Rectangle barGasto;
    
    @FXML
    private Label lblTotalGastos;
    
    @FXML
    private Label lblTotalAhorros;

    @FXML
    private Button btnVolver;

    private YearMonth currentYearMonth;
    private Usuario usuarioActual;
    private Random random = new Random(); // Para generar datos aleatorios de prueba

    /**
     * Inicializa el controlador
     */
    @FXML
    public void initialize() {
        // Inicializar el mes actual
        currentYearMonth = YearMonth.now();
        
        // Inicializar cabeceras de días
        inicializarDiasSemana();
        
        // Actualizar calendario
        actualizarCalendario();
        
        // Simular estadísticas
        actualizarEstadisticas();
        
        // Configurar listeners de botones
        configurarBotones();
    }
    
    /**
     * Inicializa los nombres de los días de la semana
     */
    private void inicializarDiasSemana() {
        String[] diasSemana = {"Lu", "Ma", "Mi", "Ju", "Vi", "Sa", "Do"};
        
        // Limpiar el grid de cabecera
        headerGrid.getChildren().clear();
        
        // Añadir los nombres de los días
        for (int i = 0; i < diasSemana.length; i++) {
            Label lblDia = new Label(diasSemana[i]);
            lblDia.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
            lblDia.setPrefWidth(50);
            lblDia.setAlignment(Pos.CENTER);
            headerGrid.add(lblDia, i, 0);
        }
    }
    
    /**
     * Establece el usuario actual
     * @param usuario El usuario actual de la aplicación
     */
    public void setUsuario(Usuario usuario) {
        this.usuarioActual = usuario;
        // Si se necesita, cargar datos del usuario
    }

    /**
     * Configura los botones y sus acciones
     */
    private void configurarBotones() {
        // Ya configurados por FXML con onAction, pero podríamos añadir más funcionalidad
        btnVolver.setOnAction(this::onVolver);
        btnPrevMonth.setOnAction(this::onPrevMonth);
        btnNextMonth.setOnAction(this::onNextMonth);
    }

    /**
     * Maneja el evento del botón mes anterior
     * @param event El evento de acción
     */
    @FXML
    public void onPrevMonth(ActionEvent event) {
        currentYearMonth = currentYearMonth.minusMonths(1);
        actualizarCalendario();
        actualizarEstadisticas();
    }

    /**
     * Maneja el evento del botón mes siguiente
     * @param event El evento de acción
     */
    @FXML
    public void onNextMonth(ActionEvent event) {
        currentYearMonth = currentYearMonth.plusMonths(1);
        actualizarCalendario();
        actualizarEstadisticas();
    }

    /**
     * Genera la vista del calendario para el mes actual.
     */
    private void actualizarCalendario() {
        // Actualizar etiqueta del mes y año
        Locale locale = new Locale("es", "ES");
        String mes = currentYearMonth.getMonth().getDisplayName(TextStyle.FULL, locale);
        mes = mes.substring(0, 1).toUpperCase() + mes.substring(1);
        lblMonthYear.setText(mes + " " + currentYearMonth.getYear());
        
        // Limpiar el grid
        calendarGrid.getChildren().clear();
        
        // Obtener fecha del primer día del mes
        LocalDate firstOfMonth = currentYearMonth.atDay(1);
        
        // Obtener día de la semana (0 = Lunes, 6 = Domingo en LocalDate)
        int dayOfWeek = firstOfMonth.getDayOfWeek().getValue() - 1;
        
        // Número de días en el mes
        int daysInMonth = currentYearMonth.lengthOfMonth();
        
        // Llenar el calendario con celdas más grandes
        int day = 1;
        for (int i = 0; i < 6; i++) { // 6 filas máx
            for (int j = 0; j < 7; j++) { // 7 días por semana
                if ((i == 0 && j < dayOfWeek) || day > daysInMonth) {
                    // Celda vacía
                    continue;
                }
                
                // Crear celda con el día
                StackPane cell = new StackPane();
                cell.setPrefSize(50, 40); // Hacer celdas más grandes
                cell.setStyle("-fx-border-color: #e0e0e0; -fx-border-radius: 5; -fx-background-radius: 5;");
                
                Label lblDay = new Label(String.valueOf(day));
                lblDay.setStyle("-fx-font-size: 14px;");
                cell.getChildren().add(lblDay);
                
                // Si es día actual, marcarlo
                LocalDate currentDate = LocalDate.now();
                if (day == currentDate.getDayOfMonth() && 
                    currentYearMonth.getMonth() == currentDate.getMonth() && 
                    currentYearMonth.getYear() == currentDate.getYear()) {
                    Circle circle = new Circle(15); // Círculo más grande
                    circle.setFill(Color.LIGHTBLUE);
                    circle.setOpacity(0.7);
                    cell.getChildren().add(0, circle); // Añadir círculo detrás del texto
                }
                
                // Agregar la celda al grid
                calendarGrid.add(cell, j, i);
                StackPane.setAlignment(lblDay, Pos.CENTER);
                
                // Configurar evento para la celda
                final int selectedDay = day;
                cell.setOnMouseClicked(e -> mostrarDatosDia(selectedDay));
                
                // Incrementar día
                day++;
            }
        }
    }
    
    /**
     * Muestra los datos de un día específico
     * @param day El día seleccionado
     */
    private void mostrarDatosDia(int day) {
        System.out.println("Día seleccionado: " + day + " de " + 
                     currentYearMonth.getMonth().getDisplayName(TextStyle.FULL, new Locale("es")) + 
                     " de " + currentYearMonth.getYear());
        // Aquí se podría mostrar una ventana emergente o actualizar información
    }
    
    /**
     * Actualiza las estadísticas mostradas
     */
    private void actualizarEstadisticas() {
        // Inicialmente las barras no mostrarán datos, esto se implementará después
        // Se establecen valores iniciales de cero
        double ahorros = 0.0;
        double gastos = 0.0;
        
        // Actualizar etiquetas
        lblTotalAhorros.setText(String.format("%.2f$", ahorros));
        lblTotalGastos.setText(String.format("%.2f$", gastos));
        
        // Las barras comienzan con altura mínima (se expandirán cuando haya datos)
        // No aplicamos transformaciones para evitar que se muevan
        barAhorro.setHeight(1);
        barGasto.setHeight(1);
        
        // No usamos translateY para evitar movimientos
    }

    /**
     * Maneja el evento del botón volver
     * @param event El evento de acción
     */
    @FXML
    public void onVolver(ActionEvent event) {
        cambiarEscena(event, "/es/franciscorodalf/saveinvestor/main.fxml");
    }
    
    /**
     * Cambia a la escena especificada
     * @param event El evento de acción
     * @param fxml La ruta al archivo FXML
     */
    private void cambiarEscena(ActionEvent event, String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();
            
            // Si vamos a la pantalla principal, pasar el usuario
            if (fxml.contains("main.fxml") && usuarioActual != null) {
                MainController controller = loader.getController();
                controller.setUsuario(usuarioActual);
            }
            
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            System.err.println("Error al cargar vista: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
