package org.kinalrh.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.kinalrh.dao.impl.UsuarioDAOImpl;
import org.kinalrh.model.Usuario;
import org.kinalrh.util.Conexion;

/**
 * T2.04 - HU US-02 / US-49
 * Comprobar usuario/correo duplicados e ingreso tras desactivación.
 * Responsable: Herberth.
 */
public class UsuarioServiceTest {

    public static void main(String[] args) {
        System.out.println("=================================================================");
        System.out.println("   PRUEBA T2.04: USUARIO/CORREO DUPLICADOS E INGRESO TRAS BAJA   ");
        System.out.println("=================================================================\n");

        UsuarioService usuarioService = new UsuarioService();
        AutenticacionService authService = new AutenticacionService();
        UsuarioDAOImpl usuarioDAO = new UsuarioDAOImpl();

        String suffix = String.valueOf(System.currentTimeMillis() % 100000);
        String testUsername = "herberth_t204_" + suffix;
        String testEmail = "herberth_" + suffix + "@kinal.edu.gt";
        String testPassword = "PasswordTest123!";

        long conteoInicial = contarUsuariosBD();
        System.out.println("[INFO] Conteo inicial de cuentas en BD: " + conteoInicial);

        Long idCreado = null;
        try {
            // -------------------------------------------------------------
            // PASO 1: Crear una cuenta válida
            // -------------------------------------------------------------
            System.out.println("\n--- PASO 1: Creación de cuenta válida base ---");
            Usuario uValido = new Usuario();
            uValido.setUsername(testUsername);
            uValido.setNombre("Herberth");
            uValido.setApellido("Prueba");
            uValido.setCorreo(testEmail);
            uValido.setPasswordHash(testPassword);
            uValido.setRol("VISOR");
            uValido.setActivo(true);

            usuarioService.guardarUsuario(uValido, "ADMIN");
            Usuario recuperado = usuarioDAO.buscarPorUsername(testUsername);
            if (recuperado != null && recuperado.getIdUsuario() != null) {
                idCreado = recuperado.getIdUsuario();
                System.out.println("   [ÉXITO] Usuario base creado correctamente con ID: " + idCreado);
            } else {
                throw new IllegalStateException("Fallo al persistir usuario base.");
            }

            long conteoTrasAlta = contarUsuariosBD();
            System.out.println("   [VERIFICACIÓN] Conteo actual: " + conteoTrasAlta + " (Esperado: " + (conteoInicial + 1) + ")");

            // -------------------------------------------------------------
            // PASO 2: Intentar crear otro usuario con el MISMO nombre_usuario
            // -------------------------------------------------------------
            System.out.println("\n--- PASO 2: Rechazo por nombre_usuario duplicado ---");
            Usuario uDuplicadoUser = new Usuario();
            uDuplicadoUser.setUsername(testUsername); // Mismo username
            uDuplicadoUser.setNombre("Otro");
            uDuplicadoUser.setApellido("Usuario");
            uDuplicadoUser.setCorreo("otro_" + suffix + "@kinal.edu.gt");
            uDuplicadoUser.setPasswordHash("OtraPassword123!");
            uDuplicadoUser.setRol("VISOR");
            uDuplicadoUser.setActivo(true);

            boolean rechazoUsername = false;
            try {
                usuarioService.guardarUsuario(uDuplicadoUser, "ADMIN");
                System.err.println("   [FALLO] El sistema permitió registrar un nombre de usuario duplicado.");
            } catch (RuntimeException ex) {
                rechazoUsername = true;
                System.out.println("   [ÉXITO] Rechazado correctamente: " + ex.getMessage());
            }

            long conteoTrasErrorUser = contarUsuariosBD();
            if (conteoTrasErrorUser == conteoTrasAlta) {
                System.out.println("   [VERIFICACIÓN] El conteo en BD NO aumentó ante error de username duplicado.");
            } else {
                System.err.println("   [FALLO] Se creó una fila parcial en BD tras el error.");
            }

            // -------------------------------------------------------------
            // PASO 3: Intentar crear otro usuario con el MISMO correo
            // -------------------------------------------------------------
            System.out.println("\n--- PASO 3: Rechazo por correo duplicado (uq_usuario_correo) ---");
            Usuario uDuplicadoCorreo = new Usuario();
            uDuplicadoCorreo.setUsername("otro_user_" + suffix);
            uDuplicadoCorreo.setNombre("Carlos");
            uDuplicadoCorreo.setApellido("Gómez");
            uDuplicadoCorreo.setCorreo(testEmail); // Mismo correo
            uDuplicadoCorreo.setPasswordHash("OtraPassword123!");
            uDuplicadoCorreo.setRol("VISOR");
            uDuplicadoCorreo.setActivo(true);

            boolean rechazoCorreo = false;
            try {
                usuarioService.guardarUsuario(uDuplicadoCorreo, "ADMIN");
                System.err.println("   [FALLO] El sistema permitió registrar un correo duplicado.");
            } catch (Exception ex) {
                rechazoCorreo = true;
                System.out.println("   [ÉXITO] Restricción uq_usuario_correo rechazó el duplicado correctamente.");
                System.out.println("   [MENSAJE CAPTURADO] " + ex.getMessage());
            }

            long conteoTrasErrorCorreo = contarUsuariosBD();
            if (conteoTrasErrorCorreo == conteoTrasAlta) {
                System.out.println("   [VERIFICACIÓN] El conteo en BD NO aumentó ante error de correo duplicado.");
            } else {
                System.err.println("   [FALLO] Se creó una fila parcial en BD tras el error de correo.");
            }

            // -------------------------------------------------------------
            // PASO 4: Desactivación seguida de intento de login
            // -------------------------------------------------------------
            System.out.println("\n--- PASO 4: Desactivación e intento de login posterior ---");
            System.out.println("   Desactivando usuario ID " + idCreado + "...");
            usuarioService.desactivar(idCreado, "ADMIN");

            Usuario usuarioDesactivado = usuarioDAO.buscarPorId(idCreado).orElse(null);
            if (usuarioDesactivado != null && !usuarioDesactivado.isActivo()) {
                System.out.println("   [ÉXITO] Estado de cuenta actualizado a inactivo en BD.");
            } else {
                System.err.println("   [FALLO] La cuenta no quedó inactiva.");
            }

            System.out.println("   Intentando login con credenciales correctas en cuenta desactivada...");
            boolean loginBloqueado = false;
            try {
                authService.autenticar(testUsername, testPassword);
                System.err.println("   [FALLO] La cuenta desactivada pudo ingresar al sistema.");
            } catch (SecurityException ex) {
                loginBloqueado = true;
                System.out.println("   [ÉXITO] Acceso denegado a cuenta inactiva: " + ex.getMessage());
            }

            // -------------------------------------------------------------
            // RESUMEN FINAL
            // -------------------------------------------------------------
            System.out.println("\n=================================================================");
            if (rechazoUsername && rechazoCorreo && loginBloqueado) {
                System.out.println("   RESULTADO T2.04: TODAS LAS PRUEBAS PASARON EXITOSAMENTE [OK]  ");
            } else {
                System.err.println("   RESULTADO T2.04: AL MENOS UNA PRUEBA NO CUMPLIÓ EL CRITERIO   ");
            }
            System.out.println("=================================================================");

        } finally {
            // Limpieza
            if (idCreado != null) {
                eliminarUsuarioPrueba(idCreado);
                System.out.println("\n[LIMPIEZA] Usuario de prueba ID " + idCreado + " eliminado correctamente.");
            }
        }
    }

    private static long contarUsuariosBD() {
        String sql = "SELECT COUNT(*) FROM usuario";
        try (Connection conn = Conexion.getInstancia().conectar();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            System.err.println("Error al contar usuarios: " + e.getMessage());
        }
        return 0;
    }

    private static void eliminarUsuarioPrueba(long idUsuario) {
        try (Connection conn = Conexion.getInstancia().conectar()) {
            try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM usuario_rol WHERE id_usuario = ?")) {
                stmt.setLong(1, idUsuario);
                stmt.executeUpdate();
            }
            try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM auditoria WHERE id_usuario = ?")) {
                stmt.setLong(1, idUsuario);
                stmt.executeUpdate();
            }
            try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM usuario WHERE id_usuario = ?")) {
                stmt.setLong(1, idUsuario);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.println("Error en limpieza de usuario prueba: " + e.getMessage());
        }
    }
}
