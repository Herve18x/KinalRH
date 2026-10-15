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

    private final AuditoriaDAO auditoriaDAO;

    public AuditoriaService() {
        this.auditoriaDAO = new AuditoriaDAOImpl();
    }

    public void auditarLoginOk(Long idUsuario) {
        try {
            auditoriaDAO.registrarEvento(idUsuario, "LOGIN_OK", "SESION", null, null, null, null);
        } catch (SQLException e) {
            System.err.println("Error al auditar login: " + e.getMessage());
        }
    }

    public void auditarLogout(Long idUsuario) {
        try {
            auditoriaDAO.registrarEvento(idUsuario, "LOGOUT", "SESION", null, null, null, null);
        } catch (SQLException e) {
            System.err.println("Error al auditar logout: " + e.getMessage());
        }
    }

    public void auditarAccesoDenegado(Long idUsuario, String intentoUsername) {
        try {
            auditoriaDAO.registrarEvento(idUsuario, "ACCESO_DENEGADO", "SESION", null, "username_intentado", null, intentoUsername);
        } catch (SQLException e) {
            System.err.println("Error al auditar acceso denegado: " + e.getMessage());
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
        // T1.22 Seguridad: Omitir datos sensibles
        if ("password_hash".equalsIgnoreCase(campo) || "contraseña".equalsIgnoreCase(campo) || "dpi".equalsIgnoreCase(campo)) {
            valorAnterior = "[PROTEGIDO]";
            valorNuevo = "[PROTEGIDO]";
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
            System.err.println("Error al registrar auditoria: " + e.getMessage());
        }
    }

    public List<String> obtenerEventosConUsuario(int limite) {
        try {
            return auditoriaDAO.obtenerEventosConUsuario(limite);
        } catch (SQLException e) {
            System.err.println("Error al consultar eventos de auditoría: " + e.getMessage());
            return new java.util.ArrayList<>();
        }
    }

    public List<Auditoria> listarRegistrosAuditoria(int limite) {
        try {
            return auditoriaDAO.listarAuditoria(limite);
        } catch (SQLException e) {
            System.err.println("Error al listar registros de auditoría: " + e.getMessage());
            return new java.util.ArrayList<>();
        }
    }

    public List<Auditoria> listarRegistrosAuditoria(String busqueda, String entidad, int limite) {
        try {
            return auditoriaDAO.listarAuditoriaPorFiltro(busqueda, entidad, limite);
        } catch (SQLException e) {
            System.err.println("Error al listar registros de auditoría por filtro: " + e.getMessage());
            return new java.util.ArrayList<>();
        }
    }
}