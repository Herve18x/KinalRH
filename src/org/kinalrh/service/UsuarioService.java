package org.kinalrh.service;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import org.kinalrh.dao.UsuarioDAO;
import org.kinalrh.dao.impl.UsuarioDAOImpl;
import org.kinalrh.model.Usuario;
import org.kinalrh.util.SecurityUtil;

public class UsuarioService {

    private UsuarioDAO usuarioDAO;
    private AutorizacionService autorizacionService;

    public UsuarioService() {
        this.usuarioDAO = new UsuarioDAOImpl();
        this.autorizacionService = new AutorizacionService();
    }

    public List<Usuario> listarUsuarios(String rolActual) {
        if (!autorizacionService.tienePermiso(rolActual, "USUARIO_GESTIONAR")) {
            throw new SecurityException("No tiene permiso para gestionar usuarios.");
        }
        return usuarioDAO.listarTodos();
    }
    
    public Optional<Usuario> buscarPorId(long id, String rolActual) {
        if (!autorizacionService.tienePermiso(rolActual, "USUARIO_GESTIONAR")) {
            throw new SecurityException("No tiene permiso para gestionar usuarios.");
        }
        return usuarioDAO.buscarPorId(id);
    }

    public void guardarUsuario(Usuario usuario, String rolActual) {
        if (!autorizacionService.tienePermiso(rolActual, "USUARIO_GESTIONAR")) {
            throw new SecurityException("No tiene permiso para crear usuarios.");
        }
        
        // Validar nombre_usuario Ãºnico
        if (usuarioDAO.buscarPorUsername(usuario.getUsername()) != null) {
            throw new IllegalArgumentException("El nombre de usuario ya estÃ¡ en uso.");
        }
        
        // Validar correo si se informa
        if (usuario.getCorreo() != null && !usuario.getCorreo().trim().isEmpty()) {
            if (!Pattern.matches("^[A-Za-z0-9+_.-]+@(.+)$", usuario.getCorreo())) {
                throw new IllegalArgumentException("El formato del correo es invÃ¡lido.");
            }
        }
        
        // Validar y encriptar contraseÃ±a inicial (T1.12)
        if (usuario.getPasswordHash() == null || usuario.getPasswordHash().isEmpty()) {
            throw new IllegalArgumentException("La contraseÃ±a inicial es obligatoria para nuevas cuentas.");
        }
        String hash = SecurityUtil.hashSHA256(usuario.getPasswordHash());
        usuario.setPasswordHash(hash);
        
        usuarioDAO.guardar(usuario);
    }

    public void actualizarUsuario(Usuario usuario, String rolActual) {
        if (!autorizacionService.tienePermiso(rolActual, "USUARIO_GESTIONAR")) {
            throw new SecurityException("No tiene permiso para editar usuarios.");
        }
        
        // Validar correo si se informa
        if (usuario.getCorreo() != null && !usuario.getCorreo().trim().isEmpty()) {
            if (!Pattern.matches("^[A-Za-z0-9+_.-]+@(.+)$", usuario.getCorreo())) {
                throw new IllegalArgumentException("El formato del correo es invÃ¡lido.");
            }
        }
        
        // Evitar actualizaciÃ³n si el ID no estÃ¡ seteado
        if (usuario.getIdUsuario() == null || usuario.getIdUsuario() == 0) {
            throw new IllegalArgumentException("El ID de usuario es requerido para actualizar.");
        }

        Usuario viejo = usuarioDAO.buscarPorId(usuario.getIdUsuario()).orElse(null);
        if (viejo == null) {
            throw new IllegalArgumentException("El usuario a actualizar no existe.");
        }
        
        // Solo hashear la contraseÃ±a si la cambiaron (no viene vacÃ­a y no es el hash viejo)
        if (usuario.getPasswordHash() != null && !usuario.getPasswordHash().isEmpty() && !usuario.getPasswordHash().equals(viejo.getPasswordHash())) {
            String hash = SecurityUtil.hashSHA256(usuario.getPasswordHash());
            usuario.setPasswordHash(hash);
        } else {
            // Mantener el hash anterior si no se modificÃ³ la contraseÃ±a
            usuario.setPasswordHash(viejo.getPasswordHash());
        }
        
        usuarioDAO.actualizar(usuario);
    }

    public void cambiarEstado(int id, boolean estado, String rolActual) {
        if (!autorizacionService.tienePermiso(rolActual, "USUARIO_GESTIONAR")) {
            throw new SecurityException("No tiene permiso para cambiar el estado.");
        }
        usuarioDAO.cambiarEstado(id, estado);
    }
    public void activar(long id, String rolActual) {
        if (!autorizacionService.tienePermiso(rolActual, "USUARIO_GESTIONAR")) {
            throw new SecurityException("No tiene permiso para gestionar usuarios.");
        }
        usuarioDAO.activar(id);
    }

    public void desactivar(long id, String rolActual) {
        if (!autorizacionService.tienePermiso(rolActual, "USUARIO_GESTIONAR")) {
            throw new SecurityException("No tiene permiso para gestionar usuarios.");
        }
        
        Usuario u = usuarioDAO.buscarPorId(id).orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        
        // Impedir desactivar la última cuenta administradora activa
        if ("ADMIN".equalsIgnoreCase(u.getRol()) && u.isActivo()) {
            if (usuarioDAO.contarAdministradoresActivos() <= 1) {
                throw new IllegalStateException("No se puede desactivar la última cuenta administradora activa del sistema.");
            }
        }
        
        usuarioDAO.desactivar(id);
    }

    public void asignarRol(long idUsuario, long idRol, String rolActual) {
        if (!autorizacionService.tienePermiso(rolActual, "USUARIO_GESTIONAR")) {
            throw new SecurityException("No tiene permiso para gestionar roles.");
        }
        
        // Rechazar rol inactivo/inexistente
        boolean rolValido = false;
        String sql = "SELECT activo FROM rol WHERE id_rol = ?";
        try (java.sql.Connection conn = org.kinalrh.util.Conexion.getInstancia().conectar();
             java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idRol);
            try (java.sql.ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    rolValido = rs.getInt("activo") == 1;
                }
            }
        } catch (java.sql.SQLException e) {
            throw new RuntimeException("Error al validar el rol", e);
        }
        
        if (!rolValido) {
            throw new IllegalArgumentException("El rol no existe o se encuentra inactivo.");
        }
        
        usuarioDAO.asignarRol(idUsuario, idRol);
    }
}