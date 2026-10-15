package org.kinalrh.dao;

import java.sql.SQLException;
import java.util.List;
import org.kinalrh.model.TipoLogro;

/**
 * Interfaz DAO para gestión de tipo_logro.
 * T3.09 Lectura/gestión básica de nivel académico y tipo de logro.
 */
public interface TipoLogroDAO {

    List<TipoLogro> listarTodos() throws SQLException;

    List<TipoLogro> listarActivos();

    TipoLogro buscarPorId(Long id);

    long insertar(TipoLogro tipo) throws SQLException;

    void actualizar(TipoLogro tipo) throws SQLException;

    void cambiarEstado(Long id, boolean activo) throws SQLException;

    boolean existeCodigo(String codigo, Long idExcluido);

    boolean existeNombre(String nombre, Long idExcluido);
}
