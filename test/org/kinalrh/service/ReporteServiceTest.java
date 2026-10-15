package org.kinalrh.service;

import java.nio.file.*;
import java.util.*;
import org.apache.poi.ss.usermodel.*;
import org.kinalrh.PruebaBD;
import org.kinalrh.dao.impl.*;
import org.kinalrh.model.*;
import org.kinalrh.util.SessionManager;
import static org.kinalrh.PruebaBD.verificar;
import static org.kinalrh.service.ImportacionXlsxServiceTest.denegado;

public class ReporteServiceTest {
    public static void main(String[] args) throws Exception {
        try(PruebaBD bd=new PruebaBD(Path.of(args[0]),true)) {
            ImportacionXlsxServiceTest.preparar(bd);
            EmpleadoDAOImpl empleados=new EmpleadoDAOImpl(bd.datos);
            long segundo=empleados.insertar(empleado("FICTICIO-020",2L,2L,1L));
            long retirado=empleados.insertar(empleado("FICTICIO-030",1L,1L,bd.numero("SELECT id_estado_empleado FROM estado_empleado WHERE codigo='RETIRADO'")));
            ReporteService servicio=new ReporteService(new ReporteDAOImpl(bd.datos),new ExportacionXlsxService());
            var general=servicio.consultar(null,null);
            verificar(general.size()==2,"Dos activos");
            verificar(general.stream().noneMatch(f->f.id()==retirado),"Retirado excluido");
            verificar(general.size()==bd.numero("SELECT COUNT(*) FROM empleado e JOIN estado_empleado ee ON ee.id_estado_empleado=e.id_estado_empleado WHERE ee.es_estado_activo=TRUE"),"Conteo control");
            verificar(servicio.consultar(2L,null).size()==1 && servicio.consultar(null,2L).size()==1,"Filtros aislados");
            verificar(servicio.consultar(2L,1L).isEmpty(),"Filtros combinados");
            var filtro=servicio.consultar(2L,2L);
            verificar(filtro.size()==1 && filtro.get(0).id()==segundo,"Área principal y puesto actual");
            Path salida=Path.of("reporte_prueba.xlsx");
            verificar(!servicio.exportar(null,null,null),"Cancelación sin éxito");
            verificar(servicio.exportar(salida,2L,2L),"Exportación autorizada");
            comprobar(salida,filtro);
            servicio.exportar(salida,null,null);
            comprobar(salida,general);
            Files.delete(salida);
            Path directorio=Path.of("destino_directorio.xlsx"); Files.createDirectories(directorio);
            try{servicio.exportar(directorio,null,null);throw new AssertionError("Escritura fallida indicada como éxito");}catch(IllegalStateException esperado) { }
            Files.delete(directorio);
            Usuario visor=new Usuario(); visor.setIdUsuario(2L); visor.setRol("visor"); SessionManager.getInstance().login(visor);
            denegado(()->servicio.consultar(null,null));
            denegado(()->servicio.exportar(salida,null,null));
            denegado(()->new ExportacionXlsxService().exportar(salida,general));
            verificar(!Files.exists(salida),"Denegación no produce archivo");
            SessionManager.getInstance().logout();
            denegado(()->servicio.consultar(null,null));
            denegado(()->servicio.exportar(salida,null,null));
            System.out.println("ReporteServiceTest: activos, filtros, orden, XLSX, columnas, errores y autorización correctos.");
        }
    }
    public static void comprobar(Path archivo,List<FilaReporte> esperado) throws Exception {
        try(var in=Files.newInputStream(archivo); Workbook libro=WorkbookFactory.create(in)) {
            Sheet hoja=libro.getSheetAt(0);
            verificar(hoja.getRow(0).getLastCellNum()==5,"Solo cinco columnas permitidas");
            for(int i=0;i<5;i++) verificar(ExportacionXlsxService.ENCABEZADOS.get(i).equals(hoja.getRow(0).getCell(i).getStringCellValue()),"Encabezado exacto");
            verificar(hoja.getLastRowNum()==esperado.size(),"Conteo XLSX");
            for(int n=0;n<esperado.size();n++) verificar(Long.toString(esperado.get(n).id()).equals(hoja.getRow(n+1).getCell(0).getStringCellValue()),"IDs y orden iguales al reporte");
        }
    }
    private static Empleado empleado(String dpi,Long area,Long puesto,Long estado) {
        Empleado e=new Empleado(); e.setDpi(dpi); e.setPrimerNombre("Nombre"); e.setPrimerApellido("Prueba");
        e.setIdAreaPrincipal(area); e.setIdPuestoActual(puesto); e.setIdEstadoEmpleado(estado); return e;
    }
}
