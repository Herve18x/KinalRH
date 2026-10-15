package org.kinalrh.dao.impl;

import java.sql.*;
import java.util.*;
import javax.sql.DataSource;
import org.kinalrh.dao.ReporteDAO;
import org.kinalrh.model.FilaReporte;
import org.kinalrh.util.Conexion;

public class ReporteDAOImpl implements ReporteDAO {
    private final DataSource datos;
    public ReporteDAOImpl() { this(null); }
    public ReporteDAOImpl(DataSource datos) { this.datos=datos; }
    @Override public List<FilaReporte> listarActivos(Long idArea,Long idPuesto) throws SQLException {
        String sql="SELECT e.id_empleado, COALESCE(e.nombre_completo, CONCAT_WS(' ', e.primer_nombre,e.primer_apellido)) AS nombre, "
                + "ee.nombre AS estado, a.nombre AS area, p.nombre AS puesto "
                + "FROM empleado e JOIN estado_empleado ee ON ee.id_estado_empleado=e.id_estado_empleado "
                + "LEFT JOIN area a ON a.id_area=e.id_area_principal "
                + "LEFT JOIN puesto p ON p.id_puesto=e.id_puesto_actual "
                + "WHERE ee.es_estado_activo=TRUE";
        List<Long> parametros=new ArrayList<>();
        if(idArea!=null) { sql+=" AND e.id_area_principal=?"; parametros.add(idArea); }
        if(idPuesto!=null) { sql+=" AND e.id_puesto_actual=?"; parametros.add(idPuesto); }
        sql+=" ORDER BY e.id_empleado ASC";
        List<FilaReporte> filas=new ArrayList<>();
        try(Connection conn=datos==null?Conexion.getInstancia().conectar():datos.getConnection(); PreparedStatement s=conn.prepareStatement(sql)) {
            for(int i=0;i<parametros.size();i++) s.setLong(i+1,parametros.get(i));
            try(ResultSet r=s.executeQuery()) {
                while(r.next()) filas.add(new FilaReporte(r.getLong("id_empleado"),r.getString("nombre"),r.getString("estado"),r.getString("area"),r.getString("puesto")));
            }
        }
        return List.copyOf(filas);
    }
}
