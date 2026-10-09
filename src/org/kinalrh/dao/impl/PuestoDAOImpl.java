package org.kinalrh.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.kinalrh.dao.PuestoDAO;
import org.kinalrh.model.Puesto;
import org.kinalrh.util.Conexion;

public class PuestoDAOImpl implements PuestoDAO {
    @Override
    public List<Puesto> listarActivos() {
        List<Puesto> lista = new ArrayList<>();
        String sql = "SELECT id_puesto, codigo, nombre, descripcion, activo FROM puesto WHERE activo = TRUE ORDER BY nombre ASC";
        try (Connection conn = Conexion.getInstancia().conectar();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Puesto p = new Puesto();
                p.setIdPuesto(rs.getLong("id_puesto"));
                p.setCodigo(rs.getString("codigo"));
                p.setNombre(rs.getString("nombre"));
                p.setDescripcion(rs.getString("descripcion"));
                p.setActivo(rs.getBoolean("activo"));
                lista.add(p);
            }
        } catch (SQLException e) {
            System.err.println("Error al listar puestos activos: " + e.getMessage());
        }
        return lista;
    }

    @Override
    public Puesto buscarPorId(Long id) {
        String sql = "SELECT id_puesto, codigo, nombre, descripcion, activo FROM puesto WHERE id_puesto = ?";
        try (Connection conn = Conexion.getInstancia().conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Puesto p = new Puesto();
                    p.setIdPuesto(rs.getLong("id_puesto"));
                    p.setCodigo(rs.getString("codigo"));
                    p.setNombre(rs.getString("nombre"));
                    p.setDescripcion(rs.getString("descripcion"));
                    p.setActivo(rs.getBoolean("activo"));
                    return p;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar puesto por id: " + e.getMessage());
        }
        return null;
    }
}