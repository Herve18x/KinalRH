package org.kinalrh.service;

import java.io.InputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.util.*;
import javax.sql.DataSource;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.kinalrh.dao.impl.EmpleadoDAOImpl;
import org.kinalrh.model.FilaImportacion;
import org.kinalrh.util.Conexion;
import org.kinalrh.util.SessionManager;

public class ImportacionXlsxService {
    public record Lectura(List<String> encabezados, List<FilaImportacion> filas) {
        public Lectura { encabezados=List.copyOf(encabezados); filas=List.copyOf(filas); }
    }
    public static final class Lote {
        private final Lectura lectura;
        private final List<FilaImportacion> filas;
        private final String sesion;
        private boolean cerrado;
        private Lote(Lectura lectura,List<FilaImportacion> filas,String sesion) {
            this.lectura=lectura; this.filas=List.copyOf(filas); this.sesion=sesion;
        }
        public List<FilaImportacion> getFilas() { return filas; }
        public boolean tieneErrores() { return filas.stream().anyMatch(f->f.getAccion()==FilaImportacion.Accion.ERROR); }
        public synchronized void cancelar() { cerrado=true; }
    }
    public record Resultado(int nuevos,int actualizados,List<Long> ids) {
        public Resultado { ids=List.copyOf(ids); }
    }
    private final DataSource datos;
    private final EmpleadoDAOImpl empleados;
    private final AutorizacionService autorizacion = new AutorizacionService();
    private final ValidadorImportacion validador = new ValidadorImportacion();
    public ImportacionXlsxService() { this(null); }
    public ImportacionXlsxService(DataSource datos) { this.datos=datos; empleados=new EmpleadoDAOImpl(datos); }
    private Connection conectar() throws SQLException { return datos==null?Conexion.getInstancia().conectar():datos.getConnection(); }

    public Lectura leer(Path archivo) {
        autorizacion.exigirPermiso("IMPORTACION_EJECUTAR");
        if (archivo==null || !archivo.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".xlsx") || !Files.isRegularFile(archivo))
            throw new IllegalArgumentException("Seleccione un archivo .xlsx existente.");
        try (InputStream in=Files.newInputStream(archivo); Workbook libro=WorkbookFactory.create(in)) {
            if (!(libro instanceof XSSFWorkbook) || libro.getNumberOfSheets()==0) throw new IOException("Formato");
            Sheet hoja=libro.getSheetAt(0);
            DataFormatter formato=new DataFormatter(Locale.ROOT);
            Row cabecera=hoja.getRow(0);
            List<String> encabezados=new ArrayList<>();
            if (cabecera!=null) for(int i=0;i<cabecera.getLastCellNum();i++) encabezados.add(texto(cabecera.getCell(i),formato));
            List<FilaImportacion> filas=new ArrayList<>();
            for (int n=1;n<=hoja.getLastRowNum();n++) {
                Row fila=hoja.getRow(n);
                if(fila==null) continue;
                List<String> valores=new ArrayList<>();
                List<FilaImportacion.Error> errores=new ArrayList<>();
                int columnas=Math.max(7,fila.getLastCellNum());
                for(int i=0;i<columnas;i++) {
                    Cell celda=fila.getCell(i);
                    valores.add(texto(celda,formato));
                    if(celda!=null && (celda.getCellType()==CellType.FORMULA || celda.getCellType()==CellType.ERROR))
                        errores.add(new FilaImportacion.Error(n+1,i<7?ValidadorImportacion.ENCABEZADOS.get(i):"columna "+(i+1),"Use un valor literal sin fórmulas ni errores."));
                    if(i>=7 && !valores.get(i).isEmpty()) errores.add(new FilaImportacion.Error(n+1,"columna "+(i+1),"Columna fuera de la plantilla."));
                }
                if(valores.stream().allMatch(String::isEmpty) && errores.isEmpty()) continue;
                filas.add(new FilaImportacion(n+1,valores,errores,FilaImportacion.Accion.ERROR,null,null,null,null));
            }
            return new Lectura(encabezados,filas);
        } catch (IOException | RuntimeException ex) {
            throw new IllegalArgumentException("No se pudo leer el XLSX. Compruebe que sea válido, legible y no esté protegido.");
        }
    }
    private String texto(Cell celda,DataFormatter formato) {
        if(celda==null) return "";
        if(celda.getCellType()==CellType.FORMULA) return "";
        if(celda.getCellType()==CellType.NUMERIC && DateUtil.isCellDateFormatted(celda))
            return celda.getLocalDateTimeCellValue().toLocalDate().toString();
        return formato.formatCellValue(celda).trim();
    }
    public Lote previsualizar(Path archivo) {
        autorizacion.exigirPermiso("IMPORTACION_EJECUTAR");
        Lectura lectura=leer(archivo);
        try(Connection conn=conectar()) {
            return new Lote(lectura,clasificar(lectura,conn),SessionManager.getInstance().getSessionUUID());
        } catch(SQLException ex) { throw new IllegalStateException("No se pudo validar el lote. No se guardaron empleados."); }
    }
    private List<FilaImportacion> clasificar(Lectura lectura,Connection conn) throws SQLException {
        List<FilaImportacion> resultado=new ArrayList<>();
        for(FilaImportacion f:validador.validar(lectura.encabezados(),lectura.filas(),conn)) {
            if(!f.getErrores().isEmpty()) { resultado.add(f); continue; }
            Long id=empleados.buscarIdPorDpi(conn,f.valor(0)).orElse(null);
            resultado.add(new FilaImportacion(f.getNumeroFila(),f.getValores(),f.getErrores(),
                    id==null?FilaImportacion.Accion.NUEVO:FilaImportacion.Accion.ACTUALIZAR,
                    id,f.getIdEstado(),f.getIdArea(),f.getIdPuesto()));
        }
        return List.copyOf(resultado);
    }
    public Resultado confirmar(Lote lote) {
        autorizacion.exigirPermiso("IMPORTACION_EJECUTAR");
        if(lote==null) throw new IllegalArgumentException("Primero valide un archivo.");
        synchronized(lote) {
            if(lote.cerrado || !Objects.equals(lote.sesion,SessionManager.getInstance().getSessionUUID()))
                throw new SecurityException("El lote fue cancelado, ejecutado o pertenece a otra sesión.");
            if(lote.tieneErrores()) throw new IllegalArgumentException("Corrija todos los errores antes de importar.");
            try(Connection conn=conectar()) {
                boolean original=conn.getAutoCommit();
                conn.setAutoCommit(false);
                try {
                    List<FilaImportacion> vigentes=clasificar(lote.lectura,conn);
                    if(vigentes.stream().anyMatch(f->!f.getErrores().isEmpty())) throw new IllegalArgumentException("Los catálogos cambiaron. Valide nuevamente el archivo.");
                    if(!vigentes.stream().map(f->f.getIdExistente()).toList().equals(lote.filas.stream().map(f->f.getIdExistente()).toList()))
                        throw new IllegalArgumentException("Los empleados cambiaron. Valide nuevamente el archivo.");
                    int nuevos=0,actualizados=0;
                    List<Long> ids=new ArrayList<>();
                    for(FilaImportacion fila:vigentes) {
                        if(!Objects.equals(lote.sesion,SessionManager.getInstance().getSessionUUID())) throw new SecurityException("La sesión cambió.");
                        long id;
                        if(fila.getAccion()==FilaImportacion.Accion.NUEVO) { id=empleados.insertar(conn,fila.empleadoPropuesto()); nuevos++; }
                        else {
                            id=fila.getIdExistente();
                            if(!empleados.actualizar(conn,fila.empleadoPropuesto())) throw new SQLException("Empleado no disponible");
                            actualizados++;
                        }
                        ids.add(id);
                    }
                    autorizacion.exigirPermiso(conn,"IMPORTACION_EJECUTAR");
                    conn.commit();
                    lote.cerrado=true;
                    return new Resultado(nuevos,actualizados,ids);
                } catch(SQLException | RuntimeException ex) {
                    try { conn.rollback(); } catch(SQLException rollback) { ex.addSuppressed(rollback); }
                    throw ex;
                } finally { conn.setAutoCommit(original); }
            } catch(SecurityException ex) { throw ex; }
            catch(SQLException | RuntimeException ex) { throw new IllegalStateException("No se importó el lote. Se revirtieron todos los cambios; vuelva a validar los datos."); }
        }
    }
}
