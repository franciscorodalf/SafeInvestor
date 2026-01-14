package es.franciscorodalf.saveinvestor.frontend.controller;

import es.franciscorodalf.saveinvestor.backend.model.Usuario;

/**
 * Interface for controllers that need information about the authenticated user.
 */
public interface UsuarioAware {
    void setUsuario(Usuario usuario);
}
