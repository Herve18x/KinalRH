package org.kinalrh.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.kinalrh.dao.LogroDAO;
import org.kinalrh.model.Logro;
import org.kinalrh.model.LogroEmpleadoDTO;
import org.kinalrh.util.Conexion;

public class LogroDAOImpl implements LogroDAO {
    @Override
    public long guardarConEmpleado(Logro logro, long idEmpleado, boolean destacado, String observaciones) throws SQLException {
        try (Connection conn = Conexion.getInstancia().conectar()) {
            boolean autoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                long id;
                if (logro.getIdLogro() == null || logro.getIdLogro() <= 0) {
                    id = insertar(conn, logro);
                    new EmpleadoLogroDAOImpl().insertar(conn, idEmpleado, id, destacado, observaciones);
                } else {
                    id = logro.getIdLogro();
                    if (!actualizar(conn, logro)) throw new SQLException("El logro no existe.");
                    boolean vinculado;
                    try (PreparedStatement stmt = conn.prepareStatement(
                            "SELECT 1 FROM empleado_logro WHERE id_empleado=? AND id_logro=?")) {
                        stmt.setLong(1, idEmpleado);
                        stmt.setLong(2, id);
                        try (ResultSet rs = stmt.executeQuery()) { vinculado = rs.next(); }
                    }
                    if (vinculado) {
                        try (PreparedStatement stmt = conn.prepareStatement(
                                "UPDATE empleado_logro SET destacado=?, observaciones=? WHERE id_empleado=? AND id_logro=?")) {
                            stmt.setBoolean(1, destacado);
                            stmt.setString(2, observaciones);
                            stmt.setLong(3, idEmpleado);
                            stmt.setLong(4, id);
                            stmt.executeUpdate();
                        }
                    } else {
                        new EmpleadoLogroDAOImpl().insertar(conn, idEmpleado, id, destacado, observaciones);
                    }
                }
                conn.commit();
                logro.setIdLogro(id);
                return id;
            } catch (SQLException | RuntimeException ex) {
                try { conn.rollback(); } catch (SQLException rollback) { ex.addSuppressed(rollback); }
                throw ex;
            } finally {
                conn.setAutoCommit(autoCommit);
            }
        }
    }
    @Override
    public List<Logro> listarTodos() throws SQLException {
        List<Logro> lista = new ArrayList<>();
        String sql = "SELECT id_logro, id_tipo_logro, nombre, descripcion, institucion, fecha_obtencion, origen, financiado_por_kinal, detalle_financiacion, activo, creado_en, actualizado_en FROM logro WHERE activo = TRUE ORDER BY id_logro";
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
    public List<Logro> listarPorEmpleado(long idEmpleado) throws SQLException {
        List<Logro> lista = new ArrayList<>();
        String sql = "SELECT l.id_logro, l.id_tipo_logro, l.nombre, l.descripcion, l.institucion, l.fecha_obtencion, " +
                     "l.origen, l.financiado_por_kinal, l.detalle_financiacion, l.activo, l.creado_en, l.actualizado_en " +
                     "FROM logro l " +
                     "INNER JOIN empleado_logro el ON l.id_logro = el.id_logro " +
                     "WHERE el.id_empleado = ? AND l.activo = TRUE " +
                     "ORDER BY l.fecha_obtencion DESC, l.id_logro DESC";
        try (Connection conn = Conexion.getInstancia().conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idEmpleado);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }
        return lista;
    }

    private Logro mapear(ResultSet rs) throws SQLException {
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
        l.setCreadoEn(rs.getTimestamp("creado_en").toLocalDateTime());
        l.setActualizadoEn(rs.getTimestamp("actualizado_en").toLocalDateTime());
        return l;
    }

    @Override
    public long insertar(Logro logro) throws SQLException {
        try (Connection conn = Conexion.getInstancia().conectar()) {
            return insertar(conn, logro);
        }
    }

    private long insertar(Connection conn, Logro logro) throws SQLException {
        String sql = "INSERT INTO logro (id_tipo_logro, nombre, descripcion, institucion, fecha_obtencion, origen, financiado_por_kinal, detalle_financiacion, activo) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            stmt.setObject(1, logro.getIdTipoLogro() != null ? logro.getIdTipoLogro() : 1L);
            stmt.setString(2, logro.getNombre());
            stmt.setString(3, logro.getDescripcion());
            stmt.setString(4, logro.getInstitucion());
            stmt.setObject(5, logro.getFechaObtencion() != null ? java.sql.Date.valueOf(logro.getFechaObtencion()) : null);
            stmt.setString(6, logro.getOrigen() != null ? logro.getOrigen() : "OTRO");
            stmt.setBoolean(7, logro.getFinanciadoPorKinal() != null ? logro.getFinanciadoPorKinal() : false);
            stmt.setString(8, logro.getDetalleFinanciacion());
            stmt.setBoolean(9, logro.getActivo()==null || logro.getActivo());
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) return rs.getLong(1);
            }
        }
        throw new SQLException("No se obtuvo el identificador del logro.");
    }

    @Override
    public boolean actualizar(Logro logro) throws SQLException {
        try (Connection conn = Conexion.getInstancia().conectar()) {
            return actualizar(conn, logro);
        }
    }

    private boolean actualizar(Connection conn, Logro logro) throws SQLException {
        String sql = "UPDATE logro SET id_tipo_logro=?, nombre=?, descripcion=?, institucion=?, fecha_obtencion=?, origen=?, financiado_por_kinal=?, detalle_financiacion=? WHERE id_logro=?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
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

    @Override
    public List<LogroEmpleadoDTO> listarLogrosConEmpleados() throws SQLException {
        List<LogroEmpleadoDTO> lista = new ArrayList<>();
        String sql = "SELECT l.id_logro, e.primer_nombre, e.primer_apellido, l.nombre AS logro, l.institucion, l.fecha_obtencion " +
                     "FROM logro l " +
                     "JOIN empleado_logro el ON l.id_logro = el.id_logro " +
                     "JOIN empleado e ON el.id_empleado = e.id_empleado " +
                     "WHERE l.activo = TRUE " +
                     "ORDER BY l.id_logro DESC";
        try (Connection conn = Conexion.getInstancia().conectar();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String empName = rs.getString("primer_nombre") + " " + (rs.getString("primer_apellido") != null ? rs.getString("primer_apellido") : "");
                java.time.LocalDate fecha = rs.getDate("fecha_obtencion") != null ? rs.getDate("fecha_obtencion").toLocalDate() : null;
                lista.add(new LogroEmpleadoDTO(
                    rs.getLong("id_logro"),
                    empName.trim(),
                    rs.getString("logro"),
                    rs.getString("institucion"),
                    fecha
                ));
            }
        }
        return lista;
    }

    @Override
    public List<LogroEmpleadoDTO> listarLogrosPorEmpleadoDTO(long idEmpleado) throws SQLException {
        List<LogroEmpleadoDTO> lista = new ArrayList<>();
        String sql = "SELECT l.id_logro, e.primer_nombre, e.primer_apellido, l.nombre AS logro, l.institucion, l.fecha_obtencion " +
                     "FROM logro l " +
                     "JOIN empleado_logro el ON l.id_logro = el.id_logro " +
                     "JOIN empleado e ON el.id_empleado = e.id_empleado " +
                     "WHERE l.activo = TRUE AND el.id_empleado = ? " +
                     "ORDER BY l.id_logro DESC";
        try (Connection conn = Conexion.getInstancia().conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idEmpleado);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String empName = rs.getString("primer_nombre") + " " + (rs.getString("primer_apellido") != null ? rs.getString("primer_apellido") : "");
                    java.time.LocalDate fecha = rs.getDate("fecha_obtencion") != null ? rs.getDate("fecha_obtencion").toLocalDate() : null;
                    lista.add(new LogroEmpleadoDTO(
                        rs.getLong("id_logro"),
                        empName.trim(),
                        rs.getString("logro"),
                        rs.getString("institucion"),
                        fecha
                    ));
                }
            }
        }
        return lista;
    }

    @Override
    public void asignarLogroAEmpleado(Long idLogro, Long idEmpleado) throws SQLException {
        asignarLogroAEmpleado(idLogro, idEmpleado, false, null);
    }

    @Override
    public void asignarLogroAEmpleado(Long idLogro, Long idEmpleado, boolean destacado, String observaciones) throws SQLException {
        try (Connection conn = Conexion.getInstancia().conectar()) {
            new EmpleadoLogroDAOImpl().insertar(conn, idEmpleado, idLogro, destacado, observaciones);
        }
    }
}
