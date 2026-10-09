package org.kinalrh.dao.impl;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.kinalrh.dao.NivelAcademicoDAO;
import org.kinalrh.model.NivelAcademico;
import org.kinalrh.util.Conexion;

public class NivelAcademicoDAOImpl implements NivelAcademicoDAO {
    @Override
    public List<NivelAcademico> listarTodos() throws SQLException {
        List<NivelAcademico> lista = new ArrayList<>();
        String sql = "SELECT id_nivel_academico, nombre FROM nivel_academico WHERE activo = 1 ORDER BY orden_nivel";
        try (Connection conn = Conexion.getInstancia().conectar();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                lista.add(new NivelAcademico(rs.getLong("id_nivel_academico"), rs.getString("nombre")));
            }
        }
        return lista;
    }
}