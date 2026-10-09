package org.kinalrh.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.kinalrh.dao.EmpleadoDAO;
import org.kinalrh.model.Empleado;
import org.kinalrh.util.Conexion;

public class EmpleadoDAOImpl implements EmpleadoDAO {

    @Override
    public List<Empleado> listarTodos() throws SQLException {
        List<Empleado> lista = new ArrayList<>();
        // Hacemos JOIN para la UI
        String sql = "SELECT e.id_empleado, e.dpi, e.primer_nombre, e.primer_apellido, e.id_area_principal, e.id_puesto_actual, e.id_estado_empleado, " +
                     "a.nombre as area_nombre, p.nombre as puesto_nombre, ee.nombre as estado_nombre " +
                     "FROM empleado e " +
                     "LEFT JOIN area a ON e.id_area_principal = a.id_area " +
                     "LEFT JOIN puesto p ON e.id_puesto_actual = p.id_puesto " +
                     "LEFT JOIN estado_empleado ee ON e.id_estado_empleado = ee.id_estado_empleado";
        
        try (Connection conn = Conexion.getInstancia().conectar();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Empleado emp = new Empleado();
                emp.setIdEmpleado(rs.getLong("id_empleado"));
                emp.setDpi(rs.getString("dpi"));
                emp.setPrimerNombre(rs.getString("primer_nombre"));
                emp.setPrimerApellido(rs.getString("primer_apellido"));
                emp.setIdAreaPrincipal(rs.getObject("id_area_principal", Long.class));
                emp.setIdPuestoActual(rs.getObject("id_puesto_actual", Long.class));
                emp.setIdEstadoEmpleado(rs.getObject("id_estado_empleado", Long.class));
                
                emp.setArea(rs.getString("area_nombre"));
                emp.setPuesto(rs.getString("puesto_nombre"));
                emp.setEstado(rs.getString("estado_nombre"));
                
                lista.add(emp);
            }
        }
        return lista;
    }

    @Override
    public Optional<Empleado> buscarPorId(long idEmpleado) throws SQLException {
        String sql = "SELECT id_empleado, dpi, primer_nombre, primer_apellido, id_area_principal, id_puesto_actual, id_estado_empleado " +
                     "FROM empleado WHERE id_empleado = ?";
        try (Connection conn = Conexion.getInstancia().conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idEmpleado);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Empleado emp = new Empleado();
                    emp.setIdEmpleado(rs.getLong("id_empleado"));
                    emp.setDpi(rs.getString("dpi"));
                    emp.setPrimerNombre(rs.getString("primer_nombre"));
                    emp.setPrimerApellido(rs.getString("primer_apellido"));
                    emp.setIdAreaPrincipal(rs.getObject("id_area_principal", Long.class));
                    emp.setIdPuestoActual(rs.getObject("id_puesto_actual", Long.class));
                    emp.setIdEstadoEmpleado(rs.getObject("id_estado_empleado", Long.class));
                    return Optional.of(emp);
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public long insertar(Empleado empleado) throws SQLException {
        String sql = "INSERT INTO empleado (dpi, primer_nombre, primer_apellido, nombre_completo, id_area_principal, id_puesto_actual, id_estado_empleado) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = Conexion.getInstancia().conectar();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, empleado.getDpi());
            stmt.setString(2, empleado.getPrimerNombre());
            stmt.setString(3, empleado.getPrimerApellido());
            stmt.setString(4, empleado.getPrimerNombre() + " " + empleado.getPrimerApellido());
            
            stmt.setObject(5, empleado.getIdAreaPrincipal());
            stmt.setObject(6, empleado.getIdPuestoActual());
            stmt.setObject(7, empleado.getIdEstadoEmpleado());

            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }
        return 0;
    }

    @Override
    public boolean actualizar(Empleado empleado) throws SQLException {
        String sql = "UPDATE empleado SET dpi = ?, primer_nombre = ?, primer_apellido = ?, nombre_completo = ?, " +
                     "id_area_principal = ?, id_puesto_actual = ?, id_estado_empleado = ? WHERE id_empleado = ?";
        try (Connection conn = Conexion.getInstancia().conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, empleado.getDpi());
            stmt.setString(2, empleado.getPrimerNombre());
            stmt.setString(3, empleado.getPrimerApellido());
            stmt.setString(4, empleado.getPrimerNombre() + " " + empleado.getPrimerApellido());
            
            stmt.setObject(5, empleado.getIdAreaPrincipal());
            stmt.setObject(6, empleado.getIdPuestoActual());
            stmt.setObject(7, empleado.getIdEstadoEmpleado());
            stmt.setLong(8, empleado.getIdEmpleado());

            return stmt.executeUpdate() > 0;
        }
    }
}