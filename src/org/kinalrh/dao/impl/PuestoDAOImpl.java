package org.kinalrh.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import org.kinalrh.dao.PuestoDAO;
import org.kinalrh.model.Puesto;
import org.kinalrh.util.Conexion;
import org.kinalrh.util.DuplicateRecordException;
import org.kinalrh.util.IntegrityViolationException;

/**
 * Implementación JDBC de PuestoDAO con consultas parametrizadas (PreparedStatement).
 * Maneja de forma controlada las violaciones de unicidad y las restricciones referenciales.
 */
public class PuestoDAOImpl implements PuestoDAO {

    @Override
    public List<Puesto> listarActivos() {
        String sql = "SELECT id_puesto, codigo, nombre, descripcion, activo, creado_en, actualizado_en "
                   + "FROM puesto WHERE activo = TRUE ORDER BY nombre ASC";
        List<Puesto> lista = new ArrayList<>();

        try (Connection conn = Conexion.getInstancia().conectar();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                lista.add(mapearPuesto(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al listar puestos activos: " + e.getMessage());
        }
        return lista;
    }

    @Override
    public Puesto buscarPorId(Long idPuesto) {
        String sql = "SELECT id_puesto, codigo, nombre, descripcion, activo, creado_en, actualizado_en "
                   + "FROM puesto WHERE id_puesto = ?";

        try (Connection conn = Conexion.getInstancia().conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, idPuesto);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearPuesto(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar puesto por ID: " + e.getMessage());
        }
        return null;
    }

    @Override
    public boolean crear(Puesto puesto) {
        String sql = "INSERT INTO puesto (codigo, nombre, descripcion, activo) VALUES (?, ?, ?, ?)";

        try (Connection conn = Conexion.getInstancia().conectar();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, puesto.getCodigo());
            stmt.setString(2, puesto.getNombre());
            stmt.setString(3, puesto.getDescripcion());
            stmt.setBoolean(4, puesto.isActivo());

            int filasAfectadas = stmt.executeUpdate();
            if (filasAfectadas > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        puesto.setIdPuesto(generatedKeys.getLong(1));
                    }
                }
                return true;
            }
            return false;

        } catch (SQLException e) {
            gestionarConflictoUnicidad(e, puesto.getCodigo(), puesto.getNombre());
            System.err.println("Error al crear puesto: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean actualizar(Puesto puesto) {
        String sql = "UPDATE puesto SET codigo = ?, nombre = ?, descripcion = ?, activo = ? WHERE id_puesto = ?";

        try (Connection conn = Conexion.getInstancia().conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, puesto.getCodigo());
            stmt.setString(2, puesto.getNombre());
            stmt.setString(3, puesto.getDescripcion());
            stmt.setBoolean(4, puesto.isActivo());
            stmt.setLong(5, puesto.getIdPuesto());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            gestionarConflictoUnicidad(e, puesto.getCodigo(), puesto.getNombre());
            System.err.println("Error al actualizar puesto: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean tieneEmpleadosAsociados(Long idPuesto) {
        String sql = "SELECT COUNT(*) AS total FROM empleado WHERE id_puesto_actual = ?";

        try (Connection conn = Conexion.getInstancia().conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, idPuesto);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total") > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al verificar colaboradores asociados al puesto: " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean eliminar(Long idPuesto) {
        if (tieneEmpleadosAsociados(idPuesto)) {
            throw new IntegrityViolationException(
                "No se puede eliminar físicamente el puesto con ID " + idPuesto + " porque tiene colaboradores asignados. Considere desactivarlo."
            );
        }

        String sql = "DELETE FROM puesto WHERE id_puesto = ?";
        try (Connection conn = Conexion.getInstancia().conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, idPuesto);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al eliminar puesto: " + e.getMessage());
            return false;
        }
    }

    /**
     * Captura y convierte los errores de duplicados (Error 1062) en excepciones de negocio controladas.
     */
    private void gestionarConflictoUnicidad(SQLException e, String codigo, String nombre) {
        if (e.getErrorCode() == 1062 || (e.getMessage() != null && e.getMessage().contains("Duplicate entry"))) {
            String msg = e.getMessage().toLowerCase();
            if (msg.contains("uq_puesto_codigo") || msg.contains("codigo")) {
                throw new DuplicateRecordException("Conflicto de unicidad: El código de puesto '" + codigo + "' ya se encuentra registrado.", e);
            }
            if (msg.contains("uq_puesto_nombre") || msg.contains("nombre")) {
                throw new DuplicateRecordException("Conflicto de unicidad: El nombre de puesto '" + nombre + "' ya se encuentra registrado.", e);
            }
            throw new DuplicateRecordException("Conflicto de unicidad al registrar o actualizar el puesto.", e);
        }
    }

    private Puesto mapearPuesto(ResultSet rs) throws SQLException {
        return new Puesto(
            rs.getLong("id_puesto"),
            rs.getString("codigo"),
            rs.getString("nombre"),
            rs.getString("descripcion"),
            rs.getBoolean("activo"),
            rs.getTimestamp("creado_en"),
            rs.getTimestamp("actualizado_en")
        );
    }
}
