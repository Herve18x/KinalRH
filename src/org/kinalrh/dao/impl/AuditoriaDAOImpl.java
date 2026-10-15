package org.kinalrh.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.kinalrh.dao.AuditoriaDAO;
import org.kinalrh.model.Auditoria;
import org.kinalrh.util.Conexion;
import org.kinalrh.util.SessionManager;

/**
 * Implementación de AuditoriaDAO con soporte para fecha, autor y listados estructurados.
 * T1.23, T3.18 Registrar alta y edición de empleado con autor y fecha.
 */
public class AuditoriaDAOImpl implements AuditoriaDAO {

    @Override
    public void registrarEvento(Long idUsuario, String accion, String entidad, Long registroId, String campo, String valorAnterior, String valorNuevo) throws SQLException {
        registrarEvento(idUsuario, accion, entidad, registroId, campo, valorAnterior, valorNuevo, LocalDateTime.now());
    }

    @Override
    public void registrarEvento(Long idUsuario, String accion, String entidad, Long registroId, String campo, String valorAnterior, String valorNuevo, LocalDateTime fechaHora) throws SQLException {
        String sql = "INSERT INTO auditoria (id_usuario, accion, entidad, registro_id, campo, valor_anterior, valor_nuevo, ip, user_agent, fecha_hora) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
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
            stmt.setTimestamp(10, fechaHora != null ? Timestamp.valueOf(fechaHora) : Timestamp.valueOf(LocalDateTime.now()));
            
            stmt.executeUpdate();
        }
    }

    @Override
    public List<String> obtenerEventosConUsuario(int limite) throws SQLException {
        List<String> eventos = new ArrayList<>();
        String sql = "SELECT a.id_auditoria, a.id_usuario, u.nombre_usuario, a.accion, a.entidad, a.registro_id, a.user_agent, a.fecha_hora " +
                     "FROM auditoria a " +
                     "LEFT JOIN usuario u ON a.id_usuario = u.id_usuario " +
                     "ORDER BY a.id_auditoria DESC LIMIT ?";
        try (Connection conn = Conexion.getInstancia().conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, limite > 0 ? limite : 10);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String username = rs.getString("nombre_usuario");
                    if (username == null) {
                        username = "SISTEMA";
                    }
                    eventos.add(String.format("ID_Audit=%d | ID_Usuario=%s (%s) | Accion=%s | Entidad=%s | Registro=%s | Fecha=%s | %s",
                            rs.getLong("id_auditoria"),
                            rs.getObject("id_usuario"),
                            username,
                            rs.getString("accion"),
                            rs.getString("entidad"),
                            rs.getObject("registro_id"),
                            rs.getTimestamp("fecha_hora"),
                            rs.getString("user_agent")));
                }
            }
        }
        return eventos;
    }

    @Override
    public List<Auditoria> listarAuditoria(int limite) throws SQLException {
        return listarAuditoriaPorFiltro(null, null, limite);
    }

    @Override
    public List<Auditoria> listarAuditoriaPorFiltro(String busqueda, String entidad, int limite) throws SQLException {
        List<Auditoria> lista = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT a.id_auditoria, a.id_usuario, u.nombre_usuario, a.accion, a.entidad, a.registro_id, " +
                "a.campo, a.valor_anterior, a.valor_nuevo, a.ip, a.user_agent, a.fecha_hora " +
                "FROM auditoria a " +
                "LEFT JOIN usuario u ON a.id_usuario = u.id_usuario " +
                "WHERE 1=1 "
        );
        List<Object> params = new ArrayList<>();

        if (entidad != null && !entidad.trim().isEmpty() && !entidad.equalsIgnoreCase("TODAS")) {
            sql.append("AND a.entidad = ? ");
            params.add(entidad.trim());
        }
        if (busqueda != null && !busqueda.trim().isEmpty()) {
            sql.append("AND (LOWER(a.accion) LIKE ? OR LOWER(a.entidad) LIKE ? OR LOWER(COALESCE(u.nombre_usuario, '')) LIKE ? OR LOWER(COALESCE(a.campo, '')) LIKE ? OR LOWER(COALESCE(a.valor_nuevo, '')) LIKE ?) ");
            String term = "%" + busqueda.trim().toLowerCase() + "%";
            params.add(term);
            params.add(term);
            params.add(term);
            params.add(term);
            params.add(term);
        }

        sql.append("ORDER BY a.id_auditoria DESC LIMIT ?");
        params.add(limite > 0 ? limite : 100);

        try (Connection conn = Conexion.getInstancia().conectar();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Timestamp ts = rs.getTimestamp("fecha_hora");
                    LocalDateTime dt = ts != null ? ts.toLocalDateTime() : null;
                    lista.add(new Auditoria(
                            rs.getLong("id_auditoria"),
                            rs.getObject("id_usuario") != null ? rs.getLong("id_usuario") : null,
                            rs.getString("nombre_usuario"),
                            rs.getString("accion"),
                            rs.getString("entidad"),
                            rs.getObject("registro_id") != null ? rs.getLong("registro_id") : null,
                            rs.getString("campo"),
                            rs.getString("valor_anterior"),
                            rs.getString("valor_nuevo"),
                            rs.getString("ip"),
                            rs.getString("user_agent"),
                            dt
                    ));
                }
            }
        }
        return lista;
    }
}