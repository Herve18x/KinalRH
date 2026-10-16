package org.kinalrh.service;

import java.nio.file.*;
import java.util.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.kinalrh.PruebaBD;
import static org.kinalrh.PruebaBD.verificar;

public class ImportacionBordesTest {
    public static void main(String[] args) throws Exception {
        try (PruebaBD bd = new PruebaBD(Path.of(args[0]), false)) {
            ImportacionXlsxServiceTest.preparar(bd);
            ImportacionXlsxService servicio = new ImportacionXlsxService(bd.datos);
            List<String> fallos = new ArrayList<>();
            Path archivo = Path.of("bordes.xlsx");
            try {
                try { servicio.leer(Path.of("C:/")); fallos.add("Raíz aceptada como archivo"); }
                catch (IllegalArgumentException esperado) { }
                catch (RuntimeException ex) { fallos.add("Ruta raíz produce " + ex.getClass().getSimpleName()); }

                for (String caso : List.of("dpi_numerico", "formula_encabezado", "fecha_cero", "duplicados_unicode")) {
                    try (Workbook libro = new XSSFWorkbook()) {
                        Sheet hoja = libro.createSheet("Datos");
                        Row cabecera = hoja.createRow(0);
                        for (int i = 0; i < 7; i++) cabecera.createCell(i).setCellValue(ValidadorImportacion.ENCABEZADOS.get(i));
                        Row fila = hoja.createRow(1);
                        String[] valores = {"FICTICIO-099", "Nombre", "Prueba", "ACTIVO", "", "", ""};
                        for (int i = 0; i < 7; i++) fila.createCell(i).setCellValue(valores[i]);
                        if (caso.equals("dpi_numerico")) fila.getCell(0).setCellValue(3000123451201d);
                        if (caso.equals("formula_encabezado")) {
                            cabecera.getCell(0).setCellFormula("\"dpi\"");
                            CellStyle estilo = libro.createCellStyle(); estilo.setDataFormat(libro.createDataFormat().getFormat("\"dpi\""));
                            cabecera.getCell(0).setCellStyle(estilo);
                        }
                        if (caso.equals("fecha_cero")) fila.getCell(6).setCellValue("0000-01-01");
                        if (caso.equals("duplicados_unicode")) {
                            // MySQL utf8mb4_0900_ai_ci considera ß = ss.
                            fila.getCell(0).setCellValue("FICTICIO-ß");
                            Row otra = hoja.createRow(2);
                            for (int i = 0; i < 7; i++) otra.createCell(i).setCellValue(valores[i]);
                            otra.getCell(0).setCellValue("FICTICIO-ss");
                        }
                        try (var out = Files.newOutputStream(archivo)) { libro.write(out); }
                    }
                    var lote = servicio.previsualizar(archivo);
                    if (!lote.tieneErrores()) fallos.add(caso + " pasa previsualización");
                }
                verificar(bd.numero("SELECT COUNT(*) FROM empleado") == 1, "Los casos inválidos no escriben");
                fallos.forEach(f -> System.out.println("FAIL: " + f));
                verificar(fallos.isEmpty(), "Fallos en límites de importación: " + fallos.size());
                System.out.println("ImportacionBordesTest: rutas, DPI de texto, cabecera, fecha y collation correctos.");
            } finally { Files.deleteIfExists(archivo); }
        }
    }
}
