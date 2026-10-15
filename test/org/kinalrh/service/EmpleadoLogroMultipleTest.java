package org.kinalrh.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import org.kinalrh.model.Empleado;
import org.kinalrh.model.Logro;
import org.kinalrh.model.Usuario;
import org.kinalrh.util.Conexion;
import org.kinalrh.util.SessionManager;

/**
 * T3.17 - Prueba de tipo, fecha y dos logros en la misma ficha
 * Responsable: Herberth.
 */
public class EmpleadoLogroMultipleTest {

    public static void main(String[] args) {
        System.out.println("=================================================================");
        System.out.println("   PRUEBA T3.17: TIPO, FECHA Y DOS LOGROS EN LA MISMA FICHA      ");
        System.out.println("=================================================================\n");

        EmpleadoService empleadoService = new EmpleadoService();
        LogroService logroService = new LogroService();

        Usuario adminSession = new Usuario();
        adminSession.setIdUsuario(1L);
        adminSession.setUsername("admin_logros");
        adminSession.setRol("ADMIN");
        SessionManager.getInstance().login(adminSession);

        long suffix = System.currentTimeMillis() % 100000;
        Long idEmpleadoPrueba = null;
        Long idLogro1 = null;
        Long idLogro2 = null;

        try {
            // -------------------------------------------------------------
            // 1. Preparar colaborador de prueba
            // -------------------------------------------------------------
            System.out.println("--- 1. Creación de colaborador base para la prueba ---");
            Empleado emp = new Empleado();
            emp.setPrimerNombre("Héctor");
            emp.setPrimerApellido("Castillo");
            emp.setDpi(String.format("3010%05d0101", suffix));
            emp.setNit(String.format("200%04d-5", suffix));
            emp.setCorreoPersonal("hector.logros." + suffix + "@kinal.edu.gt");
            emp.setIdEstadoEmpleado(1L);
            emp.setFechaNacimiento(LocalDate.of(1994, 4, 12));

            empleadoService.guardarEmpleado(emp, "ADMIN");
            idEmpleadoPrueba = emp.getIdEmpleado();
            System.out.println("   [ÉXITO] Colaborador creado con ID: " + idEmpleadoPrueba + " (" + emp.getPrimerNombre() + " " + emp.getPrimerApellido() + ")");

            // -------------------------------------------------------------
            // 2. Registrar Logro 1 (Tipo: CERTIFICACION, Fecha: 2024-03-10)
            // -------------------------------------------------------------
            System.out.println("\n--- 2. Registro del Primer Logro en la ficha ---");
            Logro l1 = new Logro();
            l1.setIdTipoLogro(2L); // 2 = CERTIFICACION
            l1.setNombre("Certificación Scrum Master Professional");
            l1.setDescripcion("Acreditación en metodologías ágiles");
            l1.setInstitucion("Scrum Alliance");
            l1.setFechaObtencion(LocalDate.of(2024, 3, 10));
            l1.setOrigen("PROPIO");
            l1.setFinanciadoPorKinal(true);
            l1.setActivo(true);

            idLogro1 = logroService.guardarLogroDeColaborador(idEmpleadoPrueba, l1, false, "Primer logro evaluado", "ADMIN");
            System.out.println("   [ÉXITO] Logro 1 registrado con ID: " + idLogro1 + 
                               " | Tipo ID: 2 (Certificación) | Fecha: " + l1.getFechaObtencion());

            // -------------------------------------------------------------
            // 3. Registrar Logro 2 en la MISMA ficha (Tipo: PREMIO, Fecha: 2024-09-15)
            // -------------------------------------------------------------
            System.out.println("\n--- 3. Registro del Segundo Logro en la MISMA ficha ---");
            Logro l2 = new Logro();
            l2.setIdTipoLogro(6L); // 6 = PREMIO
            l2.setNombre("Reconocimiento a la Excelencia Docente 2024");
            l2.setDescripcion("Distinción al mérito por desempeño sobresaliente");
            l2.setInstitucion("Fundación Kinal");
            l2.setFechaObtencion(LocalDate.of(2024, 9, 15));
            l2.setOrigen("KINAL");
            l2.setFinanciadoPorKinal(false);
            l2.setActivo(true);

            idLogro2 = logroService.guardarLogroDeColaborador(idEmpleadoPrueba, l2, true, "Segundo logro evaluado (destacado)", "ADMIN");
            System.out.println("   [ÉXITO] Logro 2 registrado con ID: " + idLogro2 + 
                               " | Tipo ID: 6 (Premio) | Fecha: " + l2.getFechaObtencion());

            // -------------------------------------------------------------
            // 4. Verificación de coexistencia e integridad de ambos logros
            // -------------------------------------------------------------
            System.out.println("\n--- 4. Verificación de consulta y coexistencia en la ficha ---");
            List<Logro> logrosRecuperados = logroService.listarLogrosPorColaborador(idEmpleadoPrueba, "ADMIN");
            System.out.println("   Total de logros recuperados para el colaborador: " + logrosRecuperados.size());

            boolean tieneLogro1 = false;
            boolean tieneLogro2 = false;
            for (Logro l : logrosRecuperados) {
                if (l.getIdLogro() != null && l.getIdLogro().equals(idLogro1)) {
                    tieneLogro1 = true;
                    System.out.println("   -> Verificado Logro 1: " + l.getNombre() + " (Tipo: " + l.getIdTipoLogro() + ", Fecha: " + l.getFechaObtencion() + ")");
                }
                if (l.getIdLogro() != null && l.getIdLogro().equals(idLogro2)) {
                    tieneLogro2 = true;
                    System.out.println("   -> Verificado Logro 2: " + l.getNombre() + " (Tipo: " + l.getIdTipoLogro() + ", Fecha: " + l.getFechaObtencion() + ")");
                }
            }

            boolean dosLogrosExitosos = (tieneLogro1 && tieneLogro2 && logrosRecuperados.size() >= 2);
            if (dosLogrosExitosos) {
                System.out.println("   [ÉXITO] Ambos logros coexisten correctamente en la misma ficha con sus fechas y tipos íntegros.");
            } else {
                System.err.println("   [FALLO] No se encontraron ambos logros asociados al colaborador.");
            }

            // -------------------------------------------------------------
            // RESUMEN FINAL
            // -------------------------------------------------------------
            System.out.println("\n=================================================================");
            if (dosLogrosExitosos) {
                System.out.println("   RESULTADO T3.17: PRUEBA DE DOS LOGROS PASÓ EXITOSAMENTE [OK] ");
            } else {
                System.err.println("   RESULTADO T3.17: FALLO EN VERIFICACIÓN DE LOGROS MULTIPLES   ");
            }
            System.out.println("=================================================================");

        } finally {
            // Limpieza
            if (idEmpleadoPrueba != null) {
                eliminarLogrosYEmpleado(idEmpleadoPrueba, idLogro1, idLogro2);
                System.out.println("\n[LIMPIEZA] Colaborador y logros de prueba eliminados correctamente.");
            }
            SessionManager.getInstance().logout();
        }
    }

    private static void eliminarLogrosYEmpleado(long idEmpleado, Long idLogro1, Long idLogro2) {
        try (Connection conn = Conexion.getInstancia().conectar()) {
            try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM empleado_logro WHERE id_empleado = ?")) {
                stmt.setLong(1, idEmpleado);
                stmt.executeUpdate();
            }
            if (idLogro1 != null) {
                try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM logro WHERE id_logro = ?")) {
                    stmt.setLong(1, idLogro1);
                    stmt.executeUpdate();
                }
            }
            if (idLogro2 != null) {
                try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM logro WHERE id_logro = ?")) {
                    stmt.setLong(1, idLogro2);
                    stmt.executeUpdate();
                }
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
            System.err.println("Error en limpieza de logros y empleado: " + e.getMessage());
        }
    }
}
