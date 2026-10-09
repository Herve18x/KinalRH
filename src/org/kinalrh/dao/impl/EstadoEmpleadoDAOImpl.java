package org.kinalrh.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import org.kinalrh.dao.EstadoEmpleadoDAO;
import org.kinalrh.model.EstadoEmpleado;
import org.kinalrh.util.Conexion;

public class EstadoEmpleadoDAOImpl implements EstadoEmpleadoDAO {

    @Override
    public List<EstadoEmpleado> listarTodos() {
        List<EstadoEmpleado> lista = new ArrayList<>();
        String sql = "SELECT id_estado_empleado, codigo, nombre, descripcion, es_estado_activo, activo, creado_en, actualizado_en " +
                     "FROM estado_empleado ORDER BY id_estado_empleado ASC";

        try (Connection conn = Conexion.getInstancia().conectar();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                lista.add(mapearEstado(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al listar estados de empleado: " + e.getMessage());
        }
        return lista;
    }

    @Override
    public EstadoEmpleado buscarPorId(Long id) {
        String sql = "SELECT id_estado_empleado, codigo, nombre, descripcion, es_estado_activo, activo, creado_en, actualizado_en " +
                     "FROM estado_empleado WHERE id_estado_empleado = ?";
        try (Connection conn = Conexion.getInstancia().conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearEstado(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar estado de empleado por id: " + e.getMessage());
        }
        return null;
    }

    @Override
    public void guardar(EstadoEmpleado estado) {
        String sql = "INSERT INTO estado_empleado (codigo, nombre, descripcion, es_estado_activo, activo) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = Conexion.getInstancia().conectar();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, estado.getCodigo());
            stmt.setString(2, estado.getNombre());
            stmt.setString(3, estado.getDescripcion());
            stmt.setBoolean(4, estado.getEsEstadoActivo() != null ? estado.getEsEstadoActivo() : false);
            stmt.setBoolean(5, estado.getActivo() != null ? estado.getActivo() : true);
            
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    estado.setIdEstadoEmpleado(rs.getLong(1));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al guardar estado de empleado: " + e.getMessage());
        }
    }

    @Override
    public void actualizar(EstadoEmpleado estado) {
        String sql = "UPDATE estado_empleado SET codigo = ?, nombre = ?, descripcion = ?, es_estado_activo = ?, activo = ? " +
                     "WHERE id_estado_empleado = ?";
        try (Connection conn = Conexion.getInstancia().conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, estado.getCodigo());
            stmt.setString(2, estado.getNombre());
            stmt.setString(3, estado.getDescripcion());
            stmt.setBoolean(4, estado.getEsEstadoActivo() != null ? estado.getEsEstadoActivo() : false);
            stmt.setBoolean(5, estado.getActivo() != null ? estado.getActivo() : true);
            stmt.setLong(6, estado.getIdEstadoEmpleado());
            
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error al actualizar estado de empleado: " + e.getMessage());
        }
    }

    @Override
    public void cambiarEstadoLogico(Long id, boolean activo) {
        String sql = "UPDATE estado_empleado SET activo = ? WHERE id_estado_empleado = ?";
        try (Connection conn = Conexion.getInstancia().conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setBoolean(1, activo);
            stmt.setLong(2, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error al cambiar estado lógico de estado_empleado: " + e.getMessage());
        }
    }

    private EstadoEmpleado mapearEstado(ResultSet rs) throws SQLException {
        EstadoEmpleado estado = new EstadoEmpleado();
        estado.setIdEstadoEmpleado(rs.getLong("id_estado_empleado"));
        estado.setCodigo(rs.getString("codigo"));
        estado.setNombre(rs.getString("nombre"));
        estado.setDescripcion(rs.getString("descripcion"));
        estado.setEsEstadoActivo(rs.getBoolean("es_estado_activo"));
        estado.setActivo(rs.getBoolean("activo"));
        estado.setCreadoEn(rs.getTimestamp("creado_en"));
        estado.setActualizadoEn(rs.getTimestamp("actualizado_en"));
        return estado;
    }
}