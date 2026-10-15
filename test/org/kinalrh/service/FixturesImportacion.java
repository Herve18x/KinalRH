package org.kinalrh.service;

import java.nio.file.*;
import java.util.List;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public final class FixturesImportacion {
    public static void main(String[] args) throws Exception {
        Path raiz=Path.of(args[0]);
        escribir(raiz.resolve("templates/empleados_plantilla.xlsx"),ValidadorImportacion.ENCABEZADOS,List.of());
        escribir(raiz.resolve("test/fixtures/lote_valido.xlsx"),ValidadorImportacion.ENCABEZADOS,List.of(
                List.of("FICTICIO-001","Elena","Actualizada","ACTIVO","","",""),
                List.of("FICTICIO-002","Mario","Nuevo","ACTIVO","AREA_B","PUESTO_B","2026-10-01")));
        escribir(raiz.resolve("test/fixtures/lote_invalido.xlsx"),List.of("documento","primer_nombre","primer_apellido","estado_codigo","area_codigo","puesto_codigo","fecha_ingreso_kinal"),List.of(
                List.of("FICTICIO-010","","","NO_EXISTE","NO_EXISTE","NO_EXISTE","2026-02-30"),
                List.of("FICTICIO-010","Nombre","Prueba","ACTIVO","","",""),
                List.of("FICTICIO-12345678901234567890","Nombre","Prueba","ACTIVO","","","")));
        System.out.println("Plantilla y fixtures generados y reabiertos con POI.");
    }
    public static void escribir(Path archivo,List<String> cabecera,List<List<String>> datos) throws Exception {
        Files.createDirectories(archivo.toAbsolutePath().getParent());
        try(Workbook libro=new XSSFWorkbook()) {
            Sheet hoja=libro.createSheet("Empleados");
            CellStyle texto=libro.createCellStyle(); texto.setDataFormat(libro.createDataFormat().getFormat("@"));
            hoja.setDefaultColumnStyle(0,texto);
            Row encabezado=hoja.createRow(0);
            for(int i=0;i<cabecera.size();i++) encabezado.createCell(i).setCellValue(cabecera.get(i));
            for(int n=0;n<datos.size();n++) {
                Row fila=hoja.createRow(n+1);
                for(int i=0;i<datos.get(n).size();i++) fila.createCell(i).setCellValue(datos.get(n).get(i));
            }
            hoja.createFreezePane(0,1);
            for(int i=0;i<cabecera.size();i++) hoja.setColumnWidth(i,24*256);
            try(var out=Files.newOutputStream(archivo)) { libro.write(out); }
        }
        try(var in=Files.newInputStream(archivo); Workbook libro=WorkbookFactory.create(in)) {
            if(libro.getSheetAt(0).getLastRowNum()!=datos.size()) throw new AssertionError("Fixture ilegible");
        }
    }
}
