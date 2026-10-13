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
        String sql = "SELECT id_nivel_academico, codigo, nombre, orden_nivel, activo FROM nivel_academico WHERE activo = TRUE ORDER BY orden_nivel ASC";
        try (Connection conn = Conexion.getInstancia().conectar();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    @Override
    public List<NivelAcademico> listarActivos() {
        try {
            return listarTodos();
        } catch (SQLException e) {
            System.err.println("Error al listar niveles académicos activos: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public NivelAcademico buscarPorId(Long id) {
        if (id == null) return null;
        String sql = "SELECT id_nivel_academico, codigo, nombre, orden_nivel, activo FROM nivel_academico WHERE id_nivel_academico = ?";
        try (Connection conn = Conexion.getInstancia().conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar nivel académico por ID: " + e.getMessage());
        }
        return null;
    }

    private NivelAcademico mapear(ResultSet rs) throws SQLException {
        return new NivelAcademico(
                rs.getLong("id_nivel_academico"),
                rs.getString("codigo"),
                rs.getString("nombre"),
                rs.getInt("orden_nivel"),
                rs.getBoolean("activo")
        );
    }
}