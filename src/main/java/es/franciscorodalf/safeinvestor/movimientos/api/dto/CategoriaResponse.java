package es.franciscorodalf.safeinvestor.movimientos.api.dto;

import es.franciscorodalf.safeinvestor.movimientos.domain.Categoria;

public record CategoriaResponse(Long id, String nombre, String color, String icono) {
    public static CategoriaResponse from(Categoria c) {
        return new CategoriaResponse(c.getId(), c.getNombre(), c.getColor(), c.getIcono());
    }
}
