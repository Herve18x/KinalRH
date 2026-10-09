package org.kinalrh.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.kinalrh.dao.AreaDAO;
import org.kinalrh.model.Area;
import org.kinalrh.util.Conexion;

public class AreaDAOImpl implements AreaDAO {
    @Override
    public List<Area> listarActivas() {
        List<Area> lista = new ArrayList<>();
        String sql = "SELECT id_area, codigo, nombre, descripcion, activo FROM area WHERE activo = TRUE ORDER BY nombre ASC";
        try (Connection conn = Conexion.getInstancia().conectar();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Area a = new Area();
                a.setIdArea(rs.getLong("id_area"));
                a.setCodigo(rs.getString("codigo"));
                a.setNombre(rs.getString("nombre"));
                a.setDescripcion(rs.getString("descripcion"));
                a.setActivo(rs.getBoolean("activo"));
                lista.add(a);
            }
        } catch (SQLException e) {
            System.err.println("Error al listar áreas activas: " + e.getMessage());
        }
        return lista;
    }

    @Override
    public Area buscarPorId(Long id) {
        String sql = "SELECT id_area, codigo, nombre, descripcion, activo FROM area WHERE id_area = ?";
        try (Connection conn = Conexion.getInstancia().conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Area a = new Area();
                    a.setIdArea(rs.getLong("id_area"));
                    a.setCodigo(rs.getString("codigo"));
                    a.setNombre(rs.getString("nombre"));
                    a.setDescripcion(rs.getString("descripcion"));
                    a.setActivo(rs.getBoolean("activo"));
                    return a;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar area por id: " + e.getMessage());
        }
        return null;
    }
}