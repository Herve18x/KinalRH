package org.kinalrh.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import org.kinalrh.dao.TipoLogroDAO;
import org.kinalrh.model.TipoLogro;
import org.kinalrh.util.Conexion;

/**
 * Implementación JDBC de TipoLogroDAO.
 * T3.09 Lectura/gestión básica de nivel académico y tipo de logro.
 */
public class TipoLogroDAOImpl implements TipoLogroDAO {

    @Override
    public List<TipoLogro> listarTodos() throws SQLException {
        List<TipoLogro> lista = new ArrayList<>();
        String sql = "SELECT id_tipo_logro, codigo, nombre, descripcion, activo FROM tipo_logro ORDER BY nombre ASC";
        try (Connection conn = Conexion.getInstancia().conectar();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    @Override
    public List<TipoLogro> listarActivos() {
        List<TipoLogro> lista = new ArrayList<>();
        String sql = "SELECT id_tipo_logro, codigo, nombre, descripcion, activo FROM tipo_logro WHERE activo = TRUE ORDER BY nombre ASC";
        try (Connection conn = Conexion.getInstancia().conectar();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al listar tipos de logro activos: " + e.getMessage());
        }
        return lista;
    }

    @Override
    public TipoLogro buscarPorId(Long id) {
        if (id == null) return null;
        String sql = "SELECT id_tipo_logro, codigo, nombre, descripcion, activo FROM tipo_logro WHERE id_tipo_logro = ?";
        try (Connection conn = Conexion.getInstancia().conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar tipo de logro por ID: " + e.getMessage());
        }
        return null;
    }

    @Override
    public long insertar(TipoLogro tipo) throws SQLException {
        String sql = "INSERT INTO tipo_logro (codigo, nombre, descripcion, activo) VALUES (?, ?, ?, ?)";
        try (Connection conn = Conexion.getInstancia().conectar();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, tipo.getCodigo());
            stmt.setString(2, tipo.getNombre());
            stmt.setString(3, tipo.getDescripcion());
            stmt.setBoolean(4, tipo.isActivo());
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    long idGenerado = rs.getLong(1);
                    tipo.setIdTipoLogro(idGenerado);
                    return idGenerado;
                }
            }
        }
        return 0;
    }

    @Override
    public void actualizar(TipoLogro tipo) throws SQLException {
        String sql = "UPDATE tipo_logro SET codigo = ?, nombre = ?, descripcion = ?, activo = ? WHERE id_tipo_logro = ?";
        try (Connection conn = Conexion.getInstancia().conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, tipo.getCodigo());
            stmt.setString(2, tipo.getNombre());
            stmt.setString(3, tipo.getDescripcion());
            stmt.setBoolean(4, tipo.isActivo());
            stmt.setLong(5, tipo.getIdTipoLogro());
            stmt.executeUpdate();
        }
    }

    @Override
    public void cambiarEstado(Long id, boolean activo) throws SQLException {
        String sql = "UPDATE tipo_logro SET activo = ? WHERE id_tipo_logro = ?";
        try (Connection conn = Conexion.getInstancia().conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBoolean(1, activo);
            stmt.setLong(2, id);
            stmt.executeUpdate();
        }
    }

    @Override
    public boolean existeCodigo(String codigo, Long idExcluido) {
        if (codigo == null || codigo.trim().isEmpty()) return false;
        String sql = "SELECT COUNT(*) FROM tipo_logro WHERE LOWER(codigo) = LOWER(?)"
                + (idExcluido != null ? " AND id_tipo_logro <> ?" : "");
        try (Connection conn = Conexion.getInstancia().conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, codigo.trim());
            if (idExcluido != null) {
                stmt.setLong(2, idExcluido);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al validar código único de tipo de logro: " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean existeNombre(String nombre, Long idExcluido) {
        if (nombre == null || nombre.trim().isEmpty()) return false;
        String sql = "SELECT COUNT(*) FROM tipo_logro WHERE LOWER(nombre) = LOWER(?)"
                + (idExcluido != null ? " AND id_tipo_logro <> ?" : "");
        try (Connection conn = Conexion.getInstancia().conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nombre.trim());
            if (idExcluido != null) {
                stmt.setLong(2, idExcluido);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al validar nombre único de tipo de logro: " + e.getMessage());
        }
        return false;
    }

    private TipoLogro mapear(ResultSet rs) throws SQLException {
        return new TipoLogro(
                rs.getLong("id_tipo_logro"),
                rs.getString("codigo"),
                rs.getString("nombre"),
                rs.getString("descripcion"),
                rs.getBoolean("activo")
        );
    }
}
