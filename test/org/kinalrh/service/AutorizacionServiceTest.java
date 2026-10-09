package org.kinalrh.service;

import java.util.List;
import org.kinalrh.model.Empleado;
import org.kinalrh.model.Usuario;

/**
 * T1.19: Casos de cuenta inactiva y servicio invocado sin permiso
 */
public class AutorizacionServiceTest {

    public static void main(String[] args) {
        System.out.println("=== Ejecutando pruebas de Seguridad T1.19 ===");
        EmpleadoService empleadoService = new EmpleadoService();

        // CASO 1: Cuenta inactiva (a nivel UI se rechaza en LoginController, pero a nivel servicio
        // verificamos que si alguien invoca un servicio con un rol inválido o cuenta inhabilitada, falla).
        // En nuestro caso, probaremos el Login si existiera un servicio, pero validaremos con EmpleadoService.
        System.out.println("\n--- Caso 1: Cuenta inactiva / Sin Permiso ---");
        try {
            // Un VISOR sin permisos o rol inventado 'VISOR_INACTIVO'
            empleadoService.listarEmpleados("VISOR_INACTIVO");
            System.err.println("Fallo: Se esperada SecurityException para VISOR_INACTIVO");
        } catch (SecurityException e) {
            System.out.println("ÉXITO Caso 1: " + e.getMessage());
        }

        System.out.println("\n--- Caso 2: Intento sin permiso EMPLEADO_VER ---");
        try {
            // Asumimos que el rol 'EXTERNO' o un rol básico no tiene el permiso EMPLEADO_VER
            empleadoService.listarEmpleados("EXTERNO");
            System.err.println("Fallo: Se esperaba SecurityException por falta de permiso");
        } catch (SecurityException e) {
            System.out.println("ÉXITO Caso 2: " + e.getMessage());
        }

        System.out.println("\n--- Caso 3: Invocación después de Logout (sin sesión) ---");
        try {
            // Al hacer logout, el rolActual o sesión se vuelve null o vacío
            empleadoService.listarEmpleados(null);
            System.err.println("Fallo: Se esperaba SecurityException para sesión nula");
        } catch (SecurityException e) {
            System.out.println("ÉXITO Caso 3: " + e.getMessage());
        }
        
        System.out.println("\n--- Caso 4: Cuenta con permiso válido (ADMIN) ---");
        try {
            List<Empleado> empleados = empleadoService.listarEmpleados("ADMIN");
            System.out.println("ÉXITO Caso 4: Empleados recuperados exitosamente. Total: " + empleados.size());
        } catch (SecurityException e) {
            System.err.println("Fallo: El ADMIN sí debería tener permiso.");
        }
        
        System.out.println("\n=== Pruebas finalizadas ===");
    }
}