package org.kinalrh.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.kinalrh.dao.AuditoriaDAO;
import org.kinalrh.util.Conexion;

public class AuditoriaDAOImpl implements AuditoriaDAO {

    @Override
    public void registrarEvento(Long idUsuario, String accion, String entidad, Long registroId, String campo, String valorAnterior, String valorNuevo) throws SQLException {
        String sql = "INSERT INTO auditoria (id_usuario, accion, entidad, registro_id, campo, valor_anterior, valor_nuevo) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = Conexion.getInstancia().conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setObject(1, idUsuario);
            stmt.setString(2, accion);
            stmt.setString(3, entidad);
            stmt.setObject(4, registroId);
            stmt.setString(5, campo);
            stmt.setString(6, valorAnterior);
            stmt.setString(7, valorNuevo);
            
            stmt.executeUpdate();
        }
    }
}