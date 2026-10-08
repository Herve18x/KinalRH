package org.kinalrh.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Script de migración en Java para la tarea T1.21 / HU US-34.
 * 
 * Propósito:
 * Rellenar cada cuenta ficticia existente en la tabla 'usuario' con un
 * UUID aleatorio y único generado con UUID.randomUUID(), permitiendo cerrar
 * la migración aplicando la restricción NOT NULL y UNIQUE en la BD.
 */
public class UuidUsuarioMigracion {

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("Iniciando Migración de UUIDs (Tarea T1.21)");
        System.out.println("=================================================");

        boolean exito = ejecutarMigracion();
        if (exito) {
            System.out.println("\nVerificando consistencia de datos...");
            verificarMigracion();
        }
    }

    /**
     * Ejecuta el proceso de migración: recorre los usuarios sin UUID y les
     * asigna un identificador único generado con UUID.randomUUID().
     */
    public static boolean ejecutarMigracion() {
        String selectSql = "SELECT id_usuario, nombre_usuario FROM usuario WHERE uuid_usuario IS NULL OR uuid_usuario = ''";
        String updateSql = "UPDATE usuario SET uuid_usuario = ? WHERE id_usuario = ?";

        try (Connection conn = Conexion.getInstancia().conectar()) {
            if (conn == null) {
                System.err.println("Error: No se pudo establecer conexión con la base de datos.");
                return false;
            }

            conn.setAutoCommit(false); // Transacción para garantizar atomicidad

            int totalActualizados = 0;

            try (PreparedStatement selectStmt = conn.prepareStatement(selectSql);
                 PreparedStatement updateStmt = conn.prepareStatement(updateSql);
                 ResultSet rs = selectStmt.executeQuery()) {

                while (rs.next()) {
                    long idUsuario = rs.getLong("id_usuario");
                    String nombreUsuario = rs.getString("nombre_usuario");
                    String nuevoUuid = UUID.randomUUID().toString();

                    updateStmt.setString(1, nuevoUuid);
                    updateStmt.setLong(2, idUsuario);
                    updateStmt.executeUpdate();

                    System.out.println("-> Asignado UUID a usuario [" + idUsuario + "] (" + nombreUsuario + "): " + nuevoUuid);
                    totalActualizados++;
                }

                conn.commit();
                System.out.println("\n✓ Migración completada exitosamente.");
                System.out.println("  Total de cuentas actualizadas: " + totalActualizados);
                return true;

            } catch (SQLException e) {
                conn.rollback();
                System.err.println("Error al actualizar usuarios, realizando rollback: " + e.getMessage());
                return false;
            }

        } catch (SQLException e) {
            System.err.println("Error en la conexión a la base de datos: " + e.getMessage());
            return false;
        }
    }

    /**
     * Valida los criterios de aceptación:
     * 1. Ningún usuario tiene UUID nulo.
     * 2. Todos los usuarios tienen UUIDs distintos entre sí (unicidad).
     */
    public static void verificarMigracion() {
        String sql = "SELECT id_usuario, nombre_usuario, uuid_usuario FROM usuario";

        try (Connection conn = Conexion.getInstancia().conectar();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            Set<String> uuidsVistos = new HashSet<>();
            int total = 0;
            boolean hayDuplicados = false;
            boolean hayNulos = false;

            System.out.println("\n--- Estado actual de cuentas en la BD ---");
            while (rs.next()) {
                total++;
                long id = rs.getLong("id_usuario");
                String nombre = rs.getString("nombre_usuario");
                String uuid = rs.getString("uuid_usuario");

                System.out.println("ID: " + id + " | Usuario: " + nombre + " | UUID: " + uuid);

                if (uuid == null || uuid.trim().isEmpty()) {
                    hayNulos = true;
                } else if (!uuidsVistos.add(uuid)) {
                    hayDuplicados = true;
                }
            }

            System.out.println("-----------------------------------------");
            if (total == 0) {
                System.out.println("No hay usuarios registrados.");
            } else if (!hayNulos && !hayDuplicados) {
                System.out.println("✓ CRITERIO CUMPLIDO: Todos los " + total + " usuarios tienen UUIDs válidos y distintos.");
            } else {
                if (hayNulos) System.err.println("✗ Fallo: Existen usuarios con UUID nulo.");
                if (hayDuplicados) System.err.println("✗ Fallo: Existen UUIDs duplicados.");
            }

        } catch (SQLException e) {
            System.err.println("Error durante la verificación: " + e.getMessage());
        }
    }
}
