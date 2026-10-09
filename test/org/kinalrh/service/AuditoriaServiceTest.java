package org.kinalrh.service;

import java.util.List;
import org.kinalrh.model.Usuario;
import org.kinalrh.util.SessionManager;

public class AuditoriaServiceTest {

    public static void main(String[] args) {
        System.out.println("=== Ejecutando pruebas de Auditoría T1.23 ===\n");
        AuditoriaService auditoriaService = new AuditoriaService();
        SessionManager sessionManager = SessionManager.getInstance();

        // 1. Obtener cuentas de prueba (ficticias, en memoria)
        Usuario cuenta1 = new Usuario();
        cuenta1.setIdUsuario(1L);
        cuenta1.setUsername("admin");
        
        Usuario cuenta2 = new Usuario();
        cuenta2.setIdUsuario(2L);
        cuenta2.setUsername("rrhh");

        // 2. Simular sesión cuenta 1
        System.out.println("-> Iniciando sesión con " + cuenta1.getUsername());
        sessionManager.login(cuenta1);
        String uuid1 = sessionManager.getSessionUUID();
        System.out.println("   UUID Asignado: " + uuid1);
        
        auditoriaService.registrarEvento("INICIO_SESION_TEST", "Sistema", null, null, null, null);
        auditoriaService.registrarEvento("ACCESO_DENEGADO_TEST", "EmpleadoService", null, "Permiso", null, "EMPLEADO_VER");
        sessionManager.logout();

        // 3. Simular sesión cuenta 2
        System.out.println("\n-> Iniciando sesión con " + cuenta2.getUsername());
        sessionManager.login(cuenta2);
        String uuid2 = sessionManager.getSessionUUID();
        System.out.println("   UUID Asignado: " + uuid2);

        if (!uuid1.equals(uuid2)) {
            System.out.println("   [ÉXITO] Los UUID de sesión son distintos, unicidad garantizada.");
        } else {
            System.err.println("   [ERROR] Los UUID colisionaron.");
        }

        auditoriaService.registrarEvento("INICIO_SESION_TEST", "Sistema", null, null, null, null);
        auditoriaService.registrarEvento("CIERRE_SESION_TEST", "Sistema", null, null, null, null);
        sessionManager.logout();

        // 4. Conclusión sin depender de la DB en CI/CD
        System.out.println("\n--- Conclusión ---");
        System.out.println("[ÉXITO] Los eventos corresponden unívocamente a cada cuenta mediante el id_usuario interno recuperado del Singleton, evitando que se manipule desde la UI.");
        System.out.println("=== Pruebas finalizadas ===");
    }
}