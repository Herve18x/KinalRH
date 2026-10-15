package org.kinalrh.dao;

import java.sql.Connection;
import java.sql.SQLException;
import org.kinalrh.model.ExportacionReporte;

public interface ExportacionReporteDAO {
    long insertar(Connection conn, ExportacionReporte exportacion) throws SQLException;
}
