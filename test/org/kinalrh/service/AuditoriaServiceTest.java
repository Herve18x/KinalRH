package org.kinalrh.service;

import java.util.List;
import org.kinalrh.dao.impl.UsuarioDAOImpl;
import org.kinalrh.model.Usuario;
import org.kinalrh.util.SessionManager;

public class AuditoriaServiceTest {

    public static void main(String[] args) {
        System.out.println("=== Ejecutando pruebas de Auditoría T1.23 ===\n");
        AuditoriaService auditoriaService = new AuditoriaService();
        SessionManager sessionManager = SessionManager.getInstance();
        UsuarioDAOImpl usuarioDAO = new UsuarioDAOImpl();

        // 1. Obtener cuentas reales de BD o respaldar con cuentas en memoria
        List<Usuario> usuariosBD = usuarioDAO.listarTodos();
        Usuario cuenta1;
        Usuario cuenta2;
        if (usuariosBD != null && usuariosBD.size() >= 2) {
            cuenta1 = usuariosBD.get(0);
            cuenta2 = usuariosBD.get(1);
        } else {
            cuenta1 = new Usuario();
            cuenta1.setIdUsuario(1L);
            cuenta1.setUsername("encargado_demo");

            cuenta2 = new Usuario();
            cuenta2.setIdUsuario(2L);
            cuenta2.setUsername("visor_demo");
        }

        // 2. Simular sesión cuenta 1
        System.out.println("-> Iniciando sesión con " + cuenta1.getUsername() + " (ID=" + cuenta1.getIdUsuario() + ")");
        sessionManager.login(cuenta1);
        String uuid1 = sessionManager.getSessionUUID();
        System.out.println("   UUID Asignado: " + uuid1);
        
        auditoriaService.registrarEvento("INICIO_SESION_TEST", "SESION", cuenta1.getIdUsuario(), null, null, null);
        auditoriaService.registrarEvento("ACCESO_DENEGADO_TEST", "EmpleadoService", null, "Permiso", null, "EMPLEADO_VER");
        sessionManager.logout();

        // 3. Simular sesión cuenta 2
        System.out.println("\n-> Iniciando sesión con " + cuenta2.getUsername() + " (ID=" + cuenta2.getIdUsuario() + ")");
        sessionManager.login(cuenta2);
        String uuid2 = sessionManager.getSessionUUID();
        org.kinalrh.PruebaBD.verificar(uuid1 != null && uuid2 != null && !uuid1.equals(uuid2), "Sesiones con UUID distintos");
        System.out.println("   UUID Asignado: " + uuid2);

        if (uuid1 != null && !uuid1.equals(uuid2)) {
            System.out.println("   [ÉXITO] Los UUID de sesión son distintos, unicidad garantizada.");
        } else {
            System.err.println("   [ERROR] Los UUID colisionaron.");
        }

        auditoriaService.registrarEvento("INICIO_SESION_TEST", "SESION", cuenta2.getIdUsuario(), null, null, null);
        auditoriaService.registrarEvento("CIERRE_SESION_TEST", "SESION", cuenta2.getIdUsuario(), null, null, null);
        sessionManager.logout();

        // 4. Verificar correspondencia en BD (JOIN auditoria.id_usuario = usuario.id_usuario)
        sessionManager.login(cuenta1);
        List<String> eventosRegistrados = auditoriaService.obtenerEventosConUsuario(6);
        org.kinalrh.PruebaBD.verificar(eventosRegistrados.size() >= 4, "Los cuatro eventos deben persistir");
        org.kinalrh.PruebaBD.verificar(eventosRegistrados.stream().filter(e->e.contains("ID_Usuario="+cuenta1.getIdUsuario()+" ") && e.contains(uuid1)).count()==2,
                "Dos eventos asociados a la primera cuenta y sesión");
        org.kinalrh.PruebaBD.verificar(eventosRegistrados.stream().filter(e->e.contains("ID_Usuario="+cuenta2.getIdUsuario()+" ") && e.contains(uuid2)).count()==2,
                "Dos eventos asociados a la segunda cuenta y sesión");
        if (!eventosRegistrados.isEmpty()) {
            System.out.println("\n-> Verificación de correspondencia en BD (auditoria JOIN usuario):");
            for (String ev : eventosRegistrados) {
                System.out.println("   " + ev);
            }
        }

        System.out.println("\n--- Conclusión ---");
        System.out.println("[ÉXITO] Los eventos corresponden unívocamente a cada cuenta mediante el id_usuario interno recuperado del Singleton, evitando que se manipule desde la UI.");
        System.out.println("=== Pruebas finalizadas ===");
    }
}
