package org.kinalrh.service;

import org.kinalrh.dao.UsuarioDAO;
import org.kinalrh.dao.impl.UsuarioDAOImpl;
import org.kinalrh.model.Usuario;
import org.kinalrh.util.PasswordHasher;
import java.sql.SQLException;
import java.util.Optional;

public class AutenticacionService {

    private UsuarioDAO usuarioDAO;
    private AuditoriaService auditoriaService;

    public AutenticacionService() {
        this.usuarioDAO = new UsuarioDAOImpl();
        this.auditoriaService = new AuditoriaService();
    }

    public Usuario autenticar(String username, String password) {
        try {
            Optional<Usuario> optUsuario = usuarioDAO.buscarPorNombreUsuario(username);
            if (!optUsuario.isPresent()) {
                auditoriaService.auditarAccesoDenegado(null, username);
                throw new SecurityException("Usuario o contraseña incorrectos");
            }
            
            Usuario u = optUsuario.get();
            if (!u.isActivo()) {
                auditoriaService.auditarAccesoDenegado(u.getIdUsuario(), username);
                throw new SecurityException("Usuario o contraseña incorrectos");
            }
            
            boolean match = PasswordHasher.verifyPassword(password, u.getPasswordHash());
            if (!match) {
                if (u.getPasswordHash() != null && u.getPasswordHash().length() == 64 && !u.getPasswordHash().contains(":")) {
                    String oldHash = org.kinalrh.util.SecurityUtil.hashSHA256(password);
                    if (oldHash.equals(u.getPasswordHash())) {
                        auditoriaService.auditarLoginOk(u.getIdUsuario());
                        return u;
                    }
                }
                auditoriaService.auditarAccesoDenegado(u.getIdUsuario(), username);
                throw new SecurityException("Usuario o contraseña incorrectos");
            }
            
            auditoriaService.auditarLoginOk(u.getIdUsuario());
            return u;
            
        } catch (SQLException e) {
            throw new RuntimeException("Error en el acceso a datos", e);
        }
    }
}