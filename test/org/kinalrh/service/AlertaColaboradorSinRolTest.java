package org.kinalrh.service;

import java.util.List;
import org.kinalrh.dao.EmpleadoDAO;
import org.kinalrh.dao.UsuarioDAO;
import org.kinalrh.dao.impl.EmpleadoDAOImpl;
import org.kinalrh.dao.impl.UsuarioDAOImpl;
import org.kinalrh.model.ColaboradorSinRol;
import org.kinalrh.model.Empleado;
import org.kinalrh.model.Usuario;
import org.kinalrh.model.Auditoria;

public class AlertaColaboradorSinRolTest {

    public static void main(String[] args) {
        System.out.println("=================================================================");
        System.out.println("   VERIFICACIÓN: ALERTA COLABORADOR SIN ROL Y AUDITORÍA          ");
        System.out.println("=================================================================\n");

        UsuarioService usuarioService = new UsuarioService();
        UsuarioDAO usuarioDAO = new UsuarioDAOImpl();
        EmpleadoDAO empleadoDAO = new EmpleadoDAOImpl();
        AuditoriaService auditoriaService = new AuditoriaService();
        Usuario administrador = usuarioDAO.buscarPorUsername("encargado_demo");
        org.kinalrh.util.SessionManager.getInstance().login(administrador);

        try {
            // 1. Estado inicial
            List<ColaboradorSinRol> inicial = usuarioService.listarColaboradoresSinRol("ADMIN");
            System.out.println("1. Colaboradores pendientes iniciales: " + inicial.size());

            // 2. Crear un nuevo colaborador en la base de datos (simulando creación desde Colaboradores)
            Empleado nuevoColab = new Empleado();
            nuevoColab.setDpi("9999888870101");
            nuevoColab.setNit("9999888-7");
            nuevoColab.setCodigoEmpleado("EMP-TEST-99");
            nuevoColab.setPrimerNombre("Carlos");
            nuevoColab.setPrimerApellido("Mendoza");
            nuevoColab.setNombreCompleto("Carlos Mendoza");
            nuevoColab.setCorreoPersonal("carlos.mendoza.test@kinal.edu.gt");
            nuevoColab.setIdEstadoEmpleado(1L);

            long idEmp = empleadoDAO.insertar(nuevoColab);
            System.out.println("2. Colaborador creado con ID: " + idEmp + " (DPI: " + nuevoColab.getDpi() + ")");

            // 3. Verificar que listarColaboradoresSinRol detecta inmediatamente al nuevo colaborador
            List<ColaboradorSinRol> pendientes = usuarioService.listarColaboradoresSinRol("ADMIN");
            System.out.println("3. Colaboradores pendientes tras alta: " + pendientes.size());
            boolean detectado = pendientes.stream().anyMatch(c -> c.getIdEmpleado().equals(idEmp));
            if (detectado) {
                System.out.println("   [ÉXITO] Colaborador detectado como PENDIENTE de rol y cuenta.");
                ColaboradorSinRol dto = pendientes.stream().filter(c -> c.getIdEmpleado().equals(idEmp)).findFirst().get();
                System.out.println("   -> Información mostrada en alerta: " + dto.toString());
            } else {
                System.err.println("   [ERROR] El nuevo colaborador no fue detectado en la lista pendiente.");
            }

            // 4. Asignar usuario y rol al colaborador
            Usuario nuevoUsuario = new Usuario();
            nuevoUsuario.setUsername("carlos.mendoza.test");
            nuevoUsuario.setPasswordHash("pass123");
            nuevoUsuario.setNombre("Carlos Mendoza");
            nuevoUsuario.setRol("ENCARGADO");
            nuevoUsuario.setActivo(true);
            nuevoUsuario.setIdEmpleado(idEmp);

            usuarioService.guardarUsuario(nuevoUsuario, "ADMIN");
            System.out.println("4. Usuario y rol 'ENCARGADO' asignados a colaborador (ID Usuario: " + nuevoUsuario.getIdUsuario() + ")");

            // 5. Verificar que el colaborador ya NO aparece como pendiente
            List<ColaboradorSinRol> pendientesDespues = usuarioService.listarColaboradoresSinRol("ADMIN");
            boolean siguePendiente = pendientesDespues.stream().anyMatch(c -> c.getIdEmpleado().equals(idEmp));
            org.kinalrh.PruebaBD.verificar(detectado && !siguePendiente, "La alerta debe aparecer y desaparecer al asignar cuenta/rol");
            if (!siguePendiente) {
                System.out.println("5. [ÉXITO] El colaborador ya fue provisto de rol y la alerta se actualiza/oculta.");
            } else {
                System.err.println("5. [ERROR] El colaborador sigue figurando como pendiente tras asignarle rol.");
            }

            // 6. Verificar el log de auditoría
            List<Auditoria> logs = auditoriaService.listarRegistrosAuditoria(10);
            System.out.println("\n6. Verificación de módulo Auditoría (Últimos " + logs.size() + " registros):");
            for (int i = 0; i < Math.min(3, logs.size()); i++) {
                Auditoria a = logs.get(i);
                System.out.println("   -> [" + a.getFechaHoraFormateada() + "] Acción: " + a.getAccion() + 
                                   " | Entidad: " + a.getEntidad() + " | Usuario: " + a.getNombreUsuario());
            }

            // 7. Limpieza de datos de prueba
            try (java.sql.Connection conn = org.kinalrh.util.Conexion.getInstancia().conectar()) {
                conn.createStatement().executeUpdate("DELETE FROM usuario_rol WHERE id_usuario = " + nuevoUsuario.getIdUsuario());
                conn.createStatement().executeUpdate("DELETE FROM usuario WHERE id_usuario = " + nuevoUsuario.getIdUsuario());
                conn.createStatement().executeUpdate("DELETE FROM empleado WHERE id_empleado = " + idEmp);
                System.out.println("\n7. [LIMPIEZA] Datos de prueba removidos limpiamente.");
            }

            System.out.println("\n=================================================================");
            System.out.println("      TODAS LAS PRUEBAS DE ALERTA Y AUDITORÍA PASARON CON ÉXITO   ");
            System.out.println("=================================================================");

        } catch (Exception e) {
            throw new AssertionError("Falló el flujo de alerta y auditoría", e);
        }
    }
}
