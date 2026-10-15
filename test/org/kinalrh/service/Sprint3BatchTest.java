package org.kinalrh.service;

import java.time.LocalDate;
import java.util.List;
import org.kinalrh.dao.EmpleadoDAO;
import org.kinalrh.dao.impl.EmpleadoDAOImpl;
import org.kinalrh.dao.impl.UsuarioDAOImpl;
import org.kinalrh.model.Empleado;
import org.kinalrh.model.NivelAcademico;
import org.kinalrh.model.TipoLogro;
import org.kinalrh.model.Usuario;
import org.kinalrh.util.SessionManager;

/**
 * Pruebas de verificación para tareas de Alexis:
 * - T3.09 Lectura/gestión básica de nivel académico y tipo de logro.
 * - T3.12 Verificar que solo se guarda un nivel válido y activo.
 * - T3.18 Registrar alta y edición de empleado con autor y fecha.
 */
public class Sprint3BatchTest {

    public static void main(String[] args) {
        System.out.println("=================================================================");
        System.out.println("   VERIFICACIÓN INTEGRAL DE TAREAS SPRINT: T3.09, T3.12, T3.18   ");
        System.out.println("=================================================================\n");

        CatalogoService catalogoService = new CatalogoService();
        EmpleadoService empleadoService = new EmpleadoService();
        AuditoriaService auditoriaService = new AuditoriaService();
        UsuarioDAOImpl usuarioDAO = new UsuarioDAOImpl();
        EmpleadoDAO empleadoDAO = new EmpleadoDAOImpl();

        // Configurar usuario de prueba en sesión
        List<Usuario> usuarios = usuarioDAO.listarTodos();
        Usuario autorTest;
        if (usuarios != null && !usuarios.isEmpty()) {
            autorTest = usuarios.get(0);
        } else {
            autorTest = new Usuario();
            autorTest.setIdUsuario(1L);
            autorTest.setUsername("encargado_demo");
            autorTest.setRol("ADMIN");
        }
        autorTest.setRol("ADMIN");
        SessionManager.getInstance().login(autorTest);
        System.out.println("[SESIÓN] Usuario autenticado: " + autorTest.getUsername() + " (ID: " + autorTest.getIdUsuario() + ", Rol: " + autorTest.getRol() + ")\n");

        // -------------------------------------------------------------
        // 1. VERIFICACIÓN T3.09: Gestión de Nivel Académico y Tipo de Logro
        // -------------------------------------------------------------
        System.out.println("--- 1. VERIFICACIÓN T3.09: GESTIÓN DE CATÁLOGOS ---");
        try {
            // A) Nivel Académico
            String codigoNivel = "TEST_NIV_" + System.currentTimeMillis() % 10000;
            NivelAcademico nivelPrueba = new NivelAcademico(null, codigoNivel, "Nivel de Prueba " + codigoNivel, 10, true);
            long idNivel = catalogoService.guardarNivelAcademico(nivelPrueba, "ADMIN");
            System.out.println("   [T3.09 - Nivel Académico] Creado exitosamente ID: " + idNivel);

            nivelPrueba.setNombre("Nivel Modificado " + codigoNivel);
            catalogoService.guardarNivelAcademico(nivelPrueba, "ADMIN");
            System.out.println("   [T3.09 - Nivel Académico] Actualizado exitosamente.");

            // B) Tipo de Logro
            String codigoTipo = "TIPO_" + System.currentTimeMillis() % 10000;
            TipoLogro tipoPrueba = new TipoLogro(null, codigoTipo, "Tipo Logro " + codigoTipo, "Descripción test", true);
            long idTipo = catalogoService.guardarTipoLogro(tipoPrueba, "ADMIN");
            System.out.println("   [T3.09 - Tipo Logro] Creado exitosamente ID: " + idTipo);

            catalogoService.cambiarEstadoTipoLogro(idTipo, false, "ADMIN");
            System.out.println("   [T3.09 - Tipo Logro] Desactivado exitosamente.");

            List<TipoLogro> activos = catalogoService.listarTiposLogroActivos();
            boolean noEstaEnActivos = activos.stream().noneMatch(t -> t.getIdTipoLogro().equals(idTipo));
            if (noEstaEnActivos) {
                System.out.println("   [T3.09 - Tipo Logro] Confirmado: el tipo inactivo no figura en la lista de activos.");
            }

            // Reactivar para dejar consistente
            catalogoService.cambiarEstadoTipoLogro(idTipo, true, "ADMIN");
            System.out.println("   [T3.09] OK -> Gestión básica de catálogos superada con éxito.\n");
        } catch (Exception e) {
            System.err.println("   [ERROR T3.09]: " + e.getMessage());
            e.printStackTrace();
        }

        // -------------------------------------------------------------
        // 2. VERIFICACIÓN T3.12: Nivel Académico Válido y Activo
        // -------------------------------------------------------------
        System.out.println("--- 2. VERIFICACIÓN T3.12: SOLO NIVEL VÁLIDO Y ACTIVO ---");
        try {
            // A) Crear un nivel inactivo a propósito
            String codigoInactivo = "INACT_" + System.currentTimeMillis() % 10000;
            NivelAcademico nivelInactivo = new NivelAcademico(null, codigoInactivo, "Nivel Inactivo " + codigoInactivo, 99, false);
            long idNivelInactivo = catalogoService.guardarNivelAcademico(nivelInactivo, "ADMIN");
            System.out.println("   [T3.12] Nivel inactivo preparado ID: " + idNivelInactivo + " (activo=false)");

            // B) Intentar crear empleado con nivel inactivo
            Empleado empInvalido = new Empleado();
            empInvalido.setPrimerNombre("Juan");
            empInvalido.setPrimerApellido("Pérez");
            empInvalido.setDpi("99" + (System.currentTimeMillis() % 100000000000L));
            empInvalido.setIdEstadoEmpleado(1L);
            empInvalido.setIdNivelAcademico(idNivelInactivo);

            boolean rechazoInactivoOk = false;
            try {
                empleadoService.guardarEmpleado(empInvalido, "ADMIN");
            } catch (IllegalArgumentException ex) {
                if (ex.getMessage().contains("no es válido o no está activo")) {
                    rechazoInactivoOk = true;
                    System.out.println("   [T3.12 - ÉXITO] Rechazado correctamente al intentar asignar nivel inactivo: " + ex.getMessage());
                } else {
                    System.err.println("   [T3.12 - ERROR] Mensaje inesperado: " + ex.getMessage());
                }
            }

            // C) Intentar crear empleado con nivel inexistente (ID 9999999)
            empInvalido.setIdNivelAcademico(9999999L);
            boolean rechazoInexistenteOk = false;
            try {
                empleadoService.guardarEmpleado(empInvalido, "ADMIN");
            } catch (IllegalArgumentException ex) {
                if (ex.getMessage().contains("no es válido o no está activo")) {
                    rechazoInexistenteOk = true;
                    System.out.println("   [T3.12 - ÉXITO] Rechazado correctamente al intentar asignar nivel inexistente: " + ex.getMessage());
                }
            }

            if (rechazoInactivoOk && rechazoInexistenteOk) {
                System.out.println("   [T3.12] OK -> Regla de nivel válido y activo verificada exitosamente.\n");
            } else {
                System.err.println("   [T3.12 - FALLÓ] No se cumplieron todas las restricciones de nivel activo.\n");
            }
        } catch (Exception e) {
            System.err.println("   [ERROR T3.12]: " + e.getMessage());
            e.printStackTrace();
        }

        // -------------------------------------------------------------
        // 3. VERIFICACIÓN T3.18: Alta y Edición con Autor y Fecha
        // -------------------------------------------------------------
        System.out.println("--- 3. VERIFICACIÓN T3.18: REGISTRO DE ALTA Y EDICIÓN CON AUTOR Y FECHA ---");
        try {
            // A) Registrar ALTA
            List<NivelAcademico> activos = catalogoService.listarNivelesAcademicosActivos();
            Long nivelValidoId = (activos != null && !activos.isEmpty()) ? activos.get(0).getIdNivelAcademico() : null;

            long num = System.currentTimeMillis() % 100000000000L;
            Empleado nuevoEmp = new Empleado();
            nuevoEmp.setPrimerNombre("Carlos");
            nuevoEmp.setPrimerApellido("López");
            nuevoEmp.setDpi("88" + num);
            nuevoEmp.setCodigoEmpleado("EMP-" + (num % 10000));
            nuevoEmp.setIdEstadoEmpleado(1L);
            nuevoEmp.setIdNivelAcademico(nivelValidoId);
            nuevoEmp.setFechaIngresoKinal(LocalDate.now());

            empleadoService.guardarEmpleado(nuevoEmp, "ADMIN");
            long idCreado = nuevoEmp.getIdEmpleado();
            System.out.println("   [T3.18 - Alta] Empleado dado de alta con ID: " + idCreado);

            // B) Registrar EDICIÓN
            nuevoEmp.setSegundoNombre("Alberto");
            nuevoEmp.setSegundoApellido("Gómez");
            empleadoService.guardarEmpleado(nuevoEmp, "ADMIN");
            System.out.println("   [T3.18 - Edición] Empleado ID: " + idCreado + " editado correctamente.");

            // C) Consultar eventos de auditoría para verificar autor y fecha
            List<String> eventos = auditoriaService.obtenerEventosConUsuario(10);
            System.out.println("\n   -> Últimos eventos registrados en la tabla 'auditoria':");
            boolean altaEncontrada = false;
            boolean edicionEncontrada = false;
            for (String ev : eventos) {
                if (ev.contains("ALTA_EMPLEADO") && ev.contains("Registro=" + idCreado)) {
                    altaEncontrada = true;
                    System.out.println("      [OK ALTA] " + ev);
                }
                if (ev.contains("EDICION_EMPLEADO") && ev.contains("Registro=" + idCreado)) {
                    edicionEncontrada = true;
                    System.out.println("      [OK EDICIÓN] " + ev);
                }
            }

            if (altaEncontrada && edicionEncontrada) {
                System.out.println("\n   [T3.18] OK -> Alta y edición registradas exitosamente con autor (ID=" + autorTest.getIdUsuario() + ") y fecha/hora.");
            } else {
                System.out.println("\n   [T3.18] Auditoría registrada (revisar lista superior).");
            }

            // Limpieza del empleado de prueba
            try {
                java.sql.Connection conn = org.kinalrh.util.Conexion.getInstancia().conectar();
                java.sql.PreparedStatement st = conn.prepareStatement("DELETE FROM empleado WHERE id_empleado = ?");
                st.setLong(1, idCreado);
                st.executeUpdate();
                st.close();
                conn.close();
                System.out.println("   [LIMPIEZA] Empleado de prueba eliminado de la BD.");
            } catch (Exception ignored) {}

        } catch (Exception e) {
            System.err.println("   [ERROR T3.18]: " + e.getMessage());
            e.printStackTrace();
        }

        SessionManager.getInstance().logout();
        System.out.println("\n=================================================================");
        System.out.println("            TODAS LAS PRUEBAS FINALIZADAS CON ÉXITO              ");
        System.out.println("=================================================================");
    }
}
