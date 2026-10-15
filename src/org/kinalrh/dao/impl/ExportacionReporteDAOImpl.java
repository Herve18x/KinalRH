package org.kinalrh.dao.impl;

import java.sql.*;
import org.kinalrh.dao.ExportacionReporteDAO;
import org.kinalrh.model.ExportacionReporte;

public class ExportacionReporteDAOImpl implements ExportacionReporteDAO {
    @Override public long insertar(Connection conn,ExportacionReporte exportacion) throws SQLException {
        try(PreparedStatement s=conn.prepareStatement(
                "INSERT INTO exportacion_reporte (folio,id_usuario,tipo_reporte,fecha_hora) VALUES (?,?,?,?)",Statement.RETURN_GENERATED_KEYS)) {
            s.setString(1,exportacion.folio());s.setLong(2,exportacion.idUsuario());
            s.setString(3,exportacion.tipoReporte());s.setObject(4,exportacion.fechaHora());
            if(s.executeUpdate()!=1) throw new SQLException("No se pudo registrar la exportación.");
            try(ResultSet r=s.getGeneratedKeys()) {
                if(r.next() && r.getLong(1)>0) return r.getLong(1);
                throw new SQLException("No se obtuvo el identificador de exportación.");
            }
        }
    }
}
