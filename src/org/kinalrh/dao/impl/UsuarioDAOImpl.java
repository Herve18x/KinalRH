package org.kinalrh.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.kinalrh.dao.UsuarioDAO;
import org.kinalrh.model.Usuario;
import org.kinalrh.util.Conexion;

public class UsuarioDAOImpl implements UsuarioDAO {

    @Override
    public List<Usuario> listarTodos() {
        List<Usuario> usuarios = new ArrayList<>();
        String sql = "SELECT u.id_usuario, u.nombre_usuario, u.nombre_completo, u.correo, u.password_hash, u.activo, r.nombre AS rol " +
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
        String sql = "SELECT u.id_usuario, u.nombre_usuario, u.nombre_completo, u.correo, u.password_hash, u.activo, r.nombre AS rol " +
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
    public Optional<Usuario> buscarPorId(long id) {
        String sql = "SELECT u.id_usuario, u.nombre_usuario, u.nombre_completo, u.correo, u.password_hash, u.activo, r.nombre AS rol " +
                     "FROM usuario u " +
                     "LEFT JOIN usuario_rol ur ON u.id_usuario = ur.id_usuario " +
                     "LEFT JOIN rol r ON ur.id_rol = r.id_rol " +
                     "WHERE u.id_usuario = ?";
                     
        try (Connection conn = Conexion.getInstancia().conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearUsuario(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar usuario por ID: " + e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public void guardar(Usuario usuario) {
        String sql = "INSERT INTO usuario (nombre_usuario, password_hash, nombre_completo, correo, activo) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = Conexion.getInstancia().conectar();
             PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            conn.setAutoCommit(false);
            stmt.setString(1, usuario.getUsername());
            stmt.setString(2, usuario.getPasswordHash());
            
            String nombreCompleto = usuario.getNombre();
            if (usuario.getApellido() != null && !usuario.getApellido().trim().isEmpty()) {
                nombreCompleto = (nombreCompleto + " " + usuario.getApellido()).trim();
            }
            stmt.setString(3, nombreCompleto);
            stmt.setString(4, usuario.getCorreo());
            stmt.setInt(5, usuario.isActivo() ? 1 : 0);
            
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int idInsertado = rs.getInt(1);
                    usuario.setIdUsuario((long) idInsertado);
                    asignarRolBD(conn, idInsertado, usuario.getRol());
                }
            }
            conn.commit();
        } catch (SQLException e) {
            System.err.println("Error al guardar usuario: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public void actualizar(Usuario usuario) {
        String sql = "UPDATE usuario SET nombre_completo = ?, correo = ?, password_hash = ?, activo = ? WHERE id_usuario = ?";
        try (Connection conn = Conexion.getInstancia().conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            conn.setAutoCommit(false);
            String nombreCompleto = usuario.getNombre();
            if (usuario.getApellido() != null && !usuario.getApellido().trim().isEmpty()) {
                nombreCompleto = (nombreCompleto + " " + usuario.getApellido()).trim();
            }
            stmt.setString(1, nombreCompleto);
            stmt.setString(2, usuario.getCorreo());
            stmt.setString(3, usuario.getPasswordHash());
            stmt.setInt(4, usuario.isActivo() ? 1 : 0);
            stmt.setLong(5, usuario.getIdUsuario());
            stmt.executeUpdate();

            if (usuario.getRol() != null && !usuario.getRol().equals("SIN_ROL")) {
                String sqlDel = "DELETE FROM usuario_rol WHERE id_usuario = ?";
                try (PreparedStatement sDel = conn.prepareStatement(sqlDel)) {
                    sDel.setLong(1, usuario.getIdUsuario());
                    sDel.executeUpdate();
                }
                asignarRolBD(conn, usuario.getIdUsuario().intValue(), usuario.getRol());
            }

            conn.commit();
        } catch (SQLException e) {
            System.err.println("Error al actualizar usuario: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private void asignarRolBD(Connection conn, int idUsuario, String rol) throws SQLException {
        if (rol == null || rol.isEmpty() || rol.equals("SIN_ROL")) return;
        String sql = "INSERT INTO usuario_rol (id_usuario, id_rol) VALUES (?, (SELECT id_rol FROM rol WHERE nombre = ? LIMIT 1))";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idUsuario);
            stmt.setString(2, rol.toLowerCase()); 
            stmt.executeUpdate();
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

    public boolean autenticar(String nombreUsuario, String passwordHash) {
        String sql = "SELECT id_usuario FROM usuario WHERE nombre_usuario = ? AND password_hash = ? AND activo = TRUE";
        try (Connection conn = Conexion.getInstancia().conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nombreUsuario);
            stmt.setString(2, passwordHash);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("Error al autenticar usuario: " + e.getMessage());
            return false;
        }
    }

    private Usuario mapearUsuario(ResultSet rs) throws SQLException {
        Usuario u = new Usuario();
        u.setIdUsuario(rs.getLong("id_usuario"));
        u.setUsername(rs.getString("nombre_usuario"));
        u.setCorreo(rs.getString("correo"));

        String nombreCompleto = rs.getString("nombre_completo");
        if (nombreCompleto != null) {
            u.setNombre(nombreCompleto);
        }

        u.setPasswordHash(rs.getString("password_hash"));
        u.setActivo(rs.getInt("activo") == 1);

        String rol = rs.getString("rol");
        u.setRol(rol != null ? rol.toUpperCase() : "SIN_ROL");

        return u;
    }

    @Override
    public int contarAdministradoresActivos() {
        String sql = "SELECT COUNT(*) FROM usuario u INNER JOIN usuario_rol ur ON u.id_usuario = ur.id_usuario INNER JOIN rol r ON ur.id_rol = r.id_rol WHERE r.nombre = 'admin' AND u.activo = 1";
        try (Connection conn = Conexion.getInstancia().conectar();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error al contar admins: " + e.getMessage());
        }
        return 0;
    }

    @Override
    public java.util.Optional<Usuario> buscarPorNombreUsuario(String nombreUsuario) throws java.sql.SQLException {
        Usuario u = buscarPorUsername(nombreUsuario);
        return u != null ? java.util.Optional.of(u) : java.util.Optional.empty();
    }

    @Override
    public java.util.Set<String> obtenerPermisos(long idUsuario) throws java.sql.SQLException {
        java.util.Set<String> permisos = new java.util.HashSet<>();
        String sql = "SELECT p.nombre FROM permiso p " +
                     "INNER JOIN rol_permiso rp ON p.id_permiso = rp.id_permiso " +
                     "INNER JOIN usuario_rol ur ON rp.id_rol = ur.id_rol " +
                     "WHERE ur.id_usuario = ?";
                     
        try (Connection conn = Conexion.getInstancia().conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idUsuario);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    permisos.add(rs.getString("nombre"));
                }
            }
        }
        return permisos;
    }

    @Override
    public void activar(long id) {
        String sql = "UPDATE usuario SET activo = 1 WHERE id_usuario = ?";
        try (Connection conn = Conexion.getInstancia().conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error al activar usuario: " + e.getMessage());
        }
    }

    @Override
    public void desactivar(long id) {
        String sql = "UPDATE usuario SET activo = 0 WHERE id_usuario = ?";
        try (Connection conn = Conexion.getInstancia().conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error al desactivar usuario: " + e.getMessage());
        }
    }

    @Override
    public void asignarRol(long idUsuario, long idRol) {
        String sqlDel = "DELETE FROM usuario_rol WHERE id_usuario = ?";
        String sqlIns = "INSERT INTO usuario_rol (id_usuario, id_rol) VALUES (?, ?)";
        try (Connection conn = Conexion.getInstancia().conectar()) {
            conn.setAutoCommit(false);
            
            try (PreparedStatement sDel = conn.prepareStatement(sqlDel)) {
                sDel.setLong(1, idUsuario);
                sDel.executeUpdate();
            }
            
            try (PreparedStatement sIns = conn.prepareStatement(sqlIns)) {
                sIns.setLong(1, idUsuario);
                sIns.setLong(2, idRol);
                sIns.executeUpdate();
            }
            
            conn.commit();
        } catch (SQLException e) {
            System.err.println("Error al asignar rol: " + e.getMessage());
            throw new RuntimeException("Error en base de datos al asignar rol", e);
        }
    }
}