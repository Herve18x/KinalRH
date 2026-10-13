package org.kinalrh.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.kinalrh.dao.AuditoriaDAO;
import org.kinalrh.util.Conexion;
import org.kinalrh.util.SessionManager;

public class AuditoriaDAOImpl implements AuditoriaDAO {

    @Override
    public void registrarEvento(Long idUsuario, String accion, String entidad, Long registroId, String campo, String valorAnterior, String valorNuevo) throws SQLException {
        String sql = "INSERT INTO auditoria (id_usuario, accion, entidad, registro_id, campo, valor_anterior, valor_nuevo, ip, user_agent) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        String sessionUuid = SessionManager.getInstance().getSessionUUID();
        try (Connection conn = Conexion.getInstancia().conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setObject(1, idUsuario);
            stmt.setString(2, accion);
            stmt.setString(3, entidad);
            stmt.setObject(4, registroId);
            stmt.setString(5, campo);
            stmt.setString(6, valorAnterior);
            stmt.setString(7, valorNuevo);
            stmt.setString(8, "127.0.0.1");
            stmt.setString(9, "Session-UUID: " + (sessionUuid != null ? sessionUuid : "NONE"));
            
            stmt.executeUpdate();
        }
    }

    @Override
    public List<String> obtenerEventosConUsuario(int limite) throws SQLException {
        List<String> eventos = new ArrayList<>();
        String sql = "SELECT a.id_auditoria, a.id_usuario, u.nombre_usuario, a.accion, a.entidad, a.user_agent, a.fecha_hora " +
                     "FROM auditoria a " +
                     "INNER JOIN usuario u ON a.id_usuario = u.id_usuario " +
                     "ORDER BY a.id_auditoria DESC LIMIT ?";
        try (Connection conn = Conexion.getInstancia().conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, limite > 0 ? limite : 10);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    eventos.add(String.format("ID_Audit=%d | ID_Usuario=%d (%s) | Accion=%s | Entidad=%s | %s",
                            rs.getLong("id_auditoria"),
                            rs.getLong("id_usuario"),
                            rs.getString("nombre_usuario"),
                            rs.getString("accion"),
                            rs.getString("entidad"),
                            rs.getString("user_agent")));
                }
            }
        }
        return eventos;
    }
}