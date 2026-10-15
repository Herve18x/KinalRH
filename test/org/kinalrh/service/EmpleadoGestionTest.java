package org.kinalrh.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import org.kinalrh.model.Empleado;
import org.kinalrh.model.Usuario;
import org.kinalrh.util.Conexion;
import org.kinalrh.util.SessionManager;

/**
 * T2.16 - Prueba de alta, edición, DPI duplicado y estado retirado
 * Responsable: Herberth.
 */
public class EmpleadoGestionTest {

    public static void main(String[] args) {
        System.out.println("=================================================================");
        System.out.println("   PRUEBA T2.16: ALTA, EDICIÓN, DPI DUPLICADO Y ESTADO RETIRADO  ");
        System.out.println("=================================================================\n");

        EmpleadoService empleadoService = new EmpleadoService();

        // Configurar sesión activa para autorizar operaciones
        Usuario adminSession = new Usuario();
        adminSession.setIdUsuario(1L);
        adminSession.setUsername("admin_herberth");
        adminSession.setRol("ADMIN");
        SessionManager.getInstance().login(adminSession);

        long suffix = System.currentTimeMillis() % 100000000;
        String dpiValido = String.format("10%07d0101", suffix);
        String nitValido = String.format("%08d", suffix);
        String correoValido = "colaborador_" + suffix + "@kinal.edu.gt";

        Long idEmpleadoCreado = null;
        try {
            // -------------------------------------------------------------
            // CASO 1: Alta de colaborador con datos válidos
            // -------------------------------------------------------------
            System.out.println("--- CASO 1: Alta de colaborador con datos válidos ---");
            Empleado nuevo = new Empleado();
            nuevo.setPrimerNombre("Juan");
            nuevo.setPrimerApellido("Pérez");
            nuevo.setDpi(dpiValido);
            nuevo.setNit(nitValido);
            nuevo.setCorreoPersonal(correoValido);
            nuevo.setIdEstadoEmpleado(1L); // 1 = ACTIVO
            nuevo.setFechaNacimiento(LocalDate.of(1995, 5, 20));

            empleadoService.guardarEmpleado(nuevo, "ADMIN");
            idEmpleadoCreado = nuevo.getIdEmpleado();

            if (idEmpleadoCreado != null && idEmpleadoCreado > 0) {
                System.out.println("   [ÉXITO] Colaborador creado con ID: " + idEmpleadoCreado);
                System.out.println("   DPI Asignado: " + nuevo.getDpi());
            } else {
                throw new IllegalStateException("Fallo en la creación de colaborador.");
            }

            // -------------------------------------------------------------
            // CASO 2: Edición de datos del colaborador
            // -------------------------------------------------------------
            System.out.println("\n--- CASO 2: Edición de datos del colaborador existente ---");
            nuevo.setSegundoNombre("Fernando");
            nuevo.setSegundoApellido("Gómez");
            nuevo.setTelefonoMovil("55551234");
            empleadoService.guardarEmpleado(nuevo, "ADMIN");

            Empleado consultado = empleadoService.buscarPorId(idEmpleadoCreado, "ADMIN");
            if (consultado != null && "Fernando".equals(consultado.getSegundoNombre()) && "55551234".equals(consultado.getTelefonoMovil())) {
                System.out.println("   [ÉXITO] Empleado ID " + idEmpleadoCreado + " editado correctamente.");
                System.out.println("   Nombres actualizados: " + consultado.getPrimerNombre() + " " + consultado.getSegundoNombre());
            } else {
                System.err.println("   [FALLO] Los cambios editados no se reflejaron.");
            }

            // -------------------------------------------------------------
            // CASO 3: Intento de alta con DPI duplicado
            // -------------------------------------------------------------
            System.out.println("\n--- CASO 3: Rechazo de alta con DPI duplicado ---");
            Empleado duplicado = new Empleado();
            duplicado.setPrimerNombre("Pedro");
            duplicado.setPrimerApellido("Ramírez");
            duplicado.setDpi(dpiValido); // Mismo DPI que el colaborador creado
            duplicado.setNit("99999999");
            duplicado.setCorreoPersonal("otro_" + suffix + "@kinal.edu.gt");
            duplicado.setIdEstadoEmpleado(1L);

            boolean dpiRechazado = false;
            try {
                empleadoService.guardarEmpleado(duplicado, "ADMIN");
                System.err.println("   [FALLO] El sistema permitió insertar un DPI duplicado.");
            } catch (IllegalArgumentException ex) {
                dpiRechazado = true;
                System.out.println("   [ÉXITO] Duplicado de DPI rechazado por servicio: " + ex.getMessage());
            }

            // -------------------------------------------------------------
            // CASO 4: Transición a estado RETIRADO
            // -------------------------------------------------------------
            System.out.println("\n--- CASO 4: Transición de estado a RETIRADO ---");
            // id_estado_empleado = 3 representa 'RETIRADO'
            nuevo.setIdEstadoEmpleado(3L);
            empleadoService.guardarEmpleado(nuevo, "ADMIN");

            Empleado empRetirado = empleadoService.buscarPorId(idEmpleadoCreado, "ADMIN");
            boolean estadoRetiradoOk = false;
            if (empRetirado != null && empRetirado.getIdEstadoEmpleado() != null && empRetirado.getIdEstadoEmpleado() == 3L) {
                estadoRetiradoOk = true;
                System.out.println("   [ÉXITO] Estado actualizado a RETIRADO (ID estado: " + empRetirado.getIdEstadoEmpleado() + ").");
            } else {
                System.err.println("   [FALLO] El estado RETIRADO no se guardó correctamente.");
            }

            // -------------------------------------------------------------
            // RESUMEN FINAL
            // -------------------------------------------------------------
            System.out.println("\n=================================================================");
            if (idEmpleadoCreado != null && dpiRechazado && estadoRetiradoOk) {
                System.out.println("   RESULTADO T2.16: TODAS LAS PRUEBAS PASARON EXITOSAMENTE [OK]  ");
            } else {
                System.err.println("   RESULTADO T2.16: FALLO EN AL MENOS UN CASO DE PRUEBA          ");
            }
            System.out.println("=================================================================");

        } finally {
            if (idEmpleadoCreado != null) {
                eliminarEmpleadoPrueba(idEmpleadoCreado);
                System.out.println("\n[LIMPIEZA] Colaborador de prueba ID " + idEmpleadoCreado + " eliminado.");
            }
            SessionManager.getInstance().logout();
        }
    }

    private static void eliminarEmpleadoPrueba(long idEmpleado) {
        try (Connection conn = Conexion.getInstancia().conectar()) {
            try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM empleado_logro WHERE id_empleado = ?")) {
                stmt.setLong(1, idEmpleado);
                stmt.executeUpdate();
            }
            try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM auditoria WHERE registro_id = ? AND entidad = 'empleado'")) {
                stmt.setLong(1, idEmpleado);
                stmt.executeUpdate();
            }
            try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM empleado WHERE id_empleado = ?")) {
                stmt.setLong(1, idEmpleado);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.println("Error al limpiar empleado prueba: " + e.getMessage());
        }
    }
}
