package es.franciscorodalf.safeinvestor.movimientos.api;

import es.franciscorodalf.safeinvestor.movimientos.api.dto.MovimientoRequest;
import es.franciscorodalf.safeinvestor.movimientos.api.dto.MovimientoResponse;
import es.franciscorodalf.safeinvestor.movimientos.security.CurrentUser;
import es.franciscorodalf.safeinvestor.movimientos.service.MovimientoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "Movimientos", description = "Gastos e ingresos del usuario autenticado")
@RestController
@RequestMapping("/api/movimientos")
public class MovimientoApiController {

    private final MovimientoService service;
    private final CurrentUser currentUser;

    public MovimientoApiController(MovimientoService service, CurrentUser currentUser) {
        this.service = service;
        this.currentUser = currentUser;
    }

    @GetMapping
    public List<MovimientoResponse> list(
        @RequestParam(required = false) Long categoriaId,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
        @RequestParam(defaultValue = "0") int page
    ) {
        return service.search(currentUser.get(), categoriaId, desde, hasta, page)
            .map(MovimientoResponse::from).getContent();
    }

    @GetMapping("/{id}")
    public MovimientoResponse get(@PathVariable Long id) {
        return MovimientoResponse.from(service.get(currentUser.get(), id));
    }

    @PostMapping
    public ResponseEntity<MovimientoResponse> create(@Valid @RequestBody MovimientoRequest req) {
        var m = service.create(currentUser.get(), req.categoriaId(), req.tipo(),
            req.importe(), req.descripcion(), req.fecha());
        return ResponseEntity.status(HttpStatus.CREATED).body(MovimientoResponse.from(m));
    }

    @PutMapping("/{id}")
    public MovimientoResponse update(@PathVariable Long id, @Valid @RequestBody MovimientoRequest req) {
        var m = service.update(currentUser.get(), id, req.categoriaId(), req.tipo(),
            req.importe(), req.descripcion(), req.fecha());
        return MovimientoResponse.from(m);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(currentUser.get(), id);
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(MovimientoService.MovimientoNotFoundException.class)
    public void handleNotFound() {}

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(IllegalArgumentException.class)
    public void handleBadCategoria() {}
}
