package org.kinalrh.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class Conexion {

    private static Conexion instancia;
    private String url;
    private String user;
    private String password;

    private Conexion() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            cargarConfiguracion();
        } catch (ClassNotFoundException e) {
            System.err.println("Error al cargar el Driver de MySQL: " + e.getMessage());
        }
    }

    private void cargarConfiguracion() {
        Properties prop = new Properties();
        File configFile = new File("config.properties");

        try {
            if (configFile.exists()) {
                try (InputStream input = new FileInputStream(configFile)) {
                    prop.load(input);
                }
            } else {
                // Intentar cargar como recurso del proyecto si no existe la raíz
                try (InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties")) {
                    if (input != null) prop.load(input);
                }
            }
        } catch (IOException e) {
            System.err.println("Advertencia: No se pudo cargar config.properties, usando valores por defecto.");
        }

        String host = prop.getProperty("db.host", "localhost");
        String port = prop.getProperty("db.port", "3306");
        String dbName = prop.getProperty("db.name", "IN4CM");
        this.user = prop.getProperty("db.user", "root");
        this.password = prop.getProperty("db.password", "");

        this.url = "jdbc:mysql://" + host + ":" + port + "/" + dbName
                + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    }

    public static synchronized Conexion getInstancia() {
        if (instancia == null) {
            instancia = new Conexion();
        }
        return instancia;
    }

    public Connection conectar() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    public boolean probarConexion() {
        try (Connection conexion = conectar()) {
            return conexion != null && !conexion.isClosed();
        } catch (SQLException e) {
            System.err.println("Error de conexión a MySQL: " + e.getMessage());
            return false;
        }
    }
}