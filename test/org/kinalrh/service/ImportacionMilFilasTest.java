package org.kinalrh.service;

import java.nio.file.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.kinalrh.PruebaBD;
import static org.kinalrh.PruebaBD.verificar;

/** Carga completa de mil filas y actualización posterior del mismo archivo. */
public class ImportacionMilFilasTest {
    public static void main(String[] args) throws Exception {
        Path archivo=Path.of("mil-filas.xlsx");
        try (PruebaBD bd=new PruebaBD(Path.of(args[0]),false)) {
            ImportacionXlsxServiceTest.preparar(bd);
            try (Workbook libro=new XSSFWorkbook()) {
                Sheet hoja=libro.createSheet("Datos"); Row cabecera=hoja.createRow(0);
                for(int i=0;i<7;i++)cabecera.createCell(i).setCellValue(ValidadorImportacion.ENCABEZADOS.get(i));
                for(int n=1;n<=1000;n++) {
                    Row fila=hoja.createRow(n);
                    String[] valores={String.format("400000000%04d",n),"Nombre","Prueba","ACTIVO","AREA_A","PUESTO_A","2026-10-15"};
                    for(int i=0;i<7;i++)fila.createCell(i).setCellValue(valores[i]);
                }
                try(var out=Files.newOutputStream(archivo)){libro.write(out);}
            }
            ImportacionXlsxService servicio=new ImportacionXlsxService(bd.datos);
            long inicio=System.nanoTime();
            var lote=servicio.previsualizar(archivo);
            verificar(lote.getFilas().size()==1000 && !lote.tieneErrores(),"Mil filas válidas");
            verificar(bd.numero("SELECT COUNT(*) FROM empleado")==1,"Previsualizar sin escritura");
            var alta=servicio.confirmar(lote);
            verificar(alta.nuevos()==1000 && alta.actualizados()==0 && alta.ids().stream().distinct().count()==1000,"Mil altas y IDs distintos");
            var edicion=servicio.confirmar(servicio.previsualizar(archivo));
            verificar(edicion.nuevos()==0 && edicion.actualizados()==1000,"Repetir actualiza las mil filas");
            verificar(bd.numero("SELECT COUNT(*) FROM empleado")==1001,"Sin duplicados");
            verificar(bd.numero("SELECT COUNT(*) FROM empleado_area")==1001,"Áreas sincronizadas");
            System.out.println("ImportacionMilFilasTest: PASS 1000 altas y 1000 actualizaciones; ms="+(System.nanoTime()-inicio)/1_000_000);
        } finally { Files.deleteIfExists(archivo); }
    }
}
