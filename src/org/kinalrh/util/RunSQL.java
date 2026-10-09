package org.kinalrh.util;
import java.sql.Connection;
import java.sql.Statement;
public class RunSQL {
    public static void main(String[] args) {
        try (Connection conn = Conexion.getInstancia().conectar();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("ALTER TABLE usuario ADD COLUMN uuid_usuario VARCHAR(36) UNIQUE");
            System.out.println("Columna uuid_usuario agregada exitosamente.");
        } catch (Exception e) {
            System.out.println("Error o la columna ya existe: " + e.getMessage());
        }
    }
}