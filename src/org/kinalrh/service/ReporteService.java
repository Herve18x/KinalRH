package org.kinalrh.service;

import java.nio.file.Path;
import java.sql.SQLException;
import java.util.List;
import org.kinalrh.dao.ReporteDAO;
import org.kinalrh.dao.impl.ReporteDAOImpl;
import org.kinalrh.model.FilaReporte;

public class ReporteService {
    private final ReporteDAO reportes;
    private final ExportacionXlsxService exportacion;
    private final AutorizacionService autorizacion=new AutorizacionService();
    public ReporteService() { this(new ReporteDAOImpl(),new ExportacionXlsxService()); }
    public ReporteService(ReporteDAO reportes,ExportacionXlsxService exportacion) {
        this.reportes=java.util.Objects.requireNonNull(reportes); this.exportacion=java.util.Objects.requireNonNull(exportacion);
    }
    public List<FilaReporte> consultar(Long idArea,Long idPuesto) {
        autorizacion.exigirPermiso("EMPLEADO_VER");
        validarFiltro(idArea); validarFiltro(idPuesto);
        try{return reportes.listarActivos(idArea,idPuesto);}
        catch(SQLException ex){throw new IllegalStateException("No se pudo consultar el reporte de empleados activos.");}
    }
    public boolean exportar(Path destino,Long idArea,Long idPuesto) {
        autorizacion.exigirPermiso("REPORTES_XLSX");
        if(destino==null) return false;
        return exportacion.exportar(destino,consultar(idArea,idPuesto));
    }
    private void validarFiltro(Long id) { if(id!=null && id<=0) throw new IllegalArgumentException("Seleccione un identificador de filtro válido."); }
}
