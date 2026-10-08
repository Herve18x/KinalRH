package org.kinalrh.dao;

import java.util.List;
import org.kinalrh.model.Usuario;

public interface UsuarioDAO {
    List<Usuario> listarTodos();
    Usuario buscarPorUsername(String username);
    void guardar(Usuario usuario);
    void actualizar(Usuario usuario);
    void cambiarEstado(int id, boolean activo);
    boolean autenticar(String nombreUsuario, String passwordHash);
}
