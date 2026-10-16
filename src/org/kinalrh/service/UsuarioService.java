package org.kinalrh.service;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import org.kinalrh.dao.UsuarioDAO;
import org.kinalrh.dao.impl.UsuarioDAOImpl;
import org.kinalrh.model.Usuario;
import org.kinalrh.util.PasswordHasher;

public class UsuarioService {
    private final UsuarioDAO usuarioDAO = new UsuarioDAOImpl();
    private final AutorizacionService autorizacionService = new AutorizacionService();
    private static final Pattern CORREO = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9-]+(?:\\.[A-Za-z0-9-]+)+$");

    public List<Usuario> listarUsuarios(String rolActual) {
        autorizacionService.exigirPermiso("USUARIO_GESTIONAR");
        return usuarioDAO.listarTodos();
    }

    public Optional<Usuario> buscarPorId(long id, String rolActual) {
        autorizacionService.exigirPermiso("USUARIO_GESTIONAR");
        return usuarioDAO.buscarPorId(id);
    }

    public void guardarUsuario(Usuario usuario, String rolActual) {
        autorizacionService.exigirPermiso("USUARIO_GESTIONAR");
        validar(usuario);
        if (usuarioDAO.buscarPorUsername(usuario.getUsername()) != null) {
            throw new IllegalArgumentException("El nombre de usuario ya está en uso.");
        }
        if (usuario.getPasswordHash() == null || usuario.getPasswordHash().isBlank()) {
            throw new IllegalArgumentException("La contraseña inicial es obligatoria para nuevas cuentas.");
        }
        String entrada = usuario.getPasswordHash();
        usuario.setPasswordHash(PasswordHasher.hashPassword(entrada));
        try { usuarioDAO.guardar(usuario); }
        catch (RuntimeException ex) { usuario.setPasswordHash(entrada); throw ex; }
    }

    public void actualizarUsuario(Usuario usuario, String rolActual) {
        autorizacionService.exigirPermiso("USUARIO_GESTIONAR");
        validar(usuario);
        if (usuario.getIdUsuario() == null || usuario.getIdUsuario() <= 0) {
            throw new IllegalArgumentException("El ID de usuario es requerido para actualizar.");
        }
        Usuario viejo = usuarioDAO.buscarPorId(usuario.getIdUsuario())
                .orElseThrow(() -> new IllegalArgumentException("El usuario a actualizar no existe."));
        String entrada = usuario.getPasswordHash();
        if (usuario.getPasswordHash() != null && !usuario.getPasswordHash().isEmpty()
                && !usuario.getPasswordHash().equals(viejo.getPasswordHash())) {
            usuario.setPasswordHash(PasswordHasher.hashPassword(usuario.getPasswordHash()));
        } else {
            usuario.setPasswordHash(viejo.getPasswordHash());
        }
        try { usuarioDAO.actualizar(usuario); }
        catch (RuntimeException ex) { usuario.setPasswordHash(entrada); throw ex; }
    }

    private void validar(Usuario usuario) {
        if (usuario == null) throw new IllegalArgumentException("Los datos del usuario son obligatorios.");
        if (usuario.getUsername() == null || usuario.getUsername().isBlank() || usuario.getUsername().trim().length() > 80) {
            throw new IllegalArgumentException("El usuario es obligatorio y admite hasta 80 caracteres.");
        }
        if (usuario.getNombreCompleto() == null || usuario.getNombreCompleto().isBlank()
                || usuario.getNombreCompleto().trim().length() > 160) {
            throw new IllegalArgumentException("El nombre es obligatorio y admite hasta 160 caracteres.");
        }
        usuario.setUsername(usuario.getUsername().trim());
        usuario.setNombreCompleto(usuario.getNombreCompleto().trim());
        String correo = usuario.getCorreo();
        correo = correo == null || correo.isBlank() ? null : correo.trim();
        if (correo != null && (correo.length() > 150 || !CORREO.matcher(correo).matches())) {
            throw new IllegalArgumentException("El formato del correo es inválido.");
        }
        usuario.setCorreo(correo);
    }

    public void cambiarEstado(int id, boolean estado, String rolActual) {
        autorizacionService.exigirPermiso("USUARIO_GESTIONAR");
        usuarioDAO.cambiarEstado(id, estado);
    }

    public void activar(long id, String rolActual) {
        autorizacionService.exigirPermiso("USUARIO_GESTIONAR");
        usuarioDAO.activar(id);
    }

    public void desactivar(long id, String rolActual) {
        autorizacionService.exigirPermiso("USUARIO_GESTIONAR");
        usuarioDAO.desactivar(id);
    }

    public void asignarRol(long idUsuario, long idRol, String rolActual) {
        autorizacionService.exigirPermiso("USUARIO_GESTIONAR");
        usuarioDAO.asignarRol(idUsuario, idRol);
    }

    public List<org.kinalrh.model.ColaboradorSinRol> listarColaboradoresSinRol(String rolActual) {
        autorizacionService.exigirPermiso("USUARIO_GESTIONAR");
        return usuarioDAO.listarColaboradoresSinRol();
    }
}
