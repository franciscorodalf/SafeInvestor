package es.franciscorodalf.safeinvestor;

import es.franciscorodalf.safeinvestor.movimientos.domain.Movimiento;
import es.franciscorodalf.safeinvestor.movimientos.domain.TipoMovimiento;
import es.franciscorodalf.safeinvestor.movimientos.security.CurrentUser;
import es.franciscorodalf.safeinvestor.movimientos.service.MovimientoService;
import es.franciscorodalf.safeinvestor.objetivos.domain.Objetivo;
import es.franciscorodalf.safeinvestor.objetivos.service.ObjetivoService;
import es.franciscorodalf.safeinvestor.tareas.domain.Tarea;
import es.franciscorodalf.safeinvestor.tareas.service.TareaService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Controller
public class HomeController {

    private final MovimientoService movimientos;
    private final ObjetivoService objetivos;
    private final TareaService tareas;
    private final CurrentUser currentUser;

    public HomeController(MovimientoService movimientos,
                          ObjetivoService objetivos,
                          TareaService tareas,
                          CurrentUser currentUser) {
        this.movimientos = movimientos;
        this.objetivos = objetivos;
        this.tareas = tareas;
        this.currentUser = currentUser;
    }

    @GetMapping("/")
    public String home(Model model, Authentication auth) {
        boolean authenticated = auth != null && auth.isAuthenticated()
            && !"anonymousUser".equals(auth.getPrincipal());

        if (!authenticated) {
            return "index";
        }

        var u = currentUser.get();
        LocalDate hoy = LocalDate.now();
        LocalDate inicioMes = hoy.withDayOfMonth(1);

        // Movimientos del mes
        var movimientosMes = movimientos.search(u, null, inicioMes, hoy, 0).getContent();
        BigDecimal ingresosMes = movimientosMes.stream()
            .filter(m -> m.getTipo() == TipoMovimiento.INGRESO)
            .map(Movimiento::getImporte)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal gastosMes = movimientosMes.stream()
            .filter(m -> m.getTipo() == TipoMovimiento.GASTO)
            .map(Movimiento::getImporte)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal balanceMes = ingresosMes.subtract(gastosMes);

        // Últimos 5 movimientos en total
        var ultimosMovimientos = movimientos.search(u, null, null, null, 0).getContent()
            .stream().limit(5).toList();

        // Objetivos en progreso (no completados)
        List<Objetivo> objetivosActivos = objetivos.findAll(u).stream()
            .filter(o -> !o.isCompletado())
            .limit(3)
            .toList();
        long objetivosCompletados = objetivos.findAll(u).stream()
            .filter(Objetivo::isCompletado).count();

        // Tareas pendientes
        List<Tarea> tareasPendientes = tareas.findAll(u).stream()
            .filter(t -> !t.isCompletada())
            .limit(4)
            .toList();
        long tareasVencidas = tareas.findAll(u).stream()
            .filter(Tarea::isVencida).count();

        // Saludo por hora del día
        int hora = java.time.LocalTime.now().getHour();
        String saludo;
        if (hora < 6) saludo = "Buenas noches";
        else if (hora < 13) saludo = "Buenos días";
        else if (hora < 21) saludo = "Buenas tardes";
        else saludo = "Buenas noches";

        model.addAttribute("usuario", u);
        model.addAttribute("saludo", saludo);
        model.addAttribute("fecha", hoy);
        model.addAttribute("ingresosMes", ingresosMes);
        model.addAttribute("gastosMes", gastosMes);
        model.addAttribute("balanceMes", balanceMes);
        model.addAttribute("ultimosMovimientos", ultimosMovimientos);
        model.addAttribute("objetivosActivos", objetivosActivos);
        model.addAttribute("objetivosCompletados", objetivosCompletados);
        model.addAttribute("tareasPendientes", tareasPendientes);
        model.addAttribute("tareasVencidas", tareasVencidas);

        return "dashboard";
    }
}
