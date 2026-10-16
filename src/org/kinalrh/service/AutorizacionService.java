package org.kinalrh.service;

import java.sql.*;
import java.util.Objects;
import org.kinalrh.model.Usuario;
import org.kinalrh.util.Conexion;
import org.kinalrh.util.SessionManager;

public class AutorizacionService {
    private final AuditoriaService auditoriaService = new AuditoriaService();

    public Usuario exigirPermiso(Connection conn, String permiso) throws SQLException {
        Usuario usuario = exigirSesion();
        String sesion = SessionManager.getInstance().getSessionUUID();
        String sql = "SELECT 1 FROM usuario u JOIN usuario_rol ur ON ur.id_usuario=u.id_usuario "
                + "JOIN rol r ON r.id_rol=ur.id_rol LEFT JOIN rol_permiso rp ON rp.id_rol=r.id_rol "
                + "LEFT JOIN permiso p ON p.id_permiso=rp.id_permiso "
                + "WHERE u.id_usuario=? AND u.activo=TRUE AND r.activo=TRUE "
                + "AND (r.nombre='admin' OR (p.activo=TRUE AND p.codigo=?))";
        try (PreparedStatement s = conn.prepareStatement(sql)) {
            s.setLong(1,usuario.getIdUsuario()); s.setString(2,permiso);
            try (ResultSet r = s.executeQuery()) {
                if (!r.next()) throw new SecurityException("Su cuenta requiere el permiso " + permiso + ".");
            }
        }
        if (!Objects.equals(sesion,SessionManager.getInstance().getSessionUUID())
                || usuario != SessionManager.getInstance().getCurrentUser()) {
            throw new SecurityException("La sesión cambió. Inicie la operación nuevamente.");
        }
        return usuario;
    }

    private Usuario exigirSesion() {
        Usuario usuario = SessionManager.getInstance().getCurrentUser();
        if (usuario == null || !usuario.isActivo() || usuario.getIdUsuario() == null
                || usuario.getIdUsuario() <= 0 || SessionManager.getInstance().getSessionUUID() == null) {
            throw new SecurityException("Se requiere una sesión activa.");
        }
        return usuario;
    }

    public Usuario exigirPermiso(String permiso) {
        exigirSesion();
        try (Connection conn = Conexion.getInstancia().conectar()) {
            return exigirPermiso(conn,permiso);
        } catch (SQLException ex) {
            throw new SecurityException("No se pudo comprobar el permiso requerido.");
        } catch (SecurityException ex) {
            auditoriaService.registrarEvento("ACCESO_DENEGADO", "OPERACION", null, "permiso", null, permiso);
            throw ex;
        }
    }

    /** Compatibilidad con controladores antiguos; el rol recibido no concede acceso. */
    public boolean tienePermiso(String rol, String permiso) {
        try { exigirPermiso(permiso); return true; }
        catch (SecurityException ex) { return false; }
    }

    public void exigirAlgunPermiso(String... permisos) {
        exigirSesion();
        try (Connection conn = Conexion.getInstancia().conectar()) {
            for (String permiso : permisos) {
                try { exigirPermiso(conn,permiso); return; }
                catch (SecurityException ex) { /* Evaluar las otras alternativas antes de auditar. */ }
            }
        } catch (SQLException ex) {
            throw new SecurityException("No se pudo comprobar el permiso requerido.");
        }
        auditoriaService.registrarEvento("ACCESO_DENEGADO", "OPERACION", null, "permiso", null, permisos.length == 0 ? null : permisos[0]);
        throw new SecurityException("Su cuenta no tiene permiso para esta operación.");
    }
}
