package org.kinalrh.dao.impl;

import java.sql.*;
import java.util.*;
import org.kinalrh.dao.UsuarioDAO;
import org.kinalrh.model.Rol;
import org.kinalrh.model.Usuario;
import org.kinalrh.util.Conexion;

public class UsuarioDAOImpl implements UsuarioDAO {
    private static final String CONSULTA = "SELECT u.id_usuario,u.nombre_usuario,u.nombre_completo,u.correo,"
            + "u.password_hash,u.activo,u.id_empleado,r.id_rol,r.nombre AS rol FROM usuario u "
            + "LEFT JOIN usuario_rol ur ON u.id_usuario=ur.id_usuario "
            + "LEFT JOIN rol r ON r.id_rol=ur.id_rol AND r.activo=TRUE ";
    private static final String ORDEN = " ORDER BY u.id_usuario,CASE WHEN LOWER(r.nombre)='admin' THEN 0 ELSE 1 END,r.id_rol";

    private List<Usuario> consultar(String filtro, Object parametro) {
        Map<Long,Usuario> usuarios = new LinkedHashMap<>();
        try (Connection c = Conexion.getInstancia().conectar(); PreparedStatement s = c.prepareStatement(CONSULTA + filtro + ORDEN)) {
            if (parametro != null) s.setObject(1, parametro);
            try (ResultSet r = s.executeQuery()) {
                while (r.next()) {
                    long id = r.getLong("id_usuario");
                    Usuario u = usuarios.get(id);
                    if (u == null) {
                        u = new Usuario(); u.setIdUsuario(id); u.setUsername(r.getString("nombre_usuario"));
                        u.setNombreCompleto(r.getString("nombre_completo")); u.setCorreo(r.getString("correo"));
                        u.setPasswordHash(r.getString("password_hash")); u.setActivo(r.getBoolean("activo"));
                        long empleado = r.getLong("id_empleado");
                        if (!r.wasNull()) u.setIdEmpleado(empleado);
                        usuarios.put(id, u);
                    }
                    if (r.getString("rol") != null) {
                        Rol rol = new Rol(); rol.setIdRol(r.getLong("id_rol"));
                        rol.setNombre(r.getString("rol").toUpperCase(Locale.ROOT)); u.getRoles().add(rol);
                    }
                }
            }
            return new ArrayList<>(usuarios.values());
        } catch (SQLException ex) { throw errorSeguro(ex); }
    }

    @Override public List<Usuario> listarTodos() { return consultar("", null); }
    @Override public Usuario buscarPorUsername(String username) {
        if (username == null) return null;
        return consultar("WHERE u.nombre_usuario=?", username).stream().findFirst().orElse(null);
    }
    @Override public Optional<Usuario> buscarPorId(long id) {
        return consultar("WHERE u.id_usuario=?", id).stream().findFirst();
    }
    @Override public Optional<Usuario> buscarPorNombreUsuario(String nombre) {
        return Optional.ofNullable(buscarPorUsername(nombre));
    }

    @FunctionalInterface private interface Escritura { void ejecutar(Connection c) throws SQLException; }
    private void transaccion(Escritura escritura) {
        try (Connection c = Conexion.getInstancia().conectar()) {
            boolean original = c.getAutoCommit(); c.setAutoCommit(false);
            try {
                // Bloqueo común: dos bajas concurrentes no pueden retirar a ambos administradores.
                try (PreparedStatement s = c.prepareStatement("SELECT id_rol FROM rol WHERE nombre='admin' FOR UPDATE"); ResultSet r = s.executeQuery()) {
                    while (r.next()) { /* consumir el bloqueo */ }
                }
                escritura.ejecutar(c); c.commit();
            } catch (SQLException | RuntimeException ex) {
                try { c.rollback(); } catch (SQLException rollback) { ex.addSuppressed(rollback); }
                throw ex;
            } finally { c.setAutoCommit(original); }
        } catch (SQLException ex) { throw errorSeguro(ex); }
    }

    private static RuntimeException errorSeguro(SQLException ex) {
        if (ex.getErrorCode() == 1062) return new IllegalArgumentException("Ya existe una cuenta con ese usuario, correo o colaborador.");
        return new IllegalStateException("No se pudo completar la operación de usuarios. Revise la conexión y los datos.");
    }

    @Override public void guardar(Usuario u) {
        Long anterior = u.getIdUsuario();
        try {
            transaccion(c -> {
                Long rol = resolverRol(c, u.getRol());
                try (PreparedStatement s = c.prepareStatement("INSERT INTO usuario(nombre_usuario,password_hash,nombre_completo,correo,activo,id_empleado) VALUES (?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS)) {
                    s.setString(1,u.getUsername()); s.setString(2,u.getPasswordHash()); s.setString(3,u.getNombreCompleto());
                    s.setString(4,u.getCorreo()); s.setBoolean(5,u.isActivo()); asignarEmpleado(s,6,u.getIdEmpleado());
                    s.executeUpdate();
                    try (ResultSet r = s.getGeneratedKeys()) {
                        if (!r.next()) throw new SQLException("Sin identificador");
                        u.setIdUsuario(r.getLong(1));
                    }
                }
                if (rol != null) insertarRol(c,u.getIdUsuario(),rol);
            });
        } catch (RuntimeException ex) { u.setIdUsuario(anterior); throw ex; }
    }

    @Override public void actualizar(Usuario u) {
        transaccion(c -> {
            Long rol = resolverRol(c,u.getRol());
            activo(c,u.getIdUsuario());
            conservarAdministrador(c,u.getIdUsuario(),u.isActivo(),rol);
            try (PreparedStatement s = c.prepareStatement("UPDATE usuario SET nombre_usuario=?,nombre_completo=?,correo=?,password_hash=?,activo=?,id_empleado=? WHERE id_usuario=?")) {
                s.setString(1,u.getUsername()); s.setString(2,u.getNombreCompleto()); s.setString(3,u.getCorreo());
                s.setString(4,u.getPasswordHash()); s.setBoolean(5,u.isActivo()); asignarEmpleado(s,6,u.getIdEmpleado());
                s.setLong(7,u.getIdUsuario()); s.executeUpdate();
            }
            if (rol != null) reemplazarRol(c,u.getIdUsuario(),rol);
        });
    }

    private void asignarEmpleado(PreparedStatement s,int indice,Long empleado) throws SQLException {
        if (empleado == null || empleado <= 0) s.setNull(indice,Types.BIGINT);
        else s.setLong(indice,empleado);
    }

    private Long resolverRol(Connection c,String nombre) throws SQLException {
        if (nombre == null || nombre.isBlank() || "SIN_ROL".equalsIgnoreCase(nombre)) return null;
        try (PreparedStatement s = c.prepareStatement("SELECT id_rol FROM rol WHERE nombre=? AND activo=TRUE FOR UPDATE")) {
            s.setString(1,nombre);
            try (ResultSet r = s.executeQuery()) {
                if (!r.next()) throw new IllegalArgumentException("El rol no existe o se encuentra inactivo.");
                return r.getLong(1);
            }
        }
    }
    private boolean activo(Connection c,long id) throws SQLException {
        try (PreparedStatement s = c.prepareStatement("SELECT activo FROM usuario WHERE id_usuario=? FOR UPDATE")) {
            s.setLong(1,id);
            try (ResultSet r = s.executeQuery()) {
                if (!r.next()) throw new IllegalArgumentException("El usuario no existe.");
                return r.getBoolean(1);
            }
        }
    }
    private boolean administrador(Connection c,long id) throws SQLException {
        try (PreparedStatement s = c.prepareStatement("SELECT 1 FROM usuario u JOIN usuario_rol ur ON ur.id_usuario=u.id_usuario JOIN rol r ON r.id_rol=ur.id_rol WHERE u.id_usuario=? AND u.activo=TRUE AND r.activo=TRUE AND r.nombre='admin'")) {
            s.setLong(1,id); try (ResultSet r = s.executeQuery()) { return r.next(); }
        }
    }
    private int contarAdministradores(Connection c) throws SQLException {
        try (PreparedStatement s = c.prepareStatement("SELECT COUNT(DISTINCT u.id_usuario) FROM usuario u JOIN usuario_rol ur ON ur.id_usuario=u.id_usuario JOIN rol r ON r.id_rol=ur.id_rol WHERE u.activo=TRUE AND r.activo=TRUE AND r.nombre='admin'"); ResultSet r = s.executeQuery()) {
            r.next(); return r.getInt(1);
        }
    }
    private void conservarAdministrador(Connection c,long id,boolean activoNuevo,Long rolNuevo) throws SQLException {
        boolean conservaRol = rolNuevo == null;
        if (rolNuevo != null) {
            try (PreparedStatement s = c.prepareStatement("SELECT 1 FROM rol WHERE id_rol=? AND nombre='admin' AND activo=TRUE")) {
                s.setLong(1,rolNuevo); try (ResultSet r = s.executeQuery()) { conservaRol = r.next(); }
            }
        }
        if ((!activoNuevo || !conservaRol) && administrador(c,id) && contarAdministradores(c) <= 1) {
            throw new IllegalStateException("No se puede desactivar ni quitar el rol a la última cuenta administradora activa.");
        }
    }
    private void insertarRol(Connection c,long usuario,long rol) throws SQLException {
        try (PreparedStatement s = c.prepareStatement("INSERT INTO usuario_rol(id_usuario,id_rol) VALUES (?,?)")) {
            s.setLong(1,usuario); s.setLong(2,rol); s.executeUpdate();
        }
    }
    private void reemplazarRol(Connection c,long usuario,long rol) throws SQLException {
        try (PreparedStatement s = c.prepareStatement("DELETE FROM usuario_rol WHERE id_usuario=?")) {
            s.setLong(1,usuario); s.executeUpdate();
        }
        insertarRol(c,usuario,rol);
    }
    private void estado(long id,boolean activoNuevo) {
        transaccion(c -> {
            activo(c,id); conservarAdministrador(c,id,activoNuevo,null);
            try (PreparedStatement s = c.prepareStatement("UPDATE usuario SET activo=? WHERE id_usuario=?")) {
                s.setBoolean(1,activoNuevo); s.setLong(2,id); s.executeUpdate();
            }
        });
    }
    @Override public void cambiarEstado(int id,boolean estado) { estado(id,estado); }
    @Override public void activar(long id) { estado(id,true); }
    @Override public void desactivar(long id) { estado(id,false); }
    @Override public int contarAdministradoresActivos() {
        try (Connection c = Conexion.getInstancia().conectar()) { return contarAdministradores(c); }
        catch (SQLException ex) { throw errorSeguro(ex); }
    }
    @Override public void asignarRol(long usuario,long rol) {
        transaccion(c -> {
            boolean activoUsuario = activo(c,usuario);
            try (PreparedStatement s = c.prepareStatement("SELECT 1 FROM rol WHERE id_rol=? AND activo=TRUE FOR UPDATE")) {
                s.setLong(1,rol); try (ResultSet r = s.executeQuery()) {
                    if (!r.next()) throw new IllegalArgumentException("El rol no existe o se encuentra inactivo.");
                }
            }
            conservarAdministrador(c,usuario,activoUsuario,rol); reemplazarRol(c,usuario,rol);
        });
    }

    public boolean autenticar(String nombreUsuario,String passwordHash) {
        Usuario u = buscarPorUsername(nombreUsuario);
        return u != null && u.isActivo() && Objects.equals(u.getPasswordHash(),passwordHash);
    }

    @Override public Set<String> obtenerPermisos(long usuario) throws SQLException {
        Set<String> permisos = new HashSet<>();
        String sql = "SELECT p.codigo FROM permiso p JOIN rol_permiso rp ON p.id_permiso=rp.id_permiso "
                + "JOIN usuario_rol ur ON rp.id_rol=ur.id_rol JOIN rol r ON r.id_rol=ur.id_rol JOIN usuario u ON u.id_usuario=ur.id_usuario "
                + "WHERE ur.id_usuario=? AND u.activo=TRUE AND r.activo=TRUE AND p.activo=TRUE";
        try (Connection c = Conexion.getInstancia().conectar(); PreparedStatement s = c.prepareStatement(sql)) {
            s.setLong(1,usuario); try (ResultSet r = s.executeQuery()) { while (r.next()) permisos.add(r.getString(1)); }
        }
        return permisos;
    }

    @Override public List<org.kinalrh.model.ColaboradorSinRol> listarColaboradoresSinRol() {
        List<org.kinalrh.model.ColaboradorSinRol> lista = new ArrayList<>();
        String sql = "SELECT e.id_empleado,e.dpi,e.nombre_completo,e.correo_personal,u.id_usuario,u.nombre_usuario "
                + "FROM empleado e LEFT JOIN usuario u ON u.id_empleado=e.id_empleado "
                + "WHERE u.id_usuario IS NULL OR NOT EXISTS (SELECT 1 FROM usuario_rol ur JOIN rol r ON r.id_rol=ur.id_rol AND r.activo=TRUE WHERE ur.id_usuario=u.id_usuario) ORDER BY e.nombre_completo";
        try (Connection c = Conexion.getInstancia().conectar(); PreparedStatement s = c.prepareStatement(sql); ResultSet r = s.executeQuery()) {
            while (r.next()) {
                long id = r.getLong("id_usuario"); Long usuario = r.wasNull() ? null : id;
                lista.add(new org.kinalrh.model.ColaboradorSinRol(r.getLong("id_empleado"),r.getString("dpi"),r.getString("nombre_completo"),
                        r.getString("correo_personal"),usuario,r.getString("nombre_usuario"),usuario == null
                                ? "Colaborador sin cuenta ni rol asignado" : "Colaborador con cuenta sin rol asignado"));
            }
        } catch (SQLException ex) { throw errorSeguro(ex); }
        return lista;
    }
}
