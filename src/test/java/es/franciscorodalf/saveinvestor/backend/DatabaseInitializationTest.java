package es.franciscorodalf.saveinvestor.backend;

import es.franciscorodalf.saveinvestor.backend.dao.CuentaInversionDAO;
import es.franciscorodalf.saveinvestor.backend.model.abstractas.Conexion;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertTrue;

class DatabaseInitializationTest {

    @Test
    void createsMissingPortfolioTables() throws SQLException {
        CuentaInversionDAO dao = new CuentaInversionDAO();
        Connection connection = null;
        try {
            connection = dao.conectar();
            try (Statement statement = connection.createStatement();
                 ResultSet rs = statement.executeQuery(
                     "SELECT name FROM sqlite_master WHERE type='table' AND name='cuenta_inversion'")) {
                assertTrue(rs.next(), "La tabla cuenta_inversion no existe después de inicializar la BD");
            }
        } finally {
            Conexion.cerrar();
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        }
    }
}
