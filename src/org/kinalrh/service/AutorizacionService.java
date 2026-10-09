package org.kinalrh.service;

public class AutorizacionService {
    
    public boolean tienePermiso(String rol, String permiso) {
        if (rol == null) return false;
        String upperRol = rol.toUpperCase();
        
        // Reglas de negocio básicas: ADMIN tiene todo.
        if ("ADMIN".equals(upperRol) || "ADMIN_TOTAL".equals(permiso)) {
            return "ADMIN".equals(upperRol);
        }
        
        if ("RRHH".equals(upperRol) || "ENCARGADO".equals(upperRol)) {
            // Recursos humanos puede ver empleados y gestionar usuarios (T2.02)
            if ("EMPLEADO_VER".equals(permiso) || "USUARIO_GESTIONAR".equals(permiso)) {
                return true;
            }
        }
        
        if ("SUPERVISOR".equals(upperRol)) {
            if ("EMPLEADO_VER".equals(permiso)) {
                return true;
            }
        }
        
        return false;
    }
}
