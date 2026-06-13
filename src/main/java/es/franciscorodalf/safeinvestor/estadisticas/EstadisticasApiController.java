package es.franciscorodalf.safeinvestor.estadisticas;

import es.franciscorodalf.safeinvestor.movimientos.security.CurrentUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/estadisticas")
public class EstadisticasApiController {

    private final EstadisticasService service;
    private final CurrentUser currentUser;

    public EstadisticasApiController(EstadisticasService service, CurrentUser currentUser) {
        this.service = service;
        this.currentUser = currentUser;
    }

    @GetMapping("/gastos-por-categoria")
    public List<EstadisticasService.CategoriaTotal> gastosPorCategoria() {
        return service.gastosPorCategoria(currentUser.get());
    }

    @GetMapping("/evolucion-mensual")
    public List<EstadisticasService.MesTotales> evolucionMensual() {
        return service.evolucionMensual(currentUser.get());
    }
}
