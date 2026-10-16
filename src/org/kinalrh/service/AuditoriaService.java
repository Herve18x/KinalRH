package org.kinalrh.service;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import org.kinalrh.dao.AuditoriaDAO;
import org.kinalrh.dao.impl.AuditoriaDAOImpl;
import org.kinalrh.model.Auditoria;
import org.kinalrh.model.Usuario;
import org.kinalrh.util.SessionManager;

/**
 * Servicio de auditoría para trazabilidad de operaciones.
 * T1.23, T3.18 Registrar alta y edición de empleado con autor y fecha.
 */
public class AuditoriaService {
    public void registrarExportacion(java.sql.Connection conn,org.kinalrh.model.ExportacionReporte exportacion) throws SQLException {
        if(exportacion.idExportacion()==null) throw new IllegalArgumentException("La exportación debe tener un identificador.");
        try(java.sql.PreparedStatement s=conn.prepareStatement(
                "INSERT INTO auditoria (id_usuario,accion,entidad,registro_id,fecha_hora,user_agent) VALUES (?,?,?,?,?,?)")) {
            s.setLong(1,exportacion.idUsuario());s.setString(2,"EXPORTAR_XLSX");s.setString(3,"EXPORTACION_REPORTE");
            s.setLong(4,exportacion.idExportacion());s.setObject(5,exportacion.fechaHora());
            s.setString(6,"Session-UUID: "+SessionManager.getInstance().getSessionUUID());
            if(s.executeUpdate()!=1) throw new SQLException("No se pudo auditar la exportación.");
        }
    }

    private final AuditoriaDAO auditoriaDAO;

    public AuditoriaService() {
        this(new AuditoriaDAOImpl());
    }

    public AuditoriaService(AuditoriaDAO auditoriaDAO) {
        this.auditoriaDAO = java.util.Objects.requireNonNull(auditoriaDAO);
    }

    public void auditarLoginOk(Long idUsuario) {
        try {
            auditoriaDAO.registrarEvento(idUsuario, "LOGIN_OK", "SESION", null, null, null, null);
        } catch (SQLException e) {
            System.err.println("No se pudo registrar el evento de sesión.");
        }
    }

    public void auditarLogout(Long idUsuario) {
        try {
            auditoriaDAO.registrarEvento(idUsuario, "LOGOUT", "SESION", null, null, null, null);
        } catch (SQLException e) {
            System.err.println("No se pudo registrar el evento de sesión.");
        }
    }

    public void auditarAccesoDenegado(Long idUsuario, String intentoUsername) {
        try {
            auditoriaDAO.registrarEvento(idUsuario, "ACCESO_DENEGADO", "SESION", null, null, null, null);
        } catch (SQLException e) {
            System.err.println("No se pudo registrar el evento de sesión.");
        }
    }

    public void auditarAltaEmpleado(Long idUsuario, Long idEmpleado, String detalle) {
        registrarEvento(idUsuario, "ALTA_EMPLEADO", "empleado", idEmpleado, "alta", null, detalle, LocalDateTime.now());
    }

    public void auditarEdicionEmpleado(Long idUsuario, Long idEmpleado, String detalle) {
        registrarEvento(idUsuario, "EDICION_EMPLEADO", "empleado", idEmpleado, "edicion", null, detalle, LocalDateTime.now());
    }

    public void registrarEvento(String accion, String entidad, Long registroId, String campo, String valorAnterior, String valorNuevo) {
        registrarEvento(null, accion, entidad, registroId, campo, valorAnterior, valorNuevo, LocalDateTime.now());
    }

    public void registrarEvento(Long idUsuario, String accion, String entidad, Long registroId, String campo, String valorAnterior, String valorNuevo) {
        registrarEvento(idUsuario, accion, entidad, registroId, campo, valorAnterior, valorNuevo, LocalDateTime.now());
    }

    public void registrarEvento(Long idUsuario, String accion, String entidad, Long registroId, String campo, String valorAnterior, String valorNuevo, LocalDateTime fechaHora) {
        String seguro = campo == null ? "" : campo.toLowerCase(java.util.Locale.ROOT);
        boolean identificador = java.util.Set.of("id_estado_empleado", "id_area_principal", "id_puesto_actual",
                "id_jefe_inmediato", "id_nivel_academico", "id_empleado", "id_rol", "id_tipo_logro").contains(seguro);
        boolean bandera = java.util.Set.of("activo", "es_estado_activo", "destacado").contains(seguro);
        boolean permiso = "permiso".equals(seguro);
        if (!identificador && !bandera && !permiso) {
            campo = null;
            valorAnterior = null;
            valorNuevo = null;
        } else {
            campo = seguro;
            String patron = identificador ? "[0-9]{1,20}" : bandera ? "(?i:true|false|0|1)" : "[A-Z_]{1,80}";
            valorAnterior = valorAnterior != null && valorAnterior.matches(patron) ? valorAnterior : null;
            valorNuevo = valorNuevo != null && valorNuevo.matches(patron) ? valorNuevo : null;
        }

        if (idUsuario == null) {
            Usuario current = SessionManager.getInstance().getCurrentUser();
            if (current == null) {
                current = SesionService.getInstance().getUsuarioAutenticado();
            }
            if (current != null) {
                idUsuario = current.getIdUsuario();
            }
        }

        try {
            auditoriaDAO.registrarEvento(idUsuario, accion, entidad, registroId, campo, valorAnterior, valorNuevo, fechaHora != null ? fechaHora : LocalDateTime.now());
        } catch (SQLException e) {
            System.err.println("No se pudo registrar el evento de auditoría.");
        }
    }

    public List<String> obtenerEventosConUsuario(int limite) {
        new AutorizacionService().exigirPermiso("AUDITORIA_VER");
        try {
            return auditoriaDAO.obtenerEventosConUsuario(limite);
        } catch (SQLException e) {
            System.err.println("No se pudo consultar la auditoría.");
            return new java.util.ArrayList<>();
        }
    }

    public List<Auditoria> listarRegistrosAuditoria(int limite) {
        new AutorizacionService().exigirPermiso("AUDITORIA_VER");
        try {
            return auditoriaDAO.listarAuditoria(limite);
        } catch (SQLException e) {
            System.err.println("No se pudo consultar la auditoría.");
            return new java.util.ArrayList<>();
        }
    }

    public List<Auditoria> listarRegistrosAuditoria(String busqueda, String entidad, int limite) {
        new AutorizacionService().exigirPermiso("AUDITORIA_VER");
        try {
            return auditoriaDAO.listarAuditoriaPorFiltro(busqueda, entidad, limite);
        } catch (SQLException e) {
            System.err.println("No se pudo consultar la auditoría.");
            return new java.util.ArrayList<>();
        }
    }
}
