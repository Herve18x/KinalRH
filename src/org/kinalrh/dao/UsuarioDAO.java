package org.kinalrh.dao;
 
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.kinalrh.model.Usuario;
 
public interface UsuarioDAO {
 
    // ── Métodos CRUD de ft/seguridad ──────────────────────────────────────────
    List<Usuario> listarTodos();
    Usuario buscarPorUsername(String username);
    void guardar(Usuario usuario);
    void actualizar(Usuario usuario);
    void cambiarEstado(int id, boolean activo);
 
    // ── Métodos de Seguridad y Permisos de develop ───────────────────────────
    /**
     * Devuelve el usuario con su hash y estado, o Optional.empty() si no existe.
     * El servicio verifica la contraseña y decide si permite iniciar sesión.
     */
    Optional<Usuario> buscarPorNombreUsuario(String nombreUsuario) throws SQLException;
 
    /**
     * Devuelve códigos únicos de permisos activos de roles activos.
     * Si la cuenta no existe, está inactiva o no tiene permisos, devuelve
     * un conjunto vacío. Los errores SQL se propagan al servicio.
     */
    Set<String> obtenerPermisos(long idUsuario) throws SQLException;
}