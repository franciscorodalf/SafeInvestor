package es.franciscorodalf.saveinvestor.backend.dao;

import java.sql.SQLException;
import java.util.List;

public interface DAO<T> {
    void insertar(T entidad) throws SQLException;

    void actualizar(T entidad) throws SQLException;

    void eliminar(Integer id) throws SQLException;

    T obtenerPorId(Integer id) throws SQLException;

    List<T> obtenerTodos() throws SQLException;
}
