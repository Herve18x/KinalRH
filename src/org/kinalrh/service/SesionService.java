package org.kinalrh.service;

import java.util.Set;
import org.kinalrh.model.Usuario;
import org.kinalrh.dao.UsuarioDAO;
import org.kinalrh.dao.impl.UsuarioDAOImpl;

public class SesionService {

    private static SesionService instance;
    private Usuario usuarioAutenticado;
    private Set<String> permisosVigentes;
    private UsuarioDAO usuarioDAO;
    private AuditoriaService auditoriaService;

    private SesionService() {
        this.usuarioDAO = new UsuarioDAOImpl();
        this.auditoriaService = new AuditoriaService();
    }

    public static SesionService getInstance() {
        if (instance == null) {
            instance = new SesionService();
        }
        return instance;
    }

    public void iniciarSesion(Usuario usuario) {
        this.usuarioAutenticado = usuario;
        try {
            this.permisosVigentes = usuarioDAO.obtenerPermisos(usuario.getIdUsuario());
        } catch (java.sql.SQLException e) {
            System.err.println("No se pudieron cargar los permisos de sesión.");
            this.permisosVigentes = new java.util.HashSet<>();
        }
        org.kinalrh.util.SessionManager.getInstance().login(usuario);
    }

    public void cerrarSesion() {
        if (this.usuarioAutenticado != null) {
            this.auditoriaService.auditarLogout(this.usuarioAutenticado.getIdUsuario());
        }
        this.usuarioAutenticado = null;
        if (this.permisosVigentes != null) {
            this.permisosVigentes.clear();
        }
        org.kinalrh.util.SessionManager.getInstance().logout();
    }

    public Usuario getUsuarioAutenticado() {
        return org.kinalrh.util.SessionManager.getInstance().getCurrentUser();
    }

    public boolean tienePermiso(String permiso) {
        Usuario actual = getUsuarioAutenticado();
        if (actual == null || !actual.isActivo() || actual != usuarioAutenticado || permisosVigentes == null) return false;
        return permiso != null && new AutorizacionService().tienePermiso(null, permiso.toUpperCase(java.util.Locale.ROOT));
    }
}
