package org.kinalrh.service;

import java.lang.reflect.*;
import java.nio.file.*;
import java.sql.*;
import java.util.*;
import javax.sql.DataSource;
import org.apache.poi.ss.usermodel.*;
import org.kinalrh.PruebaBD;
import org.kinalrh.dao.impl.ReporteDAOImpl;
import org.kinalrh.util.SessionManager;
import static org.kinalrh.PruebaBD.verificar;
import static org.kinalrh.service.ImportacionXlsxServiceTest.denegado;

public class ExportacionTrazabilidadTest {
    public static void main(String[] args) throws Exception {
        try(PruebaBD bd=new PruebaBD(Path.of(args[0]),true)) {
            ImportacionXlsxServiceTest.preparar(bd);
            var filas=new ReporteDAOImpl(bd.datos).listarActivos(null,null);
            ExportacionXlsxService servicio=new ExportacionXlsxService(bd.datos);
            Path uno=Path.of("folio_uno.xlsx"),dos=Path.of("folio_dos.xlsx");
            verificar(servicio.exportar(uno,filas) && servicio.exportar(dos,filas),"Dos exportaciones");
            String primero=comprobar(bd,uno),segundo=comprobar(bd,dos);
            verificar(!primero.equals(segundo),"Folios únicos");
            verificar(bd.numero("SELECT COUNT(*) FROM exportacion_reporte")==2,"Dos trazas");
            long eventos=bd.numero("SELECT COUNT(*) FROM auditoria WHERE entidad='EXPORTACION_REPORTE'");
            verificar(!servicio.exportar(null,filas),"Cancelar no indica éxito");
            verificar(bd.numero("SELECT COUNT(*) FROM exportacion_reporte")==2,"Cancelar sin traza");
            byte[] original=Files.readAllBytes(uno);
            bd.sql("CREATE TRIGGER fallo_auditoria BEFORE INSERT ON auditoria FOR EACH ROW SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Fallo controlado'");
            fallar(()->servicio.exportar(uno,filas));
            verificar(Arrays.equals(original,Files.readAllBytes(uno)),"Fallo de auditoría conserva archivo anterior");
            verificar(bd.numero("SELECT COUNT(*) FROM exportacion_reporte")==2,"Rollback de traza si falla auditoría");
            bd.sql("DROP TRIGGER fallo_auditoria");
            DataSource falloCommit=(DataSource)Proxy.newProxyInstance(DataSource.class.getClassLoader(),new Class[]{DataSource.class},(p,m,a)-> {
                if(!m.getName().equals("getConnection")) return invocar(m,bd.datos,a);
                Connection c=bd.datos.getConnection();
                return Proxy.newProxyInstance(Connection.class.getClassLoader(),new Class[]{Connection.class},(pc,mc,ac)-> {
                    if(mc.getName().equals("commit")) throw new SQLException("Fallo controlado antes de commit");
                    return invocar(mc,c,ac);
                });
            });
            fallar(()->new ExportacionXlsxService(falloCommit).exportar(uno,filas));
            verificar(Arrays.equals(original,Files.readAllBytes(uno)),"Fallo de commit restaura archivo anterior");
            Path nuevo=Path.of("fallo_commit.xlsx");
            fallar(()->new ExportacionXlsxService(falloCommit).exportar(nuevo,filas));
            verificar(!Files.exists(nuevo),"Fallo de commit retira archivo nuevo");
            fallar(()->servicio.exportar(Path.of("carpeta_inexistente/salida.xlsx"),filas));
            verificar(bd.numero("SELECT COUNT(*) FROM exportacion_reporte")==2 && bd.numero("SELECT COUNT(*) FROM auditoria WHERE entidad='EXPORTACION_REPORTE'")==eventos,"Fallos sin éxito registrado");
            SessionManager.getInstance().logout();
            denegado(()->servicio.exportar(nuevo,filas));
            Files.delete(uno);Files.delete(dos);
            System.out.println("ExportacionTrazabilidadTest: folio, tabla, usuario, fecha, auditoría, cancelación y compensación correctos.");
        }
    }
    private static String comprobar(PruebaBD bd,Path archivo) throws Exception {
        try(var in=Files.newInputStream(archivo);Workbook libro=WorkbookFactory.create(in)) {
            Sheet metadatos=libro.getSheet("Metadatos");
            String folio=metadatos.getRow(0).getCell(1).getStringCellValue();UUID.fromString(folio);
            try(Connection c=bd.datos.getConnection();PreparedStatement s=c.prepareStatement(
                    "SELECT er.*,a.id_auditoria FROM exportacion_reporte er JOIN auditoria a ON a.entidad='EXPORTACION_REPORTE' AND a.registro_id=er.id_exportacion AND a.id_usuario=er.id_usuario WHERE er.folio=?")) {
                s.setString(1,folio);
                try(ResultSet r=s.executeQuery()) {
                    verificar(r.next(),"Folio vinculado a auditoría");
                    verificar(r.getLong("id_usuario")==1,"Usuario autenticado");
                    verificar(r.getString("tipo_reporte").equals(metadatos.getRow(2).getCell(1).getStringCellValue()),"Tipo de reporte");
                    verificar(r.getObject("fecha_hora",java.time.LocalDateTime.class).equals(java.time.LocalDateTime.parse(metadatos.getRow(3).getCell(1).getStringCellValue())),"Fecha coincide");
                    verificar(!r.next(),"Una traza por exportación");
                }
            }
            verificar(libro.getSheetAt(0).getRow(0).getLastCellNum()==5,"Folio separado de empleados");
            return folio;
        }
    }
    private static Object invocar(Method m,Object obj,Object[] args) throws Throwable {
        try{return m.invoke(obj,args);}catch(InvocationTargetException ex){throw ex.getCause();}
    }
    private static void fallar(Runnable accion) {try{accion.run();throw new AssertionError("Fallo anunciado como éxito");}catch(IllegalStateException esperado) { }}
}
