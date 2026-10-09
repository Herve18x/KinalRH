package org.kinalrh.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.kinalrh.dao.LogroDAO;
import org.kinalrh.model.Logro;
import org.kinalrh.util.Conexion;

public class LogroDAOImpl implements LogroDAO {
    @Override
    public List<Logro> listarTodos() throws SQLException {
        List<Logro> lista = new ArrayList<>();
        String sql = "SELECT id_logro, id_tipo_logro, nombre, descripcion, institucion, fecha_obtencion, origen, financiado_por_kinal, detalle_financiacion, activo FROM logro WHERE activo = 1";
        try (Connection conn = Conexion.getInstancia().conectar();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Logro l = new Logro();
                l.setIdLogro(rs.getLong("id_logro"));
                l.setIdTipoLogro(rs.getLong("id_tipo_logro"));
                l.setNombre(rs.getString("nombre"));
                l.setDescripcion(rs.getString("descripcion"));
                l.setInstitucion(rs.getString("institucion"));
                if (rs.getDate("fecha_obtencion") != null) {
                    l.setFechaObtencion(rs.getDate("fecha_obtencion").toLocalDate());
                }
                l.setOrigen(rs.getString("origen"));
                l.setFinanciadoPorKinal(rs.getBoolean("financiado_por_kinal"));
                l.setDetalleFinanciacion(rs.getString("detalle_financiacion"));
                l.setActivo(rs.getBoolean("activo"));
                lista.add(l);
            }
        }
        return lista;
    }

    @Override
    public long insertar(Logro logro) throws SQLException {
        String sql = "INSERT INTO logro (id_tipo_logro, nombre, descripcion, institucion, fecha_obtencion, origen, financiado_por_kinal, detalle_financiacion) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = Conexion.getInstancia().conectar();
             PreparedStatement stmt = conn.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            stmt.setObject(1, logro.getIdTipoLogro() != null ? logro.getIdTipoLogro() : 1L); // Default fallback
            stmt.setString(2, logro.getNombre());
            stmt.setString(3, logro.getDescripcion());
            stmt.setString(4, logro.getInstitucion());
            stmt.setObject(5, logro.getFechaObtencion() != null ? java.sql.Date.valueOf(logro.getFechaObtencion()) : null);
            stmt.setString(6, logro.getOrigen() != null ? logro.getOrigen() : "OTRO");
            stmt.setBoolean(7, logro.getFinanciadoPorKinal() != null ? logro.getFinanciadoPorKinal() : false);
            stmt.setString(8, logro.getDetalleFinanciacion());
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) return rs.getLong(1);
            }
        }
        return 0;
    }

    @Override
    public boolean actualizar(Logro logro) throws SQLException {
        String sql = "UPDATE logro SET id_tipo_logro=?, nombre=?, descripcion=?, institucion=?, fecha_obtencion=?, origen=?, financiado_por_kinal=?, detalle_financiacion=? WHERE id_logro=?";
        try (Connection conn = Conexion.getInstancia().conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, logro.getIdTipoLogro() != null ? logro.getIdTipoLogro() : 1L);
            stmt.setString(2, logro.getNombre());
            stmt.setString(3, logro.getDescripcion());
            stmt.setString(4, logro.getInstitucion());
            stmt.setObject(5, logro.getFechaObtencion() != null ? java.sql.Date.valueOf(logro.getFechaObtencion()) : null);
            stmt.setString(6, logro.getOrigen() != null ? logro.getOrigen() : "OTRO");
            stmt.setBoolean(7, logro.getFinanciadoPorKinal() != null ? logro.getFinanciadoPorKinal() : false);
            stmt.setString(8, logro.getDetalleFinanciacion());
            stmt.setLong(9, logro.getIdLogro());
            return stmt.executeUpdate() > 0;
        }
    }
}