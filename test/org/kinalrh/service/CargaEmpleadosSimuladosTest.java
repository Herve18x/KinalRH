package org.kinalrh.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.kinalrh.model.Empleado;
import org.kinalrh.model.Usuario;
import org.kinalrh.util.Conexion;
import org.kinalrh.util.SessionManager;

/**
 * T3.04 - Casos con varios empleados de datos inventados
 * Responsable: Herberth.
 */
public class CargaEmpleadosSimuladosTest {

    public static void main(String[] args) {
        System.out.println("=================================================================");
        System.out.println("   PRUEBA T3.04: CASOS CON VARIOS EMPLEADOS DE DATOS INVENTADOS  ");
        System.out.println("=================================================================\n");

        EmpleadoService empleadoService = new EmpleadoService();

        Usuario adminSession = new Usuario();
        adminSession.setIdUsuario(1L);
        adminSession.setUsername("admin_simulados");
        adminSession.setRol("ADMIN");
        SessionManager.getInstance().login(adminSession);

        long suffix = System.currentTimeMillis() % 100000;
        List<Long> idsCreados = new ArrayList<>();

        try {
            System.out.println("--- 1. Inserción de lote de colaboradores simulados (Dataset T3.04) ---");

            // Perfil 1: Carlos Méndez (Tecnología / Desarrollador)
            Empleado e1 = new Empleado();
            e1.setPrimerNombre("Carlos");
            e1.setSegundoNombre("Alberto");
            e1.setPrimerApellido("Méndez");
            e1.setSegundoApellido("Ruiz");
            e1.setDpi(String.format("2011%05d0101", suffix + 1));
            e1.setNit(String.format("100%04d-1", suffix + 1));
            e1.setCorreoPersonal("carlos.mendez." + suffix + "@correo.com");
            e1.setIdEstadoEmpleado(1L); // ACTIVO
            e1.setFechaNacimiento(LocalDate.of(1992, 3, 15));
            empleadoService.guardarEmpleado(e1, "ADMIN");
            idsCreados.add(e1.getIdEmpleado());
            System.out.println("   [+ Creado] EMP-01: " + e1.getPrimerNombre() + " " + e1.getPrimerApellido() + " (ID: " + e1.getIdEmpleado() + ")");

            // Perfil 2: Sofía Morales (RRHH / Analista)
            Empleado e2 = new Empleado();
            e2.setPrimerNombre("Sofía");
            e2.setSegundoNombre("Elena");
            e2.setPrimerApellido("Morales");
            e2.setSegundoApellido("Castillo");
            e2.setDpi(String.format("2011%05d0101", suffix + 2));
            e2.setNit(String.format("100%04d-2", suffix + 2));
            e2.setCorreoPersonal("sofia.morales." + suffix + "@correo.com");
            e2.setIdEstadoEmpleado(1L); // ACTIVO
            e2.setFechaNacimiento(LocalDate.of(1996, 7, 22));
            empleadoService.guardarEmpleado(e2, "ADMIN");
            idsCreados.add(e2.getIdEmpleado());
            System.out.println("   [+ Creado] EMP-02: " + e2.getPrimerNombre() + " " + e2.getPrimerApellido() + " (ID: " + e2.getIdEmpleado() + ")");

            // Perfil 3: Rodrigo Fernández (Finanzas / Contador)
            Empleado e3 = new Empleado();
            e3.setPrimerNombre("Rodrigo");
            e3.setSegundoNombre("José");
            e3.setPrimerApellido("Fernández");
            e3.setSegundoApellido("Gómez");
            e3.setDpi(String.format("2011%05d0101", suffix + 3));
            e3.setNit(String.format("100%04d-3", suffix + 3));
            e3.setCorreoPersonal("rodrigo.fernandez." + suffix + "@correo.com");
            e3.setIdEstadoEmpleado(1L); // ACTIVO
            e3.setFechaNacimiento(LocalDate.of(1989, 11, 8));
            empleadoService.guardarEmpleado(e3, "ADMIN");
            idsCreados.add(e3.getIdEmpleado());
            System.out.println("   [+ Creado] EMP-03: " + e3.getPrimerNombre() + " " + e3.getPrimerApellido() + " (ID: " + e3.getIdEmpleado() + ")");

            // -------------------------------------------------------------
            // 2. Verificación de volumen y recuperación
            // -------------------------------------------------------------
            System.out.println("\n--- 2. Verificación de lectura y consistencia del lote ---");
            List<Empleado> todos = empleadoService.listarEmpleados("ADMIN");
            int encontrados = 0;
            for (Empleado emp : todos) {
                if (idsCreados.contains(emp.getIdEmpleado())) {
                    encontrados++;
                }
            }

            boolean loteCompleto = (encontrados == idsCreados.size());
            if (loteCompleto) {
                System.out.println("   [ÉXITO] Los " + idsCreados.size() + " empleados del dataset ficticio fueron recuperados sin pérdida.");
            } else {
                System.err.println("   [FALLO] Solo se recuperaron " + encontrados + " de " + idsCreados.size() + " colaboradores.");
            }

            // -------------------------------------------------------------
            // RESUMEN FINAL
            // -------------------------------------------------------------
            System.out.println("\n=================================================================");
            if (loteCompleto) {
                System.out.println("   RESULTADO T3.04: CARGA DE SIMULADOS EXITOSA [OK]              ");
            } else {
                System.err.println("   RESULTADO T3.04: FALLO EN CARGA DE SIMULADOS                  ");
            }
            System.out.println("=================================================================");

        } finally {
            // Limpieza
            for (Long id : idsCreados) {
                eliminarEmpleadoPrueba(id);
            }
            System.out.println("\n[LIMPIEZA] Los colaboradores temporales del lote fueron eliminados de BD.");
            SessionManager.getInstance().logout();
        }
    }

    private static void eliminarEmpleadoPrueba(long idEmpleado) {
        try (Connection conn = Conexion.getInstancia().conectar()) {
            try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM auditoria WHERE registro_id = ? AND entidad = 'empleado'")) {
                stmt.setLong(1, idEmpleado);
                stmt.executeUpdate();
            }
            try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM empleado WHERE id_empleado = ?")) {
                stmt.setLong(1, idEmpleado);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.println("Error en limpieza de empleado simulado: " + e.getMessage());
        }
    }
}
