package org.kinalrh.dao.impl;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.Optional;
import javax.sql.DataSource;
import org.kinalrh.dao.EmpleadoDAO;
import org.kinalrh.model.Empleado;
import org.kinalrh.util.Conexion;

public class EmpleadoDAOImpl implements EmpleadoDAO {

    private final DataSource dataSource;

    public EmpleadoDAOImpl() {
        this(null);
    }

    public EmpleadoDAOImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private Connection conectar() throws SQLException {
        return dataSource == null ? Conexion.getInstancia().conectar() : dataSource.getConnection();
    }

    @Override
    public List<Empleado> listarPaginado(int tamanioPagina, int desplazamiento) throws SQLException {
        return listarPagina(tamanioPagina, desplazamiento, false);
    }

    @Override
    public List<Empleado> listarActivosPaginado(int tamanioPagina, int desplazamiento) throws SQLException {
        return listarPagina(tamanioPagina, desplazamiento, true);
    }

    private List<Empleado> listarPagina(int tamanioPagina, int desplazamiento, boolean soloActivos) throws SQLException {
        if (tamanioPagina < 1 || tamanioPagina > 25 || desplazamiento < 0) {
            throw new IllegalArgumentException("El tamaño de página debe estar entre 1 y 25 y el desplazamiento no puede ser negativo.");
        }
        String sql = "SELECT e.id_empleado, e.primer_nombre, e.primer_apellido, e.nombre_completo, " +
                     "e.id_estado_empleado, ee.nombre AS estado_nombre, " +
                     "e.id_area_principal, a.nombre AS area_nombre, " +
                     "e.id_puesto_actual, p.nombre AS puesto_nombre " +
                     "FROM empleado e " +
                     "JOIN estado_empleado ee ON ee.id_estado_empleado = e.id_estado_empleado " +
                     "LEFT JOIN area a ON a.id_area = e.id_area_principal " +
                     "LEFT JOIN puesto p ON p.id_puesto = e.id_puesto_actual " +
                     (soloActivos ? "WHERE ee.es_estado_activo = TRUE " : "") +
                     "ORDER BY id_empleado ASC LIMIT ? OFFSET ?";
        List<Empleado> empleados = new ArrayList<>();
        try (Connection conn = conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, tamanioPagina);
            stmt.setInt(2, desplazamiento);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Empleado empleado = new Empleado();
                    empleado.setIdEmpleado(rs.getLong("id_empleado"));
                    empleado.setPrimerNombre(rs.getString("primer_nombre"));
                    empleado.setPrimerApellido(rs.getString("primer_apellido"));
                    empleado.setNombreCompleto(rs.getString("nombre_completo"));
                    empleado.setIdEstadoEmpleado(rs.getLong("id_estado_empleado"));
                    empleado.setEstado(rs.getString("estado_nombre"));
                    empleado.setIdAreaPrincipal(rs.getObject("id_area_principal", Long.class));
                    empleado.setArea(rs.getString("area_nombre"));
                    empleado.setIdPuestoActual(rs.getObject("id_puesto_actual", Long.class));
                    empleado.setPuesto(rs.getString("puesto_nombre"));
                    empleados.add(empleado);
                }
            }
        }
        return empleados;
    }

    @Override
    public boolean retirar(long idEmpleado) throws SQLException {
        validarId(idEmpleado);
        try (Connection conn = conectar()) {
            long idRetirado;
            String estadoSql = "SELECT id_estado_empleado FROM estado_empleado " +
                               "WHERE codigo = ? AND activo = TRUE AND es_estado_activo = FALSE";
            try (PreparedStatement stmt = conn.prepareStatement(estadoSql)) {
                stmt.setString(1, "RETIRADO");
                try (ResultSet rs = stmt.executeQuery()) {
                    if (!rs.next()) {
                        throw new IllegalStateException("No existe un estado RETIRADO habilitado y no activo laboralmente.");
                    }
                    idRetirado = rs.getLong("id_estado_empleado");
                }
            }
            try (PreparedStatement stmt = conn.prepareStatement(
                    "UPDATE empleado SET id_estado_empleado = ? WHERE id_empleado = ?")) {
                stmt.setLong(1, idRetirado);
                stmt.setLong(2, idEmpleado);
                return stmt.executeUpdate() > 0 || existeEmpleado(conn, idEmpleado);
            }
        }
    }

    private void validarId(long idEmpleado) {
        if (idEmpleado <= 0) {
            throw new IllegalArgumentException("El identificador del empleado debe ser positivo.");
        }
    }

    private boolean existeEmpleado(Connection conn, long idEmpleado) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement("SELECT id_empleado FROM empleado WHERE id_empleado = ?")) {
            stmt.setLong(1, idEmpleado);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    @Override
    public List<Empleado> listarTodos() throws SQLException {
        List<Empleado> lista = new ArrayList<>();
        String sql = "SELECT e.*, a.nombre as area_nombre, p.nombre as puesto_nombre, ee.nombre as estado_nombre, " +
                     "na.nombre as nivel_academico_nombre, j.nombre_completo as jefe_nombre " +
                     "FROM empleado e " +
                     "LEFT JOIN area a ON e.id_area_principal = a.id_area " +
                     "LEFT JOIN puesto p ON e.id_puesto_actual = p.id_puesto " +
                     "LEFT JOIN estado_empleado ee ON e.id_estado_empleado = ee.id_estado_empleado " +
                     "LEFT JOIN nivel_academico na ON e.id_nivel_academico = na.id_nivel_academico " +
                     "LEFT JOIN empleado j ON e.id_jefe_inmediato = j.id_empleado";
        
        try (Connection conn = conectar();
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
        validarId(idEmpleado);
        String sql = "SELECT e.*, a.nombre as area_nombre, p.nombre as puesto_nombre, ee.nombre as estado_nombre, " +
                     "na.nombre as nivel_academico_nombre, j.nombre_completo as jefe_nombre " +
                     "FROM empleado e " +
                     "LEFT JOIN area a ON e.id_area_principal = a.id_area " +
                     "LEFT JOIN puesto p ON e.id_puesto_actual = p.id_puesto " +
                     "LEFT JOIN estado_empleado ee ON e.id_estado_empleado = ee.id_estado_empleado " +
                     "LEFT JOIN nivel_academico na ON e.id_nivel_academico = na.id_nivel_academico " +
                     "LEFT JOIN empleado j ON e.id_jefe_inmediato = j.id_empleado " +
                     "WHERE e.id_empleado = ?";
        try (Connection conn = conectar();
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
    public long insertar(Empleado empleado) throws SQLException {
        validarEmpleado(empleado);
        Map<String, Object> valores = valoresEmpleado(empleado, true);
        String nombreCompleto = Stream.of(empleado.getPrimerNombre(), empleado.getSegundoNombre(),
                empleado.getTercerNombre(), empleado.getPrimerApellido(), empleado.getSegundoApellido(),
                empleado.getApellidoCasada())
                .filter(nombre -> nombre != null && !nombre.isBlank())
                .map(String::trim).collect(Collectors.joining(" "));
        valores.put("nombre_completo", nombreCompleto);
        String sql = "INSERT INTO empleado (" + String.join(", ", valores.keySet()) + ") VALUES (" +
                valores.keySet().stream().map(columna -> "?").collect(Collectors.joining(", ")) + ")";
        try (Connection conn = conectar()) {
            conn.setAutoCommit(false);
            try {
                validarEstado(conn, empleado.getIdEstadoEmpleado());
                long idGenerado;
                try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    asignarParametros(stmt, valores);
                    if (stmt.executeUpdate() != 1) {
                        throw new SQLException("No se pudo insertar el empleado.");
                    }
                    try (ResultSet rs = stmt.getGeneratedKeys()) {
                        if (!rs.next() || (idGenerado = rs.getLong(1)) <= 0) {
                            throw new SQLException("No se obtuvo el identificador del empleado insertado.");
                        }
                    }
                }
                sincronizarAreaPrincipal(conn, idGenerado, empleado.getIdAreaPrincipal());
                conn.commit();
                empleado.setNombreCompleto(nombreCompleto);
                return idGenerado;
            } catch (SQLException | RuntimeException e) {
                revertir(conn, e);
                throw e;
            }
        } catch (SQLException e) {
            throw controlarDuplicadoDpi(e);
        }
    }

    @Override
    public boolean actualizar(Empleado empleado) throws SQLException {
        validarEmpleado(empleado);
        if (empleado.getIdEmpleado() == null) {
            throw new IllegalArgumentException("El identificador del empleado es obligatorio.");
        }
        validarId(empleado.getIdEmpleado());
        Map<String, Object> valores = valoresEmpleado(empleado, false);
        String asignaciones = valores.keySet().stream()
                .map(columna -> columna + " = ?").collect(Collectors.joining(", "));
        String nombreSql = "CONCAT_WS(' ', NULLIF(TRIM(primer_nombre), ''), NULLIF(TRIM(segundo_nombre), ''), " +
                "NULLIF(TRIM(tercer_nombre), ''), NULLIF(TRIM(primer_apellido), ''), " +
                "NULLIF(TRIM(segundo_apellido), ''), NULLIF(TRIM(apellido_casada), ''))";
        String sql = "UPDATE empleado SET " + asignaciones + ", nombre_completo = " + nombreSql +
                " WHERE id_empleado = ?";
        try (Connection conn = conectar()) {
            conn.setAutoCommit(false);
            try {
                validarEstado(conn, empleado.getIdEstadoEmpleado());
                boolean existe;
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    asignarParametros(stmt, valores);
                    stmt.setLong(valores.size() + 1, empleado.getIdEmpleado());
                    existe = stmt.executeUpdate() > 0 || existeEmpleado(conn, empleado.getIdEmpleado());
                }
                if (existe && empleado.tieneCampoOpcional("idAreaPrincipal")) {
                    sincronizarAreaPrincipal(conn, empleado.getIdEmpleado(), empleado.getIdAreaPrincipal());
                }
                conn.commit();
                return existe;
            } catch (SQLException | RuntimeException e) {
                revertir(conn, e);
                throw e;
            }
        } catch (SQLException e) {
            throw controlarDuplicadoDpi(e);
        }
    }

    private void validarEmpleado(Empleado empleado) {
        if (empleado == null || empleado.getDpi() == null || empleado.getDpi().isBlank()
                || empleado.getPrimerNombre() == null || empleado.getPrimerNombre().isBlank()
                || empleado.getPrimerApellido() == null || empleado.getPrimerApellido().isBlank()
                || empleado.getIdEstadoEmpleado() == null || empleado.getIdEstadoEmpleado() <= 0) {
            throw new IllegalArgumentException("DPI, primer nombre, primer apellido y estado laboral válido son obligatorios.");
        }
    }

    private void validarEstado(Connection conn, long idEstado) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT id_estado_empleado FROM estado_empleado WHERE id_estado_empleado = ?")) {
            stmt.setLong(1, idEstado);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalArgumentException("El estado laboral no existe.");
                }
            }
        }
    }

    private SQLException controlarDuplicadoDpi(SQLException error) {
        for (SQLException actual = error; actual != null; actual = actual.getNextException()) {
            if (actual.getErrorCode() == 1062 && actual.getMessage() != null
                    && actual.getMessage().contains("uq_empleado_dpi")) {
                throw new IllegalArgumentException("Ya existe un empleado registrado con ese DPI.", error);
            }
        }
        return error;
    }

    private void revertir(Connection conn, Exception error) {
        try {
            conn.rollback();
        } catch (SQLException rollbackError) {
            error.addSuppressed(rollbackError);
        }
    }

    @Override
    public boolean actualizarNivelAcademico(long idEmpleado, Long idNivelAcademico) throws SQLException {
        String sql = "UPDATE empleado SET id_nivel_academico = ? WHERE id_empleado = ?";
        try (Connection conn = conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, idNivelAcademico);
            stmt.setLong(2, idEmpleado);
            return stmt.executeUpdate() > 0;
        }
    }

    private void sincronizarAreaPrincipal(Connection conn, long idEmpleado, Long idAreaPrincipal) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(
                "UPDATE empleado_area SET es_principal = FALSE WHERE id_empleado = ?")) {
            stmt.setLong(1, idEmpleado);
            stmt.executeUpdate();
        }
        if (idAreaPrincipal == null) return;
        String sql = "INSERT INTO empleado_area (id_empleado, id_area, es_principal, fecha_inicio) " +
                "VALUES (?, ?, TRUE, CURRENT_DATE) ON DUPLICATE KEY UPDATE es_principal = TRUE";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idEmpleado);
            stmt.setLong(2, idAreaPrincipal);
            stmt.executeUpdate();
        }
    }

    private Map<String, Object> valoresEmpleado(Empleado e, boolean alta) {
        Map<String, Object> valores = new LinkedHashMap<>();
        valores.put("dpi", e.getDpi());
        valores.put("primer_nombre", e.getPrimerNombre());
        valores.put("primer_apellido", e.getPrimerApellido());
        valores.put("id_estado_empleado", e.getIdEstadoEmpleado());
        agregarOpcional(valores, e, alta, "nit", "nit", e.getNit());
        agregarOpcional(valores, e, alta, "codigo_empleado", "codigoEmpleado", e.getCodigoEmpleado());
        agregarOpcional(valores, e, alta, "segundo_nombre", "segundoNombre", e.getSegundoNombre());
        agregarOpcional(valores, e, alta, "tercer_nombre", "tercerNombre", e.getTercerNombre());
        agregarOpcional(valores, e, alta, "segundo_apellido", "segundoApellido", e.getSegundoApellido());
        agregarOpcional(valores, e, alta, "apellido_casada", "apellidoCasada", e.getApellidoCasada());
        agregarOpcional(valores, e, alta, "fecha_nacimiento", "fechaNacimiento", e.getFechaNacimiento());
        agregarOpcional(valores, e, alta, "estado_civil", "estadoCivil", e.getEstadoCivil());
        agregarOpcional(valores, e, alta, "religion", "religion", e.getReligion());
        agregarOpcional(valores, e, alta, "direccion", "direccion", e.getDireccion());
        agregarOpcional(valores, e, alta, "zona", "zona", e.getZona());
        agregarOpcional(valores, e, alta, "municipio", "municipio", e.getMunicipio());
        agregarOpcional(valores, e, alta, "departamento", "departamento", e.getDepartamento());
        agregarOpcional(valores, e, alta, "telefono_movil", "telefonoMovil", e.getTelefonoMovil());
        agregarOpcional(valores, e, alta, "telefono_fijo", "telefonoFijo", e.getTelefonoFijo());
        agregarOpcional(valores, e, alta, "correo_personal", "correoPersonal", e.getCorreoPersonal());
        agregarOpcional(valores, e, alta, "fecha_ingreso_kinal", "fechaIngresoKinal", e.getFechaIngresoKinal());
        agregarOpcional(valores, e, alta, "id_nivel_academico", "idNivelAcademico", e.getIdNivelAcademico());
        agregarOpcional(valores, e, alta, "id_puesto_actual", "idPuestoActual", e.getIdPuestoActual());
        agregarOpcional(valores, e, alta, "id_area_principal", "idAreaPrincipal", e.getIdAreaPrincipal());
        agregarOpcional(valores, e, alta, "id_jefe_inmediato", "idJefeInmediato", e.getIdJefeInmediato());
        return valores;
    }

    private void agregarOpcional(Map<String, Object> valores, Empleado empleado, boolean alta,
            String columna, String propiedad, Object valor) {
        if (alta || empleado.tieneCampoOpcional(propiedad)) {
            if (valor instanceof java.time.LocalDate fecha) {
                valor = Date.valueOf(fecha);
            } else if (("nit".equals(columna) || "codigo_empleado".equals(columna))
                    && valor instanceof String texto) {
                valor = texto.trim().isEmpty() ? null : texto.trim();
            }
            valores.put(columna, valor);
        }
    }

    private void asignarParametros(PreparedStatement stmt, Map<String, Object> valores) throws SQLException {
        int indice = 1;
        for (Object valor : valores.values()) {
            stmt.setObject(indice++, valor);
        }
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
        e.setNivelAcademico(rs.getString("nivel_academico_nombre"));
        e.setJefeInmediato(rs.getString("jefe_nombre"));
        
        return e;
    }

    @Override
    public boolean existeDpi(String dpi, Long idExcluido) {
        String sql = "SELECT 1 FROM empleado WHERE dpi = ?";
        if (idExcluido != null) sql += " AND id_empleado != ?";
        try (Connection conn = conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, dpi);
            if (idExcluido != null) stmt.setLong(2, idExcluido);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("Error al validar DPI: " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean existeNit(String nit, Long idExcluido) {
        String sql = "SELECT 1 FROM empleado WHERE nit = ?";
        if (idExcluido != null) sql += " AND id_empleado != ?";
        try (Connection conn = conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nit);
            if (idExcluido != null) stmt.setLong(2, idExcluido);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("Error al validar NIT: " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean existeCorreoPersonal(String correo, Long idExcluido) {
        String sql = "SELECT 1 FROM empleado WHERE correo_personal = ?";
        if (idExcluido != null) sql += " AND id_empleado != ?";
        try (Connection conn = conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, correo);
            if (idExcluido != null) stmt.setLong(2, idExcluido);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("Error al validar correo: " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean existeReferencia(String tabla, String pkColumna, Long id) {
        // Evitamos inyección validando nombres de tabla permitidos
        if (!tabla.equals("area") && !tabla.equals("puesto") && !tabla.equals("estado_empleado")
                && !tabla.equals("nivel_academico") && !tabla.equals("empleado")) {
            return false;
        }
        String sql = "SELECT 1 FROM " + tabla + " WHERE " + pkColumna + " = ?";
        try (Connection conn = conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("Error al validar referencia " + tabla + ": " + e.getMessage());
        }
        return false;
    }
}
