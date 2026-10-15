package org.kinalrh.dao;

import java.sql.SQLException;
import java.util.List;
import org.kinalrh.model.FilaReporte;

public interface ReporteDAO {
    List<FilaReporte> listarActivos(Long idArea, Long idPuesto) throws SQLException;
}
