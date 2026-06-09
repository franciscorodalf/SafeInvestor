package es.franciscorodalf.safeinvestor.objetivos.api;

import es.franciscorodalf.safeinvestor.movimientos.security.CurrentUser;
import es.franciscorodalf.safeinvestor.objetivos.api.dto.AportarRequest;
import es.franciscorodalf.safeinvestor.objetivos.api.dto.ObjetivoRequest;
import es.franciscorodalf.safeinvestor.objetivos.api.dto.ObjetivoResponse;
import es.franciscorodalf.safeinvestor.objetivos.service.ObjetivoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/objetivos")
public class ObjetivoApiController {

    private final ObjetivoService service;
    private final CurrentUser currentUser;

    public ObjetivoApiController(ObjetivoService service, CurrentUser currentUser) {
        this.service = service;
        this.currentUser = currentUser;
    }

    @GetMapping
    public List<ObjetivoResponse> list() {
        return service.findAll(currentUser.get()).stream().map(ObjetivoResponse::from).toList();
    }

    @GetMapping("/{id}")
    public ObjetivoResponse get(@PathVariable Long id) {
        return ObjetivoResponse.from(service.get(currentUser.get(), id));
    }

    @PostMapping
    public ResponseEntity<ObjetivoResponse> create(@Valid @RequestBody ObjetivoRequest req) {
        var o = service.create(currentUser.get(), req.nombre(), req.importeObjetivo(),
            req.fechaLimite(), req.color());
        return ResponseEntity.status(HttpStatus.CREATED).body(ObjetivoResponse.from(o));
    }

    @PutMapping("/{id}")
    public ObjetivoResponse update(@PathVariable Long id, @Valid @RequestBody ObjetivoRequest req) {
        var o = service.update(currentUser.get(), id, req.nombre(), req.importeObjetivo(),
            req.fechaLimite(), req.color());
        return ObjetivoResponse.from(o);
    }

    @PostMapping("/{id}/aportar")
    public ObjetivoResponse aportar(@PathVariable Long id, @Valid @RequestBody AportarRequest req) {
        var o = service.aportar(currentUser.get(), id, req.importe());
        return ObjetivoResponse.from(o);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(currentUser.get(), id);
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(ObjetivoService.ObjetivoNotFoundException.class)
    public void handleNotFound() {}
}
