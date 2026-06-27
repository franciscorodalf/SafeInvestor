package es.franciscorodalf.safeinvestor.tareas.api;

import es.franciscorodalf.safeinvestor.movimientos.security.CurrentUser;
import es.franciscorodalf.safeinvestor.tareas.api.dto.TareaRequest;
import es.franciscorodalf.safeinvestor.tareas.api.dto.TareaResponse;
import es.franciscorodalf.safeinvestor.tareas.service.TareaService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Tareas", description = "Recordatorios financieros con fecha de vencimiento")
@RestController
@RequestMapping("/api/tareas")
public class TareaApiController {

    private final TareaService service;
    private final CurrentUser currentUser;

    public TareaApiController(TareaService service, CurrentUser currentUser) {
        this.service = service;
        this.currentUser = currentUser;
    }

    @GetMapping
    public List<TareaResponse> list() {
        return service.findAll(currentUser.get()).stream().map(TareaResponse::from).toList();
    }

    @PostMapping
    public ResponseEntity<TareaResponse> create(@Valid @RequestBody TareaRequest req) {
        var t = service.create(currentUser.get(), req.titulo(), req.descripcion(), req.fechaVencimiento());
        return ResponseEntity.status(HttpStatus.CREATED).body(TareaResponse.from(t));
    }

    @PutMapping("/{id}")
    public TareaResponse update(@PathVariable Long id, @Valid @RequestBody TareaRequest req) {
        var t = service.update(currentUser.get(), id, req.titulo(), req.descripcion(), req.fechaVencimiento());
        return TareaResponse.from(t);
    }

    @PostMapping("/{id}/completar")
    public TareaResponse completar(@PathVariable Long id) {
        return TareaResponse.from(service.completar(currentUser.get(), id));
    }

    @PostMapping("/{id}/descompletar")
    public TareaResponse descompletar(@PathVariable Long id) {
        return TareaResponse.from(service.descompletar(currentUser.get(), id));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(currentUser.get(), id);
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(TareaService.TareaNotFoundException.class)
    public void handleNotFound() {}
}
