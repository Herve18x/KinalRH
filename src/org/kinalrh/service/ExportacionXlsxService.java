package org.kinalrh.service;

import java.io.IOException;
import java.nio.file.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import javax.sql.DataSource;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.kinalrh.model.FilaReporte;
import org.kinalrh.model.ExportacionReporte;
import org.kinalrh.model.Usuario;
import org.kinalrh.dao.impl.ExportacionReporteDAOImpl;
import org.kinalrh.util.Conexion;
import org.kinalrh.util.SessionManager;

public class ExportacionXlsxService {
    private final DataSource datos;
    public ExportacionXlsxService() { this(null); }
    public ExportacionXlsxService(DataSource datos) { this.datos=datos; }
    public static final List<String> ENCABEZADOS=List.of("ID","nombre","estado","área","puesto");
    public boolean exportar(Path destino,List<FilaReporte> filas) {
        Usuario usuario=new AutorizacionService().exigirPermiso("REPORTES_XLSX");
        String sesion=SessionManager.getInstance().getSessionUUID();
        if(destino==null) return false;
        Path temporal=null;
        Path respaldo=null;
        boolean conservarRespaldo=false;
        try {
            Path absoluto=destino.toAbsolutePath();
            if(!absoluto.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".xlsx"))
                throw new IllegalArgumentException("El destino debe tener extensión .xlsx.");
            if(Files.exists(absoluto) && !Files.isRegularFile(absoluto)) throw new IOException("Destino inválido");
            ExportacionReporte exportacion=new ExportacionReporte(null,UUID.randomUUID().toString(),usuario.getIdUsuario(),
                    "EMPLEADOS_ACTIVOS",LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.MICROS));
            temporal=Files.createTempFile(absoluto.getParent(),"reporte-",".tmp");
            try(Workbook libro=new XSSFWorkbook()) {
                Sheet hoja=libro.createSheet("Empleados");
                Row cabecera=hoja.createRow(0);
                for(int i=0;i<ENCABEZADOS.size();i++) cabecera.createCell(i).setCellValue(ENCABEZADOS.get(i));
                int n=1;
                for(FilaReporte f:filas) {
                    Row fila=hoja.createRow(n++);
                    fila.createCell(0).setCellValue(Long.toString(f.id()));
                    fila.createCell(1).setCellValue(f.nombre());
                    fila.createCell(2).setCellValue(f.estado());
                    fila.createCell(3).setCellValue(f.area()==null?"":f.area());
                    fila.createCell(4).setCellValue(f.puesto()==null?"":f.puesto());
                }
                hoja.createFreezePane(0,1);
                hoja.setAutoFilter(new org.apache.poi.ss.util.CellRangeAddress(0,Math.max(0,n-1),0,4));
                for(int i=0;i<5;i++) hoja.setColumnWidth(i,(i==1?40:24)*256);
                Sheet metadatos=libro.createSheet("Metadatos");
                String[][] valores={{"folio",exportacion.folio()},{"id_usuario",Long.toString(exportacion.idUsuario())},
                        {"tipo_reporte",exportacion.tipoReporte()},{"fecha_hora",exportacion.fechaHora().toString()}};
                for(int i=0;i<valores.length;i++) {
                    Row fila=metadatos.createRow(i);fila.createCell(0).setCellValue(valores[i][0]);fila.createCell(1).setCellValue(valores[i][1]);
                }
                metadatos.setColumnWidth(0,24*256);metadatos.setColumnWidth(1,42*256);
                try(var out=Files.newOutputStream(temporal)) { libro.write(out); }
            }
            try(var in=Files.newInputStream(temporal); Workbook comprobado=WorkbookFactory.create(in)) {
                if(comprobado.getSheetAt(0).getLastRowNum()!=filas.size()) throw new IOException("Reporte incompleto");
            }
            if(Files.exists(absoluto)) {
                respaldo=Files.createTempFile(absoluto.getParent(),"respaldo-reporte-",".tmp");
                Files.copy(absoluto,respaldo,StandardCopyOption.REPLACE_EXISTING);
            }
            try(Connection conn=datos==null?Conexion.getInstancia().conectar():datos.getConnection()) {
                boolean original=conn.getAutoCommit();
                boolean publicado=false;
                conn.setAutoCommit(false);
                try {
                    Usuario vigente=new AutorizacionService().exigirPermiso(conn,"REPORTES_XLSX");
                    if(!Objects.equals(sesion,SessionManager.getInstance().getSessionUUID()) || vigente.getIdUsuario()!=exportacion.idUsuario())
                        throw new SecurityException("La sesión cambió. Genere el reporte nuevamente.");
                    long id=new ExportacionReporteDAOImpl().insertar(conn,exportacion);
                    ExportacionReporte guardada=new ExportacionReporte(id,exportacion.folio(),exportacion.idUsuario(),exportacion.tipoReporte(),exportacion.fechaHora());
                    new AuditoriaService().registrarExportacion(conn,guardada);
                    mover(temporal,absoluto);
                    publicado=true;
                    conn.commit();
                } catch(SQLException | IOException | RuntimeException ex) {
                    try{conn.rollback();}catch(SQLException rollback){ex.addSuppressed(rollback);}
                    if(publicado) {
                        try {
                            if(respaldo!=null) mover(respaldo,absoluto);
                            else Files.deleteIfExists(absoluto);
                        } catch(IOException restauracion) {
                            conservarRespaldo=true;
                            throw new IllegalStateException("La exportación falló y no se pudo restaurar el destino. Conserve el respaldo temporal de la carpeta.");
                        }
                    }
                    throw ex;
                } finally { conn.setAutoCommit(original); }
            }
            return true;
        } catch(SecurityException ex) { throw ex; }
        catch(IOException | SQLException ex) { throw new IllegalStateException("No se pudo guardar y registrar el XLSX. Revise la carpeta, la conexión y las migraciones."); }
        finally {
            if(temporal!=null) try{Files.deleteIfExists(temporal);}catch(IOException ex){System.err.println("No se pudo retirar un archivo temporal de exportación.");}
            if(respaldo!=null && !conservarRespaldo) try{Files.deleteIfExists(respaldo);}catch(IOException ex){System.err.println("No se pudo retirar un respaldo temporal de exportación.");}
        }
    }
    private void mover(Path origen,Path destino) throws IOException {
        try{Files.move(origen,destino,StandardCopyOption.ATOMIC_MOVE,StandardCopyOption.REPLACE_EXISTING);}
        catch(AtomicMoveNotSupportedException ex){Files.move(origen,destino,StandardCopyOption.REPLACE_EXISTING);}
    }
}
