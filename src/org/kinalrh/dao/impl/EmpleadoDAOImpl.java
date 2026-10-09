package org.kinalrh.dao.impl;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
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
        String sql = "SELECT e.*, a.nombre as area_nombre, p.nombre as puesto_nombre, ee.nombre as estado_nombre " +
                     "FROM empleado e " +
                     "LEFT JOIN area a ON e.id_area_principal = a.id_area " +
                     "LEFT JOIN puesto p ON e.id_puesto_actual = p.id_puesto " +
                     "LEFT JOIN estado_empleado ee ON e.id_estado_empleado = ee.id_estado_empleado";
        
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
    public Optional<Empleado> buscarPorId(long idEmpleado) throws SQLException {
        String sql = "SELECT e.*, a.nombre as area_nombre, p.nombre as puesto_nombre, ee.nombre as estado_nombre " +
                     "FROM empleado e " +
                     "LEFT JOIN area a ON e.id_area_principal = a.id_area " +
                     "LEFT JOIN puesto p ON e.id_puesto_actual = p.id_puesto " +
                     "LEFT JOIN estado_empleado ee ON e.id_estado_empleado = ee.id_estado_empleado " +
                     "WHERE e.id_empleado = ?";
        try (Connection conn = Conexion.getInstancia().conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idEmpleado);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapear(rs));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public long insertar(Empleado e) throws SQLException {
        String sql = "INSERT INTO empleado (dpi, nit, codigo_empleado, primer_nombre, segundo_nombre, tercer_nombre, " +
                     "primer_apellido, segundo_apellido, apellido_casada, nombre_completo, fecha_nacimiento, estado_civil, " +
                     "religion, direccion, zona, municipio, departamento, telefono_movil, telefono_fijo, correo_personal, " +
                     "fecha_ingreso_kinal, id_estado_empleado, id_nivel_academico, id_puesto_actual, id_area_principal, id_jefe_inmediato) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                     
        try (Connection conn = Conexion.getInstancia().conectar();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            setParametros(stmt, e);

            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) return rs.getLong(1);
            }
        }
        return 0;
    }

    @Override
    public boolean actualizar(Empleado e) throws SQLException {
        String sql = "UPDATE empleado SET dpi=?, nit=?, codigo_empleado=?, primer_nombre=?, segundo_nombre=?, tercer_nombre=?, " +
                     "primer_apellido=?, segundo_apellido=?, apellido_casada=?, nombre_completo=?, fecha_nacimiento=?, estado_civil=?, " +
                     "religion=?, direccion=?, zona=?, municipio=?, departamento=?, telefono_movil=?, telefono_fijo=?, correo_personal=?, " +
                     "fecha_ingreso_kinal=?, id_estado_empleado=?, id_nivel_academico=?, id_puesto_actual=?, id_area_principal=?, id_jefe_inmediato=? " +
                     "WHERE id_empleado = ?";
                     
        try (Connection conn = Conexion.getInstancia().conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            setParametros(stmt, e);
            stmt.setLong(27, e.getIdEmpleado());

            return stmt.executeUpdate() > 0;
        }
    }
    
    private void setParametros(PreparedStatement stmt, Empleado e) throws SQLException {
        stmt.setString(1, e.getDpi());
        stmt.setString(2, e.getNit());
        stmt.setString(3, e.getCodigoEmpleado());
        stmt.setString(4, e.getPrimerNombre());
        stmt.setString(5, e.getSegundoNombre());
        stmt.setString(6, e.getTercerNombre());
        stmt.setString(7, e.getPrimerApellido());
        stmt.setString(8, e.getSegundoApellido());
        stmt.setString(9, e.getApellidoCasada());
        
        String nombreCompleto = e.getPrimerNombre() + 
            (e.getSegundoNombre() != null ? " " + e.getSegundoNombre() : "") + 
            " " + e.getPrimerApellido() + 
            (e.getSegundoApellido() != null ? " " + e.getSegundoApellido() : "");
        stmt.setString(10, nombreCompleto);
        
        if(e.getFechaNacimiento() != null) stmt.setDate(11, Date.valueOf(e.getFechaNacimiento())); else stmt.setNull(11, Types.DATE);
        stmt.setString(12, e.getEstadoCivil());
        stmt.setString(13, e.getReligion());
        stmt.setString(14, e.getDireccion());
        stmt.setString(15, e.getZona());
        stmt.setString(16, e.getMunicipio());
        stmt.setString(17, e.getDepartamento());
        stmt.setString(18, e.getTelefonoMovil());
        stmt.setString(19, e.getTelefonoFijo());
        stmt.setString(20, e.getCorreoPersonal());
        
        if(e.getFechaIngresoKinal() != null) stmt.setDate(21, Date.valueOf(e.getFechaIngresoKinal())); else stmt.setNull(21, Types.DATE);
        
        stmt.setObject(22, e.getIdEstadoEmpleado());
        stmt.setObject(23, e.getIdNivelAcademico());
        stmt.setObject(24, e.getIdPuestoActual());
        stmt.setObject(25, e.getIdAreaPrincipal());
        stmt.setObject(26, e.getIdJefeInmediato());
    }

    private Empleado mapear(ResultSet rs) throws SQLException {
        Empleado e = new Empleado();
        e.setIdEmpleado(rs.getLong("id_empleado"));
        e.setDpi(rs.getString("dpi"));
        e.setNit(rs.getString("nit"));
        e.setCodigoEmpleado(rs.getString("codigo_empleado"));
        e.setPrimerNombre(rs.getString("primer_nombre"));
        e.setSegundoNombre(rs.getString("segundo_nombre"));
        e.setTercerNombre(rs.getString("tercer_nombre"));
        e.setPrimerApellido(rs.getString("primer_apellido"));
        e.setSegundoApellido(rs.getString("segundo_apellido"));
        e.setApellidoCasada(rs.getString("apellido_casada"));
        e.setNombreCompleto(rs.getString("nombre_completo"));
        
        Date fn = rs.getDate("fecha_nacimiento");
        if(fn != null) e.setFechaNacimiento(fn.toLocalDate());
        
        e.setEstadoCivil(rs.getString("estado_civil"));
        e.setReligion(rs.getString("religion"));
        e.setDireccion(rs.getString("direccion"));
        e.setZona(rs.getString("zona"));
        e.setMunicipio(rs.getString("municipio"));
        e.setDepartamento(rs.getString("departamento"));
        e.setTelefonoMovil(rs.getString("telefono_movil"));
        e.setTelefonoFijo(rs.getString("telefono_fijo"));
        e.setCorreoPersonal(rs.getString("correo_personal"));
        
        Date fi = rs.getDate("fecha_ingreso_kinal");
        if(fi != null) e.setFechaIngresoKinal(fi.toLocalDate());
        
        e.setIdEstadoEmpleado(rs.getObject("id_estado_empleado", Long.class));
        e.setIdNivelAcademico(rs.getObject("id_nivel_academico", Long.class));
        e.setIdPuestoActual(rs.getObject("id_puesto_actual", Long.class));
        e.setIdAreaPrincipal(rs.getObject("id_area_principal", Long.class));
        e.setIdJefeInmediato(rs.getObject("id_jefe_inmediato", Long.class));
        
        e.setArea(rs.getString("area_nombre"));
        e.setPuesto(rs.getString("puesto_nombre"));
        e.setEstado(rs.getString("estado_nombre"));
        
        return e;
    }
}