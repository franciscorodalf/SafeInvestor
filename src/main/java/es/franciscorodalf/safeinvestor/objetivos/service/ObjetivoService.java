package es.franciscorodalf.safeinvestor.objetivos.service;

import es.franciscorodalf.safeinvestor.auth.domain.Usuario;
import es.franciscorodalf.safeinvestor.objetivos.domain.Objetivo;
import es.franciscorodalf.safeinvestor.objetivos.domain.ObjetivoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@Service
public class ObjetivoService {

    private final ObjetivoRepository objetivos;

    public ObjetivoService(ObjetivoRepository objetivos) {
        this.objetivos = objetivos;
    }

    public List<Objetivo> findAll(Usuario usuario) {
        return objetivos.findAllOrdered(usuario);
    }

    public Objetivo get(Usuario usuario, Long id) {
        return objetivos.findByIdAndUsuario(id, usuario)
            .orElseThrow(() -> new ObjetivoNotFoundException(id));
    }

    @Transactional
    public Objetivo create(Usuario usuario, String nombre, BigDecimal importeObjetivo,
                           LocalDate fechaLimite, String color) {
        return objetivos.save(new Objetivo(usuario, nombre, importeObjetivo, fechaLimite, color));
    }

    @Transactional
    public Objetivo update(Usuario usuario, Long id, String nombre, BigDecimal importeObjetivo,
                           LocalDate fechaLimite, String color) {
        Objetivo o = get(usuario, id);
        o.setNombre(nombre);
        o.setImporteObjetivo(importeObjetivo);
        o.setFechaLimite(fechaLimite);
        if (color != null) o.setColor(color);
        // Si el objetivo nuevo es mayor, ya no está completado
        if (o.getCompletadoAt() != null && o.getImporteAhorrado().compareTo(importeObjetivo) < 0) {
            o.setCompletadoAt(null);
        }
        return o;
    }

    @Transactional
    public Objetivo aportar(Usuario usuario, Long id, BigDecimal importe) {
        if (importe == null || importe.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El importe a aportar debe ser positivo");
        }
        Objetivo o = get(usuario, id);
        o.setImporteAhorrado(o.getImporteAhorrado().add(importe));
        if (o.getCompletadoAt() == null
            && o.getImporteAhorrado().compareTo(o.getImporteObjetivo()) >= 0) {
            o.setCompletadoAt(OffsetDateTime.now());
        }
        return o;
    }

    @Transactional
    public void delete(Usuario usuario, Long id) {
        objetivos.delete(get(usuario, id));
    }

    public static class ObjetivoNotFoundException extends RuntimeException {
        public ObjetivoNotFoundException(Long id) { super("Objetivo no encontrado: " + id); }
    }
}
