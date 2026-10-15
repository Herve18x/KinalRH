package org.kinalrh.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import org.kinalrh.dao.NivelAcademicoDAO;
import org.kinalrh.model.NivelAcademico;
import org.kinalrh.util.Conexion;

/**
 * Implementación JDBC para NivelAcademicoDAO.
 * T3.09 Lectura/gestión básica de nivel académico y tipo de logro.
 * T3.12 Verificar que solo se guarda un nivel válido y activo.
 */
public class NivelAcademicoDAOImpl implements NivelAcademicoDAO {

    @Override
    public List<NivelAcademico> listarTodos() throws SQLException {
        List<NivelAcademico> lista = new ArrayList<>();
        String sql = "SELECT id_nivel_academico, codigo, nombre, orden_nivel, activo FROM nivel_academico ORDER BY orden_nivel ASC, nombre ASC";
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
    public List<NivelAcademico> listarActivos() {
        List<NivelAcademico> lista = new ArrayList<>();
        String sql = "SELECT id_nivel_academico, codigo, nombre, orden_nivel, activo FROM nivel_academico WHERE activo = TRUE ORDER BY orden_nivel ASC, nombre ASC";
        try (Connection conn = Conexion.getInstancia().conectar();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al listar niveles académicos activos: " + e.getMessage());
        }
        return lista;
    }

    @Override
    public NivelAcademico buscarPorId(Long id) {
        if (id == null) return null;
        String sql = "SELECT id_nivel_academico, codigo, nombre, orden_nivel, activo FROM nivel_academico WHERE id_nivel_academico = ?";
        try (Connection conn = Conexion.getInstancia().conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar nivel académico por ID: " + e.getMessage());
        }
        return null;
    }

    @Override
    public long insertar(NivelAcademico nivel) throws SQLException {
        String sql = "INSERT INTO nivel_academico (codigo, nombre, orden_nivel, activo) VALUES (?, ?, ?, ?)";
        try (Connection conn = Conexion.getInstancia().conectar();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, nivel.getCodigo());
            stmt.setString(2, nivel.getNombre());
            stmt.setInt(3, nivel.getOrdenNivel());
            stmt.setBoolean(4, nivel.isActivo());
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    long idGenerado = rs.getLong(1);
                    nivel.setIdNivelAcademico(idGenerado);
                    return idGenerado;
                }
            }
        }
        return 0;
    }

    @Override
    public void actualizar(NivelAcademico nivel) throws SQLException {
        String sql = "UPDATE nivel_academico SET codigo = ?, nombre = ?, orden_nivel = ?, activo = ? WHERE id_nivel_academico = ?";
        try (Connection conn = Conexion.getInstancia().conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nivel.getCodigo());
            stmt.setString(2, nivel.getNombre());
            stmt.setInt(3, nivel.getOrdenNivel());
            stmt.setBoolean(4, nivel.isActivo());
            stmt.setLong(5, nivel.getIdNivelAcademico());
            stmt.executeUpdate();
        }
    }

    @Override
    public void cambiarEstado(Long id, boolean activo) throws SQLException {
        String sql = "UPDATE nivel_academico SET activo = ? WHERE id_nivel_academico = ?";
        try (Connection conn = Conexion.getInstancia().conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBoolean(1, activo);
            stmt.setLong(2, id);
            stmt.executeUpdate();
        }
    }

    @Override
    public boolean esNivelValidoYActivo(Long id) {
        if (id == null || id <= 0) return false;
        String sql = "SELECT 1 FROM nivel_academico WHERE id_nivel_academico = ? AND activo = TRUE";
        try (Connection conn = Conexion.getInstancia().conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("Error al validar nivel académico activo: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean existeCodigo(String codigo, Long idExcluido) {
        if (codigo == null || codigo.trim().isEmpty()) return false;
        String sql = "SELECT COUNT(*) FROM nivel_academico WHERE LOWER(codigo) = LOWER(?)"
                + (idExcluido != null ? " AND id_nivel_academico <> ?" : "");
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
            System.err.println("Error al validar código único de nivel académico: " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean existeNombre(String nombre, Long idExcluido) {
        if (nombre == null || nombre.trim().isEmpty()) return false;
        String sql = "SELECT COUNT(*) FROM nivel_academico WHERE LOWER(nombre) = LOWER(?)"
                + (idExcluido != null ? " AND id_nivel_academico <> ?" : "");
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
            System.err.println("Error al validar nombre único de nivel académico: " + e.getMessage());
        }
        return false;
    }

    private NivelAcademico mapear(ResultSet rs) throws SQLException {
        return new NivelAcademico(
                rs.getLong("id_nivel_academico"),
                rs.getString("codigo"),
                rs.getString("nombre"),
                rs.getInt("orden_nivel"),
                rs.getBoolean("activo")
        );
    }
}