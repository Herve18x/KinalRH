package org.kinalrh.dao;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.kinalrh.model.Usuario;

public interface UsuarioDAO {

    List<Usuario> listarTodos();
    Usuario buscarPorUsername(String username);
    Optional<Usuario> buscarPorId(long id);
    void guardar(Usuario usuario);
    void actualizar(Usuario usuario);
    void cambiarEstado(int id, boolean activo);
    int contarAdministradoresActivos();
    
    void activar(long id);
    void desactivar(long id);
    void asignarRol(long idUsuario, long idRol);

    Optional<Usuario> buscarPorNombreUsuario(String nombreUsuario) throws SQLException;
    Set<String> obtenerPermisos(long idUsuario) throws SQLException;
}