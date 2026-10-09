package org.kinalrh.service;

import java.sql.SQLException;
import org.kinalrh.dao.AuditoriaDAO;
import org.kinalrh.dao.impl.AuditoriaDAOImpl;

public class AuditoriaService {
    
    private AuditoriaDAO auditoriaDAO;
    
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
            // T1.22: Omitir contraseñas y datos personales. Solo registramos que hubo un intento para este username
            auditoriaDAO.registrarEvento(idUsuario, "ACCESO_DENEGADO", "SESION", null, "username_intentado", null, intentoUsername);
        } catch (SQLException e) {
            System.err.println("Error al auditar acceso denegado: " + e.getMessage());
        }
    }
    
    public void registrarEvento(String accion, String entidad, Long registroId, String campo, String valorAnterior, String valorNuevo) {
        // T1.22 Seguridad extra: Omitir datos sensibles general
        if ("password_hash".equalsIgnoreCase(campo) || "contraseña".equalsIgnoreCase(campo) || "dpi".equalsIgnoreCase(campo)) {
            valorAnterior = "[PROTEGIDO]";
            valorNuevo = "[PROTEGIDO]";
        }
        
        org.kinalrh.model.Usuario current = org.kinalrh.util.SessionManager.getInstance().getCurrentUser();
        Long idUsuario = (current != null) ? current.getIdUsuario() : null;
        
        try {
            auditoriaDAO.registrarEvento(idUsuario, accion, entidad, registroId, campo, valorAnterior, valorNuevo);
        } catch (SQLException e) {
            System.err.println("Error al registrar auditoria: " + e.getMessage());
        }
    }
}