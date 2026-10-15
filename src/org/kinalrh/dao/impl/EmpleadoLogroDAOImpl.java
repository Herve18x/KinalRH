package org.kinalrh.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class EmpleadoLogroDAOImpl {
    public void insertar(Connection conn, long idEmpleado, long idLogro,
            boolean destacado, String observaciones) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO empleado_logro (id_empleado, id_logro, destacado, observaciones) VALUES (?, ?, ?, ?)")) {
            stmt.setLong(1, idEmpleado);
            stmt.setLong(2, idLogro);
            stmt.setBoolean(3, destacado);
            stmt.setString(4, observaciones);
            stmt.executeUpdate();
        }
    }
}
