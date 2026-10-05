package org.kinalrh.dao.Impl;

import java.util.ArrayList;
import java.util.List;
import org.kinalrh.dao.UsuarioDAO;
import org.kinalrh.model.Usuario;

public class UsuarioDAOImpl implements UsuarioDAO {
    // Simulando base de datos para la prueba
    private static List<Usuario> usuarios = new ArrayList<>();
    
    static {
        // Usuario admin por defecto
        usuarios.add(new Usuario(1, "admin", "Admin", "Admin", "8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918", "ADMIN", true));
        // Usuario RRHH
        usuarios.add(new Usuario(2, "rrhh", "Juan", "Perez", "e10adc3949ba59abbe56e057f20f883e", "RRHH", true));
    }

    @Override
    public List<Usuario> listarTodos() {
        return new ArrayList<>(usuarios);
    }

    @Override
    public Usuario buscarPorUsername(String username) {
        return usuarios.stream()
            .filter(u -> u.getUsername().equals(username))
            .findFirst()
            .orElse(null);
    }

    @Override
    public void guardar(Usuario usuario) {
        usuario.setId(usuarios.size() + 1);
        usuarios.add(usuario);
    }

    @Override
    public void actualizar(Usuario usuario) {
        for (int i = 0; i < usuarios.size(); i++) {
            if (usuarios.get(i).getId().equals(usuario.getId())) {
                usuarios.set(i, usuario);
                return;
            }
        }
    }

    @Override
    public void cambiarEstado(int id, boolean activo) {
        for (Usuario u : usuarios) {
            if (u.getId().equals(id)) {
                u.setActivo(activo);
                return;
            }
        }
    }
}
