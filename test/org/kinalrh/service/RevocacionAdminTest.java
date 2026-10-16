package org.kinalrh.service;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.kinalrh.PruebaBD;
import org.kinalrh.model.Usuario;
import org.kinalrh.util.SessionManager;
import static org.kinalrh.PruebaBD.verificar;

public class RevocacionAdminTest {
    public static void main(String[] args) throws Exception {
        try (PruebaBD bd = new PruebaBD(Path.of(args[0]), false)) {
            ImportacionXlsxServiceTest.preparar(bd);
            bd.sql("INSERT IGNORE INTO usuario_rol SELECT 1,id_rol FROM rol WHERE nombre='admin'");
            AutorizacionService servicio = new AutorizacionService();
            List<String> fallos = new ArrayList<>();
            servicio.exigirPermiso("USUARIO_GESTIONAR");
            bd.sql("UPDATE usuario SET activo=FALSE WHERE id_usuario=1");
            rechazo(fallos,"Cuenta admin desactivada", () -> servicio.exigirPermiso("USUARIO_GESTIONAR"));
            bd.sql("UPDATE usuario SET activo=TRUE WHERE id_usuario=1");
            bd.sql("UPDATE rol SET activo=FALSE WHERE nombre='admin'");
            rechazo(fallos,"Rol admin desactivado", () -> servicio.exigirPermiso("USUARIO_GESTIONAR"));
            bd.sql("UPDATE rol SET activo=TRUE WHERE nombre='admin'");
            bd.sql("DELETE FROM usuario_rol WHERE id_usuario=1");
            rechazo(fallos,"Rol admin revocado", () -> servicio.exigirPermiso("USUARIO_GESTIONAR"));
            Usuario impostor = new Usuario(); impostor.setIdUsuario(2L); impostor.setRol("ADMIN");
            SessionManager.getInstance().login(impostor);
            rechazo(fallos,"Rol admin solo en memoria", () -> servicio.exigirPermiso("USUARIO_GESTIONAR"));
            bd.sql("INSERT INTO usuario_rol SELECT 2,id_rol FROM rol WHERE nombre='visor'");
            bd.sql("INSERT INTO rol_permiso SELECT r.id_rol,p.id_permiso FROM rol r JOIN permiso p ON p.codigo='EMPLEADO_VER' WHERE r.nombre='visor'");
            impostor.setRol("visor"); SesionService.getInstance().iniciarSesion(impostor);
            verificar(SesionService.getInstance().tienePermiso("EMPLEADO_VER"), "Permiso inicial de sesión");
            bd.sql("DELETE FROM rol_permiso");
            if (SesionService.getInstance().tienePermiso("EMPLEADO_VER")) fallos.add("Permiso revocado permanece en caché");
            fallos.forEach(f -> System.out.println("FAIL: " + f));
            verificar(fallos.isEmpty(), "Fallos de revocación: " + fallos.size());
            System.out.println("RevocacionAdminTest: cuenta, rol, asociación y permisos revocados se comprueban en SQL.");
        }
    }
    private static void rechazo(List<String> fallos,String caso,Runnable accion) {
        try { accion.run(); fallos.add(caso); } catch (SecurityException esperado) { }
    }
}
