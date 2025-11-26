package es.franciscorodalf.saveinvestor.backend.service;

import es.franciscorodalf.saveinvestor.backend.dao.UsuarioDAO;
import es.franciscorodalf.saveinvestor.backend.dao.MovimientoInversionDAO;
// Add other DAOs as needed

public class ServiceFactory {

    private static ServiceFactory instance;

    // DAOs
    private UsuarioDAO usuarioDAO;
    private MovimientoInversionDAO movimientoInversionDAO;

    private ServiceFactory() {
        // Initialize DAOs
    }

    public static synchronized ServiceFactory getInstance() {
        if (instance == null) {
            instance = new ServiceFactory();
        }
        return instance;
    }

    public UsuarioDAO getUsuarioDAO() {
        if (usuarioDAO == null) {
            usuarioDAO = new UsuarioDAO();
        }
        return usuarioDAO;
    }

    public MovimientoInversionDAO getMovimientoInversionDAO() {
        if (movimientoInversionDAO == null) {
            movimientoInversionDAO = new MovimientoInversionDAO();
        }
        return movimientoInversionDAO;
    }

    // Add other getters
}
