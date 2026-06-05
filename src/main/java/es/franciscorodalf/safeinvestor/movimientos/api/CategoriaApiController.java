package es.franciscorodalf.safeinvestor.movimientos.api;

import es.franciscorodalf.safeinvestor.movimientos.api.dto.CategoriaRequest;
import es.franciscorodalf.safeinvestor.movimientos.api.dto.CategoriaResponse;
import es.franciscorodalf.safeinvestor.movimientos.security.CurrentUser;
import es.franciscorodalf.safeinvestor.movimientos.service.CategoriaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categorias")
public class CategoriaApiController {

    private final CategoriaService service;
    private final CurrentUser currentUser;

    public CategoriaApiController(CategoriaService service, CurrentUser currentUser) {
        this.service = service;
        this.currentUser = currentUser;
    }

    @GetMapping
    public List<CategoriaResponse> list() {
        return service.findAll(currentUser.get()).stream().map(CategoriaResponse::from).toList();
    }

    @PostMapping
    public ResponseEntity<CategoriaResponse> create(@Valid @RequestBody CategoriaRequest req) {
        var c = service.create(currentUser.get(), req.nombre(), req.color(), req.icono());
        return ResponseEntity.status(HttpStatus.CREATED).body(CategoriaResponse.from(c));
    }

    @PutMapping("/{id}")
    public CategoriaResponse update(@PathVariable Long id, @Valid @RequestBody CategoriaRequest req) {
        var c = service.update(currentUser.get(), id, req.nombre(), req.color(), req.icono());
        return CategoriaResponse.from(c);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(currentUser.get(), id);
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(CategoriaService.CategoriaNotFoundException.class)
    public void handleNotFound() {}

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(CategoriaService.CategoriaDuplicadaException.class)
    public void handleDuplicate() {}
}
