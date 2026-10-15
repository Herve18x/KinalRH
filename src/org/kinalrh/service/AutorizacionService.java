package org.kinalrh.service;

import org.kinalrh.model.Usuario;
import org.kinalrh.util.SessionManager;

public class AutorizacionService {
    public Usuario exigirPermiso(java.sql.Connection conn, String permiso) throws java.sql.SQLException {
        Usuario usuario = SessionManager.getInstance().getCurrentUser();
        if (usuario == null || !usuario.isActivo() || usuario.getIdUsuario() == null || usuario.getIdUsuario() <= 0
                || SessionManager.getInstance().getSessionUUID() == null) throw new SecurityException("Se requiere una sesión activa.");
        if ("ADMIN".equalsIgnoreCase(usuario.getRol())) return usuario;
        String sql = "SELECT 1 FROM usuario u JOIN usuario_rol ur ON ur.id_usuario=u.id_usuario "
                + "JOIN rol r ON r.id_rol=ur.id_rol JOIN rol_permiso rp ON rp.id_rol=r.id_rol "
                + "JOIN permiso p ON p.id_permiso=rp.id_permiso "
                + "WHERE u.id_usuario=? AND u.activo=TRUE AND r.activo=TRUE AND p.activo=TRUE AND p.codigo=?";
        try (java.sql.PreparedStatement s=conn.prepareStatement(sql)) {
            s.setLong(1,usuario.getIdUsuario()); s.setString(2,permiso);
            try(java.sql.ResultSet r=s.executeQuery()) {
                if(!r.next()) throw new SecurityException("Su cuenta requiere el permiso "+permiso+".");
            }
        }
        return usuario;
    }
    public Usuario exigirPermiso(String permiso) {
        Usuario usuario = SessionManager.getInstance().getCurrentUser();
        if (usuario == null || !usuario.isActivo() || usuario.getIdUsuario() == null
                || usuario.getIdUsuario() <= 0 || SessionManager.getInstance().getSessionUUID() == null) {
            throw new SecurityException("Se requiere una sesión activa.");
        }
        boolean permitido = "ADMIN".equalsIgnoreCase(usuario.getRol());
        if (!permitido) {
            try {
                permitido = new org.kinalrh.dao.impl.UsuarioDAOImpl().obtenerPermisos(usuario.getIdUsuario()).contains(permiso);
            } catch (java.sql.SQLException ex) {
                throw new SecurityException("No se pudo comprobar el permiso requerido.");
            }
        }
        if (!permitido) {
            auditoriaService.registrarEvento("ACCESO_DENEGADO", "OPERACION", null, "permiso", null, permiso);
            throw new SecurityException("Su cuenta requiere el permiso " + permiso + ".");
        }
        return usuario;
    }
    
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
