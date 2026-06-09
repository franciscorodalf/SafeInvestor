package es.franciscorodalf.safeinvestor.tareas.service;

import es.franciscorodalf.safeinvestor.auth.domain.Usuario;
import es.franciscorodalf.safeinvestor.tareas.domain.Tarea;
import es.franciscorodalf.safeinvestor.tareas.domain.TareaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@Service
public class TareaService {

    private final TareaRepository tareas;

    public TareaService(TareaRepository tareas) {
        this.tareas = tareas;
    }

    public List<Tarea> findAll(Usuario usuario) {
        return tareas.findAllOrdered(usuario);
    }

    public Tarea get(Usuario usuario, Long id) {
        return tareas.findByIdAndUsuario(id, usuario)
            .orElseThrow(() -> new TareaNotFoundException(id));
    }

    @Transactional
    public Tarea create(Usuario usuario, String titulo, String descripcion,
                        LocalDate fechaVencimiento) {
        return tareas.save(new Tarea(usuario, titulo, descripcion, fechaVencimiento));
    }

    @Transactional
    public Tarea update(Usuario usuario, Long id, String titulo, String descripcion,
                        LocalDate fechaVencimiento) {
        Tarea t = get(usuario, id);
        t.setTitulo(titulo);
        t.setDescripcion(descripcion);
        t.setFechaVencimiento(fechaVencimiento);
        return t;
    }

    @Transactional
    public Tarea completar(Usuario usuario, Long id) {
        Tarea t = get(usuario, id);
        if (t.getCompletadaAt() == null) {
            t.setCompletadaAt(OffsetDateTime.now());
        }
        return t;
    }

    @Transactional
    public Tarea descompletar(Usuario usuario, Long id) {
        Tarea t = get(usuario, id);
        t.setCompletadaAt(null);
        return t;
    }

    @Transactional
    public void delete(Usuario usuario, Long id) {
        tareas.delete(get(usuario, id));
    }

    public static class TareaNotFoundException extends RuntimeException {
        public TareaNotFoundException(Long id) { super("Tarea no encontrada: " + id); }
    }
}
