package org.kinalrh.service;

import org.kinalrh.dao.UsuarioDAO;
import org.kinalrh.dao.impl.UsuarioDAOImpl;
import org.kinalrh.model.Usuario;
import org.kinalrh.util.PasswordHasher;
import java.sql.SQLException;
import java.util.Optional;

public class AutenticacionService {

    private UsuarioDAO usuarioDAO;

    public AutenticacionService() {
        this.usuarioDAO = new UsuarioDAOImpl();
    }

    public Usuario autenticar(String username, String password) {
        try {
            Optional<Usuario> optUsuario = usuarioDAO.buscarPorNombreUsuario(username);
            if (!optUsuario.isPresent()) {
                throw new SecurityException("Usuario o contraseña incorrectos");
            }
            
            Usuario u = optUsuario.get();
            if (!u.isActivo()) {
                throw new SecurityException("Usuario o contraseña incorrectos"); // Rechaza inactivos con el mismo mensaje
            }
            
            boolean match = PasswordHasher.verifyPassword(password, u.getPasswordHash());
            if (!match) {
                // Posibilidad de fallback a SecurityUtil.hashSHA256 si hay contraseñas viejas, pero por T1.12 lo dejamos limpio
                if (u.getPasswordHash().length() == 64 && !u.getPasswordHash().contains(":")) {
                    String oldHash = org.kinalrh.util.SecurityUtil.hashSHA256(password);
                    if (oldHash.equals(u.getPasswordHash())) {
                        return u;
                    }
                }
                throw new SecurityException("Usuario o contraseña incorrectos");
            }
            
            return u;
            
        } catch (SQLException e) {
            throw new RuntimeException("Error en el acceso a datos", e);
        }
    }
}