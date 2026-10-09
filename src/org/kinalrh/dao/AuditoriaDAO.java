package org.kinalrh.dao;

import java.sql.SQLException;

public interface AuditoriaDAO {
    void registrarEvento(Long idUsuario, String accion, String entidad, Long registroId, String campo, String valorAnterior, String valorNuevo) throws SQLException;
}