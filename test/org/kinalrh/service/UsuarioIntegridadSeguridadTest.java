package org.kinalrh.service;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.kinalrh.PruebaBD;
import org.kinalrh.dao.impl.UsuarioDAOImpl;
import org.kinalrh.model.Usuario;
import org.kinalrh.util.PasswordHasher;
import org.kinalrh.util.SessionManager;
import static org.kinalrh.PruebaBD.verificar;

public class UsuarioIntegridadSeguridadTest {
    public static void main(String[] args) throws Exception {
        try (PruebaBD bd = new PruebaBD(Path.of(args[0]), true)) {
            ImportacionXlsxServiceTest.preparar(bd);
            bd.sql("INSERT IGNORE INTO usuario_rol SELECT 1,id_rol FROM rol WHERE nombre='admin'");
            UsuarioService servicio = new UsuarioService();
            UsuarioDAOImpl dao = new UsuarioDAOImpl();
            List<String> fallos = new ArrayList<>();
            List<Runnable> bajas = List.of(
                    () -> servicio.cambiarEstado(1, false, "ADMIN"),
                    () -> servicio.desactivar(1, "ADMIN"),
                    () -> { Usuario u = dao.buscarPorId(1).orElseThrow(); u.setActivo(false); servicio.actualizarUsuario(u, "ADMIN"); },
                    () -> servicio.asignarRol(1, 6, "ADMIN"),
                    () -> { Usuario u = dao.buscarPorId(1).orElseThrow(); u.setRol("visor"); servicio.actualizarUsuario(u, "ADMIN"); });
            for (int i = 0; i < bajas.size(); i++) {
                boolean rechazo = false;
                try { bajas.get(i).run(); } catch (IllegalStateException esperado) { rechazo = true; }
                if (!rechazo || dao.contarAdministradoresActivos() != 1) fallos.add("Último administrador: vía " + (i + 1));
                bd.sql("UPDATE usuario SET activo=TRUE WHERE id_usuario=1");
                bd.sql("DELETE FROM usuario_rol WHERE id_usuario=1");
                bd.sql("INSERT INTO usuario_rol SELECT 1,id_rol FROM rol WHERE nombre='admin'");
            }

            Usuario primero = usuario("sin_correo_1", "");
            servicio.guardarUsuario(primero, "ADMIN");
            comprobar(fallos, "Hash nuevo con PBKDF2", () -> verificar(PasswordHasher.verifyPassword("ClaveFicticia!123", dao.buscarPorId(primero.getIdUsuario()).orElseThrow().getPasswordHash()), "Hash seguro"));
            comprobar(fallos, "Dos cuentas sin correo", () -> servicio.guardarUsuario(usuario("sin_correo_2", "  "), "ADMIN"));
            Usuario correo = usuario("correo_base", "duplicado@example.test");
            servicio.guardarUsuario(correo, "ADMIN");
            ByteArrayOutputStream registro = new ByteArrayOutputStream();
            PrintStream anterior = System.err;
            RuntimeException capturada = null;
            try (PrintStream captura = new PrintStream(registro)) {
                System.setErr(captura);
                try { servicio.guardarUsuario(usuario("correo_duplicado", "duplicado@example.test"), "ADMIN"); }
                catch (RuntimeException ex) { capturada = ex; }
            } finally { System.setErr(anterior); }
            verificar(capturada != null, "Correo duplicado rechazado");
            String mensajes = registro.toString(java.nio.charset.StandardCharsets.UTF_8);
            for (Throwable ex = capturada; ex != null; ex = ex.getCause()) mensajes += " " + ex.getMessage();
            if (mensajes.contains("duplicado@example.test") || mensajes.contains("Duplicate entry") || mensajes.contains("uq_usuario")) fallos.add("El error expone correo o SQL");
            verificar(bd.numero("SELECT COUNT(*) FROM usuario WHERE nombre_usuario='correo_duplicado'") == 0, "Sin alta parcial");

            comprobar(fallos, "Correo inválido", () -> {
                try { servicio.guardarUsuario(usuario("correo_malo", "a@b c"), "ADMIN"); throw new AssertionError("Correo inválido aceptado"); }
                catch (IllegalArgumentException esperado) { }
            });
            comprobar(fallos, "Rol inactivo rechazado", () -> {
                bd.sql("UPDATE rol SET activo=FALSE WHERE nombre='visor'");
                try { servicio.guardarUsuario(usuario("rol_inactivo", null), "ADMIN"); throw new AssertionError("Rol inactivo asignado"); }
                catch (IllegalArgumentException esperado) { }
                finally { bd.sql("UPDATE rol SET activo=TRUE WHERE nombre='visor'"); }
            });
            bd.sql("INSERT INTO usuario_rol SELECT 2,id_rol FROM rol WHERE nombre IN ('visor','admin')");
            comprobar(fallos, "Una fila por cuenta con varios roles", () -> verificar(dao.listarTodos().stream().filter(u -> u.getIdUsuario() == 2L).count() == 1, "Cuenta única"));

            // Con un segundo administrador, el cambio válido sigue funcionando.
            servicio.cambiarEstado(1, false, "ADMIN");
            verificar(!dao.buscarPorId(1).orElseThrow().isActivo(), "Baja válida con otro administrador");
            bd.sql("UPDATE usuario SET activo=TRUE WHERE id_usuario=1");
            fallos.forEach(f -> System.out.println("FAIL: " + f));
            verificar(fallos.isEmpty(), "Fallos de usuarios: " + fallos.size());
            SessionManager.getInstance().logout();
            System.out.println("UsuarioIntegridadSeguridadTest: última cuenta admin, hashes, correo, errores y roles correctos.");
        }
    }

    private static Usuario usuario(String nombre, String correo) {
        Usuario u = new Usuario(); u.setUsername(nombre); u.setNombre("Cuenta ficticia");
        u.setCorreo(correo); u.setPasswordHash("ClaveFicticia!123"); u.setRol("visor");
        return u;
    }
    private interface Caso { void ejecutar() throws Exception; }
    private static void comprobar(List<String> fallos, String nombre, Caso caso) {
        try { caso.ejecutar(); } catch (Exception | AssertionError ex) { fallos.add(nombre + ": " + ex.getClass().getSimpleName()); }
    }
}
