package org.kinalrh.service;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.kinalrh.PruebaBD;
import org.kinalrh.dao.impl.UsuarioDAOImpl;
import org.kinalrh.model.Usuario;
import org.kinalrh.util.SessionManager;
import static org.kinalrh.PruebaBD.verificar;

/** Reproduce las regresiones de compatibilidad y de auditoría con permisos mínimos. */
public class SegundaRegresionesTest {
    public static void main(String[] args) throws Exception {
        List<String> fallos = new ArrayList<>();
        try (PruebaBD bd = new PruebaBD(Path.of(args[0]), true)) {
            ImportacionXlsxServiceTest.preparar(bd);
            UsuarioDAOImpl dao = new UsuarioDAOImpl();
            UsuarioService servicio = new UsuarioService();
            Usuario u = new Usuario();
            u.setUsername("sin_empleado"); u.setNombreCompleto("Cuenta ficticia");
            u.setPasswordHash("EntradaFicticia!123"); u.setRol("visor"); u.setIdEmpleado(0L);
            try {
                servicio.guardarUsuario(u, "ADMIN");
                verificar(dao.buscarPorId(u.getIdUsuario()).orElseThrow().getIdEmpleado() == null, "Alta normaliza cero a NULL");
            } catch (RuntimeException ex) { fallos.add("B alta con idEmpleado=0 rechazada: " + ex.getClass().getSimpleName()); }
            Usuario editar = dao.buscarPorId(2).orElseThrow(); editar.setIdEmpleado(0L);
            try {
                servicio.actualizarUsuario(editar, "ADMIN");
                verificar(dao.buscarPorId(2).orElseThrow().getIdEmpleado() == null, "Edición normaliza cero a NULL");
            } catch (RuntimeException ex) { fallos.add("B edición con idEmpleado=0 rechazada: " + ex.getClass().getSimpleName()); }
            bd.sql("INSERT INTO usuario_rol SELECT 2,id_rol FROM rol WHERE nombre='visor'");
            bd.sql("INSERT INTO rol_permiso SELECT r.id_rol,p.id_permiso FROM rol r JOIN permiso p ON p.codigo='CAMBIO_APROBAR_RH' WHERE r.nombre='visor'");
            SessionManager.getInstance().login(dao.buscarPorId(2).orElseThrow());
            long antes = bd.numero("SELECT COUNT(*) FROM auditoria WHERE accion='ACCESO_DENEGADO'");
            new AutorizacionService().exigirAlgunPermiso("CAMBIO_CREAR", "CAMBIO_APROBAR_RH");
            if (bd.numero("SELECT COUNT(*) FROM auditoria WHERE accion='ACCESO_DENEGADO'") != antes)
                fallos.add("C alternativa autorizada genera ACCESO_DENEGADO");
            long denegados = bd.numero("SELECT COUNT(*) FROM auditoria WHERE accion='ACCESO_DENEGADO'");
            ImportacionXlsxServiceTest.denegado(() -> new AutorizacionService().exigirAlgunPermiso("USUARIO_GESTIONAR", "ROL_GESTIONAR"));
            if (bd.numero("SELECT COUNT(*) FROM auditoria WHERE accion='ACCESO_DENEGADO'") != denegados + 1)
                fallos.add("C rechazo completo debe generar un solo evento");
        }
        fallos.forEach(System.out::println);
        verificar(fallos.isEmpty(), "Regresiones: " + fallos);
        System.out.println("SegundaRegresionesTest: PASS alta/edición cero y auditoría de alternativas.");
    }
}
