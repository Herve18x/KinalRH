package org.kinalrh.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.kinalrh.util.Conexion;

public class UsuarioDAOImpl {

    /**
     * Valida credenciales utilizando los nombres exactos de columnas de la BD (dbkinalrh_dev.sql).
     */
    public boolean autenticar(String nombreUsuario, String passwordHash) {
        String sql = "SELECT id_usuario, nombre_usuario, password_hash, activo "
                   + "FROM usuario WHERE nombre_usuario = ? AND password_hash = ? AND activo = TRUE";

        try (Connection conn = Conexion.getInstancia().conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nombreUsuario);
            stmt.setString(2, passwordHash);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("Error al autenticar usuario: " + e.getMessage());
            return false;
        }
    }
}
