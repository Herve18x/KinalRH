package org.kinalrh.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import org.kinalrh.dao.AreaDAO;
import org.kinalrh.model.Area;
import org.kinalrh.util.Conexion;
import org.kinalrh.util.DuplicateRecordException;
import org.kinalrh.util.IntegrityViolationException;

/**
 * Implementación JDBC de AreaDAO con consultas parametrizadas (PreparedStatement).
 * Maneja de forma controlada las violaciones de unicidad y las restricciones referenciales.
 */
public class AreaDAOImpl implements AreaDAO {

    @Override
    public List<Area> listarActivos() {
        String sql = "SELECT id_area, codigo, nombre, descripcion, activo, creado_en, actualizado_en "
                   + "FROM area WHERE activo = TRUE ORDER BY nombre ASC";
        List<Area> lista = new ArrayList<>();

        try (Connection conn = Conexion.getInstancia().conectar();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                lista.add(mapearArea(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al listar áreas activas: " + e.getMessage());
        }
        return lista;
    }

    @Override
    public Area buscarPorId(Long idArea) {
        String sql = "SELECT id_area, codigo, nombre, descripcion, activo, creado_en, actualizado_en "
                   + "FROM area WHERE id_area = ?";

        try (Connection conn = Conexion.getInstancia().conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, idArea);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearArea(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar área por ID: " + e.getMessage());
        }
        return null;
    }

    @Override
    public boolean crear(Area area) {
        String sql = "INSERT INTO area (codigo, nombre, descripcion, activo) VALUES (?, ?, ?, ?)";

        try (Connection conn = Conexion.getInstancia().conectar();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, area.getCodigo());
            stmt.setString(2, area.getNombre());
            stmt.setString(3, area.getDescripcion());
            stmt.setBoolean(4, area.isActivo());

            int filasAfectadas = stmt.executeUpdate();
            if (filasAfectadas > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        area.setIdArea(generatedKeys.getLong(1));
                    }
                }
                return true;
            }
            return false;

        } catch (SQLException e) {
            gestionarConflictoUnicidad(e, area.getCodigo(), area.getNombre());
            System.err.println("Error al crear área: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean actualizar(Area area) {
        String sql = "UPDATE area SET codigo = ?, nombre = ?, descripcion = ?, activo = ? WHERE id_area = ?";

        try (Connection conn = Conexion.getInstancia().conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, area.getCodigo());
            stmt.setString(2, area.getNombre());
            stmt.setString(3, area.getDescripcion());
            stmt.setBoolean(4, area.isActivo());
            stmt.setLong(5, area.getIdArea());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            gestionarConflictoUnicidad(e, area.getCodigo(), area.getNombre());
            System.err.println("Error al actualizar área: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean tieneEmpleadosAsociados(Long idArea) {
        String sql = "SELECT "
                   + "(SELECT COUNT(*) FROM empleado WHERE id_area_principal = ?) + "
                   + "(SELECT COUNT(*) FROM empleado_area WHERE id_area = ?) AS total";

        try (Connection conn = Conexion.getInstancia().conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, idArea);
            stmt.setLong(2, idArea);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total") > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al verificar colaboradores asociados al área: " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean eliminar(Long idArea) {
        if (tieneEmpleadosAsociados(idArea)) {
            throw new IntegrityViolationException(
                "No se puede eliminar físicamente el área con ID " + idArea + " porque tiene colaboradores asignados. Considere desactivarla."
            );
        }

        String sql = "DELETE FROM area WHERE id_area = ?";
        try (Connection conn = Conexion.getInstancia().conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, idArea);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al eliminar área: " + e.getMessage());
            return false;
        }
    }

    /**
     * Captura y convierte los errores de duplicados (Error 1062) en excepciones de negocio controladas.
     */
    private void gestionarConflictoUnicidad(SQLException e, String codigo, String nombre) {
        if (e.getErrorCode() == 1062 || (e.getMessage() != null && e.getMessage().contains("Duplicate entry"))) {
            String msg = e.getMessage().toLowerCase();
            if (msg.contains("uq_area_codigo") || msg.contains("codigo")) {
                throw new DuplicateRecordException("Conflicto de unicidad: El código de área '" + codigo + "' ya se encuentra registrado.", e);
            }
            if (msg.contains("uq_area_nombre") || msg.contains("nombre")) {
                throw new DuplicateRecordException("Conflicto de unicidad: El nombre de área '" + nombre + "' ya se encuentra registrado.", e);
            }
            throw new DuplicateRecordException("Conflicto de unicidad al registrar o actualizar el área.", e);
        }
    }

    private Area mapearArea(ResultSet rs) throws SQLException {
        return new Area(
            rs.getLong("id_area"),
            rs.getString("codigo"),
            rs.getString("nombre"),
            rs.getString("descripcion"),
            rs.getBoolean("activo"),
            rs.getTimestamp("creado_en"),
            rs.getTimestamp("actualizado_en")
        );
    }
}
