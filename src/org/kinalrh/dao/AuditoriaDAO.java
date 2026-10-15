package org.kinalrh.dao;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import org.kinalrh.model.Auditoria;

/**
 * Interfaz DAO para registro y consulta de auditoría.
 * T1.23, T3.18 Registrar alta y edición de empleado con autor y fecha.
 */
public interface AuditoriaDAO {

    void registrarEvento(Long idUsuario, String accion, String entidad, Long registroId, String campo, String valorAnterior, String valorNuevo) throws SQLException;

    void registrarEvento(Long idUsuario, String accion, String entidad, Long registroId, String campo, String valorAnterior, String valorNuevo, LocalDateTime fechaHora) throws SQLException;

    List<String> obtenerEventosConUsuario(int limite) throws SQLException;

    List<Auditoria> listarAuditoria(int limite) throws SQLException;

    List<Auditoria> listarAuditoriaPorFiltro(String busqueda, String entidad, int limite) throws SQLException;
}