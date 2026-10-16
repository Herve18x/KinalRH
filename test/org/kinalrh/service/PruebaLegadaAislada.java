package org.kinalrh.service;

import java.nio.file.Path;
import org.kinalrh.PruebaBD;

/** Ejecuta pruebas históricas que usan Conexion exclusivamente sobre un esquema propio. */
public class PruebaLegadaAislada {
    public static void main(String[] args) throws Exception {
        Path raiz=Path.of(args[0]);
        if(!args[1].matches("org\\.kinalrh\\.service\\.[A-Za-z0-9]+Test")) throw new IllegalArgumentException("Clase de prueba inválida.");
        try(PruebaBD bd=new PruebaBD(raiz,true)) {
            bd.semilla(raiz);
            // Cada ejecutable conserva el perfil encargado y recibe solo lo que ejercita.
            String permisos = switch (args[1].substring(args[1].lastIndexOf('.')+1)) {
                case "UsuarioServiceTest" -> "'USUARIO_GESTIONAR'";
                case "EmpleadoGestionTest", "AuditoriaFlujoPermisosTest" -> "'EMPLEADO_CREAR','EMPLEADO_EDITAR'";
                case "EmpleadoLogroMultipleTest" -> "'EMPLEADO_CREAR','LOGRO_GESTIONAR'";
                case "CargaEmpleadosSimuladosTest" -> "'EMPLEADO_CREAR'";
                case "AuditoriaServiceTest" -> "'AUDITORIA_VER'";
                case "AlertaColaboradorSinRolTest" -> "'USUARIO_GESTIONAR','AUDITORIA_VER'";
                case "Sprint3BatchTest" -> "'NIVEL_ACADEMICO_GESTIONAR','LOGRO_GESTIONAR','EMPLEADO_CREAR','EMPLEADO_EDITAR','AUDITORIA_VER'";
                case "SprintHerberthBatchTest" -> "'USUARIO_GESTIONAR','EMPLEADO_CREAR','EMPLEADO_EDITAR','LOGRO_GESTIONAR'";
                case "EmpleadoListaAutorizacionTest" -> "'EMPLEADO_VER'";
                default -> throw new IllegalArgumentException("Prueba sin perfil mínimo definido.");
            };
            bd.sql("INSERT IGNORE INTO rol_permiso SELECT r.id_rol,p.id_permiso FROM rol r JOIN permiso p ON p.codigo IN ("+permisos+") WHERE r.nombre='encargado'");
            PruebaBD.verificar(bd.numero("SELECT COUNT(*) FROM usuario_rol ur JOIN rol r ON r.id_rol=ur.id_rol WHERE r.nombre='admin'")==0,
                    "Las pruebas legadas no deben tener administrador");
            System.out.println("Fixture encargado sin admin; permisos adicionales: "+permisos);
            Class.forName(args[1]).getMethod("main",String[].class).invoke(null,(Object)new String[0]);
        }
    }
}
