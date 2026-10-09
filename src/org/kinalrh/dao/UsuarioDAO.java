package org.kinalrh.dao;
 
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.kinalrh.model.Usuario;
 
public interface UsuarioDAO {
 
    // â”€â”€ MÃ©todos CRUD de ft/seguridad â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    List<Usuario> listarTodos();
    Usuario buscarPorUsername(String username);
    Optional<Usuario> buscarPorId(long id);
    void guardar(Usuario usuario);
    void actualizar(Usuario usuario);
    void cambiarEstado(int id, boolean activo);
 
    // â”€â”€ MÃ©todos de Seguridad y Permisos de develop â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    /**
     * Devuelve el usuario con su hash y estado, o Optional.empty() si no existe.
     * El servicio verifica la contraseÃ±a y decide si permite iniciar sesiÃ³n.
     */
    Optional<Usuario> buscarPorNombreUsuario(String nombreUsuario) throws SQLException;
 
    /**
     * Devuelve cÃ³digos Ãºnicos de permisos activos de roles activos.
     * Si la cuenta no existe, estÃ¡ inactiva o no tiene permisos, devuelve
     * un conjunto vacÃ­o. Los errores SQL se propagan al servicio.
     */
    Set<String> obtenerPermisos(long idUsuario) throws SQLException;
}