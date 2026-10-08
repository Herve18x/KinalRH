package org.kinalrh.dao.Impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.kinalrh.dao.UsuarioDAO;
import org.kinalrh.model.Usuario;
import org.kinalrh.util.Conexion;

public class UsuarioDAOImpl implements UsuarioDAO {

    @Override
    public List<Usuario> listarTodos() {
        List<Usuario> usuarios = new ArrayList<>();
        String sql = "SELECT u.id_usuario, u.nombre_usuario, u.nombre_completo, u.password_hash, u.activo, r.nombre AS rol " +
                     "FROM usuario u " +
                     "LEFT JOIN usuario_rol ur ON u.id_usuario = ur.id_usuario " +
                     "LEFT JOIN rol r ON ur.id_rol = r.id_rol";
                     
        try (Connection conn = Conexion.getInstancia().conectar();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
             
            while (rs.next()) {
                usuarios.add(mapearUsuario(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al listar usuarios: " + e.getMessage());
        }
        return usuarios;
    }

    @Override
    public Usuario buscarPorUsername(String username) {
        String sql = "SELECT u.id_usuario, u.nombre_usuario, u.nombre_completo, u.password_hash, u.activo, r.nombre AS rol " +
                     "FROM usuario u " +
                     "LEFT JOIN usuario_rol ur ON u.id_usuario = ur.id_usuario " +
                     "LEFT JOIN rol r ON ur.id_rol = r.id_rol " +
                     "WHERE u.nombre_usuario = ?";
                     
        try (Connection conn = Conexion.getInstancia().conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
             
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearUsuario(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar usuario: " + e.getMessage());
        }
        return null;
    }

    @Override
    public void guardar(Usuario usuario) {
        // Implementación básica, asumiendo inserción simple. 
        // En la vida real se maneja la transacción para insertar en usuario y usuario_rol.
        String sql = "INSERT INTO usuario (nombre_usuario, password_hash, nombre_completo, activo) VALUES (?, ?, ?, ?)";
        try (Connection conn = Conexion.getInstancia().conectar();
             PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
             
            stmt.setString(1, usuario.getUsername());
            stmt.setString(2, usuario.getPasswordHash());
            stmt.setString(3, usuario.getNombre()); // usando nombre como nombre_completo
            stmt.setInt(4, usuario.isActivo() ? 1 : 0);
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            System.err.println("Error al guardar usuario: " + e.getMessage());
        }
    }

    @Override
    public void actualizar(Usuario usuario) {
        String sql = "UPDATE usuario SET nombre_completo = ?, password_hash = ?, activo = ? WHERE id_usuario = ?";
        try (Connection conn = Conexion.getInstancia().conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
             
            stmt.setString(1, usuario.getNombre());
            stmt.setString(2, usuario.getPasswordHash());
            stmt.setInt(3, usuario.isActivo() ? 1 : 0);
            stmt.setInt(4, usuario.getId());
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            System.err.println("Error al actualizar usuario: " + e.getMessage());
        }
    }

    @Override
    public void cambiarEstado(int id, boolean activo) {
        String sql = "UPDATE usuario SET activo = ? WHERE id_usuario = ?";
        try (Connection conn = Conexion.getInstancia().conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
             
            stmt.setInt(1, activo ? 1 : 0);
            stmt.setInt(2, id);
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            System.err.println("Error al cambiar estado: " + e.getMessage());
        }
    }
    
    private Usuario mapearUsuario(ResultSet rs) throws SQLException {
        Usuario u = new Usuario();
        u.setId(rs.getInt("id_usuario"));
        u.setUsername(rs.getString("nombre_usuario"));
        
        String nombreCompleto = rs.getString("nombre_completo");
        if (nombreCompleto != null) {
            u.setNombre(nombreCompleto);
            u.setApellido(""); // El backend unificó a nombre_completo
        }
        
        u.setPasswordHash(rs.getString("password_hash"));
        u.setActivo(rs.getInt("activo") == 1);
        
        String rol = rs.getString("rol");
        u.setRol(rol != null ? rol.toUpperCase() : "SIN_ROL");
        
        return u;
    }
}
