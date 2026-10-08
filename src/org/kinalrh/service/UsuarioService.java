package org.kinalrh.service;

import java.util.List;
import org.kinalrh.dao.UsuarioDAO;
import org.kinalrh.dao.Impl.UsuarioDAOImpl;
import org.kinalrh.model.Usuario;
import org.kinalrh.util.SecurityUtil;

public class UsuarioService {

    private UsuarioDAO usuarioDAO;
    private AutorizacionService autorizacionService;

    public UsuarioService() {
        this.usuarioDAO = new UsuarioDAOImpl();
        this.autorizacionService = new AutorizacionService();
    }

    // Retorna la lista solo si el usuario actual tiene el permiso USUARIO_GESTIONAR
    public List<Usuario> listarUsuarios(String rolActual) {
        if (!autorizacionService.tienePermiso(rolActual, "USUARIO_GESTIONAR")) {
            throw new SecurityException("No tiene permiso para gestionar usuarios.");
        }
        return usuarioDAO.listarTodos();
    }

    public void guardarUsuario(Usuario usuario, String rolActual) {
        if (!autorizacionService.tienePermiso(rolActual, "USUARIO_GESTIONAR")) {
            throw new SecurityException("No tiene permiso para crear usuarios.");
        }
        // Encriptar password si viene
        if (usuario.getPasswordHash() != null && !usuario.getPasswordHash().isEmpty()) {
            
        }
        usuarioDAO.guardar(usuario);
    }

    public void actualizarUsuario(Usuario usuario, String rolActual) {
        if (!autorizacionService.tienePermiso(rolActual, "USUARIO_GESTIONAR")) {
            throw new SecurityException("No tiene permiso para editar usuarios.");
        }
        // Solo hashear la contraseÃ±a si la cambiaron (no viene vacÃ­a y no es el hash viejo)
        if (usuario.getPasswordHash() != null && !usuario.getPasswordHash().isEmpty()) {
            
        } else {
            // Mantener el hash anterior
            Usuario viejo = usuarioDAO.buscarPorUsername(usuario.getUsername());
            if (viejo != null) {
                usuario.setPasswordHash(viejo.getPasswordHash());
            }
        }
        usuarioDAO.actualizar(usuario);
    }

    public void cambiarEstado(int id, boolean estado, String rolActual) {
        if (!autorizacionService.tienePermiso(rolActual, "USUARIO_GESTIONAR")) {
            throw new SecurityException("No tiene permiso para cambiar el estado.");
        }
        usuarioDAO.cambiarEstado(id, estado);
    }
}
