package org.kinalrh.service;

import java.nio.file.*;
import org.kinalrh.PruebaBD;
import org.kinalrh.dao.impl.*;
import org.kinalrh.model.*;
import org.kinalrh.util.SessionManager;
import static org.kinalrh.PruebaBD.verificar;
import static org.kinalrh.service.ImportacionXlsxServiceTest.denegado;

public class AutorizacionServiceTest {
    public static void main(String[] args) throws Exception {
        Path raiz=Path.of(args[0]);
        try(PruebaBD bd=new PruebaBD(raiz,true)) {
            ImportacionXlsxServiceTest.preparar(bd);
            bd.sql("INSERT INTO usuario_rol SELECT 2,id_rol FROM rol WHERE nombre='encargado'");
            bd.sql("INSERT INTO rol_permiso SELECT r.id_rol,p.id_permiso FROM rol r JOIN permiso p ON p.codigo='EMPLEADO_VER' WHERE r.nombre='encargado'");
            Usuario usuario=new Usuario();usuario.setIdUsuario(2L);usuario.setRol("encargado");
            SesionService.getInstance().iniciarSesion(usuario);
            verificar(SesionService.getInstance().tienePermiso("EMPLEADO_VER"),"Permiso por código SQL");
            EmpleadoService empleados=new EmpleadoService(new EmpleadoDAOImpl(bd.datos));
            ImportacionXlsxService importacion=new ImportacionXlsxService(bd.datos);
            ReporteService reportes=new ReporteService(new ReporteDAOImpl(bd.datos),new ExportacionXlsxService());
            verificar(empleados.buscarPorId(1,"ADMIN")!=null,"Consulta con permiso de la sesión");
            Empleado edicion=new Empleado();edicion.setIdEmpleado(1L);
            Path salida=Path.of("no_autorizado.xlsx");
            denegado(()->empleados.guardarEmpleado(edicion,"ADMIN"));
            denegado(()->importacion.previsualizar(raiz.resolve("test/fixtures/lote_valido.xlsx")));
            denegado(()->reportes.exportar(salida,null,null));
            bd.sql("INSERT INTO rol_permiso SELECT r.id_rol,p.id_permiso FROM rol r JOIN permiso p ON p.codigo IN ('EMPLEADO_EDITAR','IMPORTACION_EJECUTAR','REPORTES_XLSX') WHERE r.nombre='encargado'");
            var lote=importacion.previsualizar(raiz.resolve("test/fixtures/lote_valido.xlsx"));
            bd.sql("DELETE rp FROM rol_permiso rp JOIN permiso p ON p.id_permiso=rp.id_permiso WHERE p.codigo='IMPORTACION_EJECUTAR'");
            denegado(()->importacion.confirmar(lote));
            verificar(bd.numero("SELECT COUNT(*) FROM empleado")==1,"Revocación antes de confirmar no escribe");
            bd.sql("INSERT INTO rol_permiso SELECT r.id_rol,p.id_permiso FROM rol r JOIN permiso p ON p.codigo='IMPORTACION_EJECUTAR' WHERE r.nombre='encargado'");
            var valido=importacion.previsualizar(raiz.resolve("test/fixtures/lote_valido.xlsx"));
            var resultado=importacion.confirmar(valido);
            verificar(resultado.nuevos()==1 && resultado.actualizados()==1,"Importar con permiso");
            var leido=empleados.buscarPorId(1,"encargado");leido.setPrimerNombre("Editado");empleados.guardarEmpleado(leido,"encargado");
            verificar("Editado".equals(empleados.buscarPorId(1,"encargado").getPrimerNombre()),"Editar con permiso");
            verificar(reportes.exportar(salida,null,null),"Exportar con permiso");Files.delete(salida);
            SesionService.getInstance().cerrarSesion();
            denegado(()->empleados.buscarPorId(1,"ADMIN"));
            denegado(()->empleados.buscar(null,null,null,null,25,0));
            denegado(()->empleados.guardarEmpleado(leido,"ADMIN"));
            denegado(()->importacion.previsualizar(raiz.resolve("test/fixtures/lote_valido.xlsx")));
            denegado(()->importacion.confirmar(valido));
            denegado(()->reportes.exportar(salida,null,null));
            verificar(!Files.exists(salida),"Logout no produce archivo");
            verificar(bd.numero("SELECT COUNT(*) FROM auditoria WHERE accion='ACCESO_DENEGADO' AND campo='permiso'")>=3,"Permiso denegado trazado");
            System.out.println("AutorizacionServiceTest: EMPLEADO_VER, EMPLEADO_EDITAR, IMPORTACION_EJECUTAR, REPORTES_XLSX y logout correctos.");
        }
    }
}
