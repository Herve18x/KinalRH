package org.kinalrh.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import org.kinalrh.model.Usuario;
import org.kinalrh.util.Conexion;
import org.kinalrh.util.SessionManager;

public class AuditoriaService {
    
    public void registrarEvento(String accion, String entidad, Long registroId, String campo, String valorAnterior, String valorNuevo) {
        String sql = "INSERT INTO auditoria (id_usuario, accion, entidad, registro_id, campo, valor_anterior, valor_nuevo, ip, user_agent) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
                     
        Usuario current = SessionManager.getInstance().getCurrentUser();
        Long idUsuario = (current != null) ? current.getIdUsuario() : null;
        String sessionUuid = SessionManager.getInstance().getSessionUUID(); // Se puede registrar en user_agent para pruebas
        
        try (Connection conn = Conexion.getInstancia().conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setObject(1, idUsuario);
            stmt.setString(2, accion);
            stmt.setString(3, entidad);
            stmt.setObject(4, registroId);
            stmt.setString(5, campo);
            stmt.setString(6, valorAnterior);
            stmt.setString(7, valorNuevo);
            stmt.setString(8, "127.0.0.1"); // Dummy IP
            stmt.setString(9, "Session-UUID: " + (sessionUuid != null ? sessionUuid : "NONE"));
            
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            System.err.println("Error al registrar auditoria: " + e.getMessage());
        }
    }

    public List<String> obtenerAuditoriasPrueba() {
        List<String> logs = new ArrayList<>();
        String sql = "SELECT a.id_auditoria, a.accion, a.user_agent, u.nombre_usuario " +
                     "FROM auditoria a LEFT JOIN usuario u ON a.id_usuario = u.id_usuario " +
                     "ORDER BY a.id_auditoria DESC LIMIT 10";
        try (Connection conn = Conexion.getInstancia().conectar();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                logs.add("Accion: " + rs.getString("accion") + 
                         " | Usuario: " + rs.getString("nombre_usuario") + 
                         " | Session: " + rs.getString("user_agent"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return logs;
    }
}