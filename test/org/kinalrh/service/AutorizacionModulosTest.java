package org.kinalrh.service;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.kinalrh.PruebaBD;
import org.kinalrh.model.Usuario;
import org.kinalrh.util.SessionManager;
import static org.kinalrh.PruebaBD.verificar;

/** Regresión: un parámetro de rol nunca sustituye la sesión y sus permisos SQL. */
public class AutorizacionModulosTest {
    public static void main(String[] args) throws Exception {
        try (PruebaBD bd = new PruebaBD(Path.of(args[0]), true)) {
            ImportacionXlsxServiceTest.preparar(bd);
            UsuarioService usuarios = new UsuarioService();
            CatalogoService catalogos = new CatalogoService();
            LogroService logros = new LogroService();
            AuditoriaService auditoria = new AuditoriaService();
            List<Runnable> operaciones = List.of(
                    () -> usuarios.listarUsuarios("ADMIN"),
                    () -> usuarios.buscarPorId(1, "ADMIN"),
                    () -> usuarios.guardarUsuario(null, "ADMIN"),
                    () -> usuarios.actualizarUsuario(null, "ADMIN"),
                    () -> usuarios.cambiarEstado(99999, false, "ADMIN"),
                    () -> usuarios.activar(99999, "ADMIN"),
                    () -> usuarios.desactivar(99999, "ADMIN"),
                    () -> usuarios.asignarRol(99999, 1, "ADMIN"),
                    () -> usuarios.listarColaboradoresSinRol("ADMIN"),
                    () -> catalogos.listarNivelesAcademicos("ADMIN"),
                    () -> catalogos.listarNivelesAcademicosActivos(),
                    () -> catalogos.guardarNivelAcademico(null, "ADMIN"),
                    () -> catalogos.cambiarEstadoNivelAcademico(99999L, false, "ADMIN"),
                    () -> catalogos.esNivelValidoYActivo(1L),
                    () -> catalogos.listarTiposLogro("ADMIN"),
                    () -> catalogos.listarTiposLogroActivos(),
                    () -> catalogos.guardarTipoLogro(null, "ADMIN"),
                    () -> catalogos.cambiarEstadoTipoLogro(99999L, false, "ADMIN"),
                    () -> logros.listarTiposLogro(),
                    () -> logros.listarTodosLosTiposLogro("ADMIN"),
                    () -> logros.guardarTipoLogro(null, "ADMIN"),
                    () -> logros.cambiarEstadoTipoLogro(99999L, false, "ADMIN"),
                    () -> logros.guardarLogroDeColaborador(1L, null, "ADMIN"),
                    () -> logros.listarLogrosPorColaborador(1, "ADMIN"),
                    () -> logros.listarLogrosPorColaboradorDTO(1, "ADMIN"),
                    () -> logros.listarLogrosConEmpleados("ADMIN"),
                    () -> auditoria.obtenerEventosConUsuario(10),
                    () -> auditoria.listarRegistrosAuditoria(10),
                    () -> auditoria.listarRegistrosAuditoria(null, null, 10));
            List<String> fallos = new ArrayList<>();
            SessionManager.getInstance().logout();
            comprobarDenegaciones("logout", operaciones, fallos);
            Usuario visor = new Usuario();
            visor.setIdUsuario(2L); visor.setRol("visor");
            SessionManager.getInstance().login(visor);
            comprobarDenegaciones("visor con parámetro ADMIN", operaciones, fallos);
            if (new AutorizacionService().tienePermiso("ADMIN", "USUARIO_GESTIONAR")) {
                fallos.add("tienePermiso confía en el rol recibido");
            }
            fallos.forEach(f -> System.out.println("FAIL: " + f));
            verificar(fallos.isEmpty(), "Accesos indebidos: " + fallos.size());

            bd.sql("INSERT INTO usuario_rol SELECT 2,id_rol FROM rol WHERE nombre='visor'");
            bd.sql("INSERT INTO rol_permiso SELECT r.id_rol,p.id_permiso FROM rol r JOIN permiso p ON p.codigo IN ('USUARIO_GESTIONAR','NIVEL_ACADEMICO_GESTIONAR','LOGRO_GESTIONAR','AUDITORIA_VER') WHERE r.nombre='visor'");
            verificar(usuarios.listarUsuarios("SIN_ROL").size() == 2, "Permiso SQL de usuarios");
            verificar(!catalogos.listarNivelesAcademicos("SIN_ROL").isEmpty(), "Permiso SQL de niveles");
            verificar(!catalogos.listarTiposLogro("SIN_ROL").isEmpty(), "Permiso SQL de tipos");
            logros.listarLogrosPorColaborador(1, "SIN_ROL");
            auditoria.listarRegistrosAuditoria(10);
            bd.sql("UPDATE rol SET activo=FALSE WHERE nombre='visor'");
            ImportacionXlsxServiceTest.denegado(() -> usuarios.listarUsuarios("ADMIN"));
            bd.sql("UPDATE rol SET activo=TRUE WHERE nombre='visor'");
            bd.sql("UPDATE usuario SET activo=FALSE WHERE id_usuario=2");
            ImportacionXlsxServiceTest.denegado(() -> usuarios.listarUsuarios("ADMIN"));
            System.out.println("AutorizacionModulosTest: 58 denegaciones, permisos SQL y revocación correctos.");
        }
    }

    private static void comprobarDenegaciones(String contexto, List<Runnable> operaciones, List<String> fallos) {
        for (int i = 0; i < operaciones.size(); i++) {
            try {
                operaciones.get(i).run();
                fallos.add(contexto + " operación " + (i + 1) + " permitida");
            } catch (SecurityException esperado) {
                // Rechazo antes de consultar o validar datos de negocio.
            } catch (RuntimeException ex) {
                fallos.add(contexto + " operación " + (i + 1) + " sin comprobar autorización: " + ex.getClass().getSimpleName());
            }
        }
    }
}
