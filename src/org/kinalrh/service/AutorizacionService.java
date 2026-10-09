package org.kinalrh.service;

import org.kinalrh.model.Usuario;
import org.kinalrh.util.SessionManager;

public class AutorizacionService {
    
    private AuditoriaService auditoriaService;
    
    public AutorizacionService() {
        this.auditoriaService = new AuditoriaService();
    }
    
    public boolean tienePermiso(String rol, String permiso) {
        boolean permitido = false;
        if (rol != null) {
            String upperRol = rol.toUpperCase();
            
            if ("ADMIN".equals(upperRol) || "ADMIN_TOTAL".equals(permiso)) {
                permitido = "ADMIN".equals(upperRol);
            } else if ("RRHH".equals(upperRol) || "ENCARGADO".equals(upperRol)) {
                if ("EMPLEADO_VER".equals(permiso) || "USUARIO_GESTIONAR".equals(permiso)) {
                    permitido = true;
                }
            } else if ("SUPERVISOR".equals(upperRol)) {
                if ("EMPLEADO_VER".equals(permiso)) {
                    permitido = true;
                }
            }
        }
        
        if (!permitido) {
            Usuario u = SessionManager.getInstance().getCurrentUser();
            if (u != null) {
                // Registrar intento fallido de acceso a operación (T1.22)
                auditoriaService.registrarEvento("ACCESO_DENEGADO", "OPERACION", null, "permiso", null, permiso);
            }
        }
        
        return permitido;
    }
}