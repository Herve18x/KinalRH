package org.kinalrh.service;

import java.nio.file.*;
import java.sql.*;
import java.util.UUID;
import org.kinalrh.PruebaBD;
import org.kinalrh.dao.impl.*;
import org.kinalrh.util.PasswordHasher;
import static org.kinalrh.PruebaBD.verificar;
import static org.kinalrh.service.ImportacionXlsxServiceTest.denegado;

public class PruebaIntegradaTest {
    public static void main(String[] args) throws Exception {
        Path raiz=Path.of(args[0]);
        try(PruebaBD bd=new PruebaBD(raiz,true)) {
            ImportacionXlsxServiceTest.preparar(bd);
            String clave=UUID.randomUUID().toString();
            try(Connection c=bd.datos.getConnection();PreparedStatement s=c.prepareStatement("UPDATE usuario SET password_hash=? WHERE id_usuario=2")) {
                s.setString(1,PasswordHasher.hashPassword(clave));s.executeUpdate();
            }
            bd.sql("INSERT INTO usuario_rol SELECT 2,id_rol FROM rol WHERE nombre='encargado'");
            bd.sql("INSERT INTO rol_permiso SELECT r.id_rol,p.id_permiso FROM rol r JOIN permiso p ON p.codigo IN ('EMPLEADO_VER','IMPORTACION_EJECUTAR','REPORTES_XLSX') WHERE r.nombre='encargado'");
            SesionService.getInstance().cerrarSesion();
            var usuario=new AutenticacionService().autenticar("revision_visor",clave);
            SesionService.getInstance().iniciarSesion(usuario);
            verificar("encargado".equalsIgnoreCase(usuario.getRol()),"Rol autenticado desde SQL");
            ImportacionXlsxService importacion=new ImportacionXlsxService(bd.datos);
            var lote=importacion.previsualizar(raiz.resolve("test/fixtures/lote_valido.xlsx"));
            verificar(!lote.tieneErrores(),"Lote válido");
            var resultado=importacion.confirmar(lote);
            verificar(resultado.nuevos()==1 && resultado.actualizados()==1,"Alta y actualización integradas");
            long nuevo=resultado.ids().get(1),actualizado=resultado.ids().get(0);
            EmpleadoService empleados=new EmpleadoService(new EmpleadoDAOImpl(bd.datos));
            var ficha=empleados.buscarPorId(nuevo,usuario.getRol());
            verificar(ficha!=null && ficha.getIdAreaPrincipal()==2 && ficha.getIdPuestoActual()==2,"Ficha nueva y referencias");
            verificar("Actualizada".equals(empleados.buscarPorId(actualizado,usuario.getRol()).getPrimerApellido()),"Ficha actualizada");
            ReporteService reportes=new ReporteService(new ReporteDAOImpl(bd.datos),new ExportacionXlsxService(bd.datos));
            var filas=reportes.consultar(2L,2L);
            verificar(filas.size()==1 && filas.get(0).id()==nuevo,"Empleado importado en reporte filtrado");
            Path salida=Path.of("recorrido_integrado.xlsx");
            verificar(reportes.exportar(salida,2L,2L),"Exportación integrada");
            ReporteServiceTest.comprobar(salida,filas);
            verificar(bd.numero("SELECT COUNT(*) FROM exportacion_reporte WHERE id_usuario=2")==1,"Traza del usuario autenticado");
            SesionService.getInstance().cerrarSesion();
            denegado(()->empleados.buscarPorId(nuevo,usuario.getRol()));
            denegado(()->importacion.previsualizar(raiz.resolve("test/fixtures/lote_valido.xlsx")));
            denegado(()->reportes.exportar(salida,2L,2L));
            Files.delete(salida);
            System.out.println("PruebaIntegradaTest: login, rol, XLSX, IDs, ficha, reporte filtrado, exportación y logout correctos.");
        }
    }
}
