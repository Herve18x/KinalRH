package org.kinalrh.service;

/**
 * Suite maestra de ejecución por lotes para todas las tareas de Herberth:
 * - T2.04: Comprobar usuario/correo duplicados e ingreso tras desactivación.
 * - T2.16: Prueba de alta, edición, DPI duplicado y estado retirado.
 * - T2.19: Contrastar lista con BD y probar autorización.
 * - T3.04: Casos con varios empleados de datos inventados.
 * - T3.17: Prueba de tipo, fecha y dos logros en la misma ficha.
 * - T3.20: Comprobar evento, usuario relacionado y permiso del flujo.
 */
public class SprintHerberthBatchTest {

    public static void main(String[] args) {
        System.out.println("#################################################################");
        System.out.println("   VERIFICACIÓN INTEGRAL DE TODAS LAS TAREAS DE HERBERTH (QA)    ");
        System.out.println("#################################################################\n");

        int exitosas = 0;
        int fallidas = 0;

        // 1. T2.04
        try {
            System.out.println(">>> EJECUTANDO T2.04 (UsuarioServiceTest) <<<");
            UsuarioServiceTest.main(new String[]{});
            exitosas++;
        } catch (Throwable t) {
            System.err.println("Error en T2.04: " + t.getMessage());
            fallidas++;
        }

        System.out.println("\n");

        // 2. T2.16
        try {
            System.out.println(">>> EJECUTANDO T2.16 (EmpleadoGestionTest) <<<");
            EmpleadoGestionTest.main(new String[]{});
            exitosas++;
        } catch (Throwable t) {
            System.err.println("Error en T2.16: " + t.getMessage());
            fallidas++;
        }

        System.out.println("\n");

        // 3. T2.19
        try {
            System.out.println(">>> EJECUTANDO T2.19 (EmpleadoListaAutorizacionTest) <<<");
            EmpleadoListaAutorizacionTest.main(new String[]{});
            exitosas++;
        } catch (Throwable t) {
            System.err.println("Error en T2.19: " + t.getMessage());
            fallidas++;
        }

        System.out.println("\n");

        // 4. T3.04
        try {
            System.out.println(">>> EJECUTANDO T3.04 (CargaEmpleadosSimuladosTest) <<<");
            CargaEmpleadosSimuladosTest.main(new String[]{});
            exitosas++;
        } catch (Throwable t) {
            System.err.println("Error en T3.04: " + t.getMessage());
            fallidas++;
        }

        System.out.println("\n");

        // 5. T3.17
        try {
            System.out.println(">>> EJECUTANDO T3.17 (EmpleadoLogroMultipleTest) <<<");
            EmpleadoLogroMultipleTest.main(new String[]{});
            exitosas++;
        } catch (Throwable t) {
            System.err.println("Error en T3.17: " + t.getMessage());
            fallidas++;
        }

        System.out.println("\n");

        // 6. T3.20
        try {
            System.out.println(">>> EJECUTANDO T3.20 (AuditoriaFlujoPermisosTest) <<<");
            AuditoriaFlujoPermisosTest.main(new String[]{});
            exitosas++;
        } catch (Throwable t) {
            System.err.println("Error en T3.20: " + t.getMessage());
            fallidas++;
        }

        System.out.println("\n#################################################################");
        System.out.println("   RESUMEN FINAL SPRINT HERBERTH: " + exitosas + " ÉXITOS, " + fallidas + " FALLOS");
        if (fallidas == 0) {
            System.out.println("   ESTADO GENERAL: TODAS LAS TAREAS DE HERBERTH CUMPLIDAS [100% OK]");
        } else {
            System.err.println("   ESTADO GENERAL: REVISAR PRUEBAS FALLIDAS");
            throw new AssertionError("Fallaron " + fallidas + " pruebas del lote.");
        }
        System.out.println("#################################################################");
    }
}
