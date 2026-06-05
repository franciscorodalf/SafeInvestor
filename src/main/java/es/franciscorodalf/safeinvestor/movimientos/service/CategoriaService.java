package es.franciscorodalf.safeinvestor.movimientos.service;

import es.franciscorodalf.safeinvestor.auth.domain.Usuario;
import es.franciscorodalf.safeinvestor.movimientos.domain.Categoria;
import es.franciscorodalf.safeinvestor.movimientos.domain.CategoriaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoriaService {

    /** Categorías que se crean automáticamente al registrarse un usuario. */
    static final List<DefaultCategoria> DEFAULTS = List.of(
        new DefaultCategoria("Comida",         "#F59E0B"),
        new DefaultCategoria("Transporte",     "#3B82F6"),
        new DefaultCategoria("Hogar",          "#10B981"),
        new DefaultCategoria("Ocio",           "#EC4899"),
        new DefaultCategoria("Salud",          "#EF4444"),
        new DefaultCategoria("Compras",        "#8B5CF6"),
        new DefaultCategoria("Servicios",      "#6B7280"),
        new DefaultCategoria("Otros gastos",   "#9CA3AF"),
        new DefaultCategoria("Nómina",         "#22C55E"),
        new DefaultCategoria("Freelance",      "#14B8A6"),
        new DefaultCategoria("Otros ingresos", "#A3E635")
    );

    private final CategoriaRepository categorias;

    public CategoriaService(CategoriaRepository categorias) {
        this.categorias = categorias;
    }

    @Transactional
    public void seedDefaults(Usuario usuario) {
        for (DefaultCategoria d : DEFAULTS) {
            categorias.save(new Categoria(usuario, d.nombre(), d.color(), null));
        }
    }

    public List<Categoria> findAll(Usuario usuario) {
        return categorias.findByUsuarioOrderByNombre(usuario);
    }

    @Transactional
    public Categoria create(Usuario usuario, String nombre, String color, String icono) {
        if (categorias.existsByUsuarioAndNombreIgnoreCase(usuario, nombre)) {
            throw new CategoriaDuplicadaException(nombre);
        }
        return categorias.save(new Categoria(usuario, nombre, color, icono));
    }

    @Transactional
    public Categoria update(Usuario usuario, Long id, String nombre, String color, String icono) {
        Categoria c = categorias.findByIdAndUsuario(id, usuario)
            .orElseThrow(() -> new CategoriaNotFoundException(id));
        c.setNombre(nombre);
        c.setColor(color);
        c.setIcono(icono);
        return c;
    }

    @Transactional
    public void delete(Usuario usuario, Long id) {
        Categoria c = categorias.findByIdAndUsuario(id, usuario)
            .orElseThrow(() -> new CategoriaNotFoundException(id));
        categorias.delete(c);
    }

    private record DefaultCategoria(String nombre, String color) {}

    public static class CategoriaNotFoundException extends RuntimeException {
        public CategoriaNotFoundException(Long id) { super("Categoría no encontrada: " + id); }
    }

    public static class CategoriaDuplicadaException extends RuntimeException {
        public CategoriaDuplicadaException(String nombre) { super("Categoría ya existe: " + nombre); }
    }
}
