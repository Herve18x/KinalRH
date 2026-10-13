package org.kinalrh.dao;

import java.sql.SQLException;
import java.util.List;

public interface AuditoriaDAO {
    void registrarEvento(Long idUsuario, String accion, String entidad, Long registroId, String campo, String valorAnterior, String valorNuevo) throws SQLException;
    List<String> obtenerEventosConUsuario(int limite) throws SQLException;
}