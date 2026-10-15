package org.kinalrh;

import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.sql.*;
import java.util.Properties;
import java.util.logging.Logger;
import javax.sql.DataSource;

/** Cada proceso usa un esquema propio; nunca reutiliza la configuración de la aplicación. */
public final class PruebaBD implements AutoCloseable {
    private final String nombre = "kinalrh_revision_" + Long.toUnsignedString(System.nanoTime());
    private final Connection servidor;
    private final String usuario = System.getenv().getOrDefault("KINALRH_TEST_USER", "root");
    private final String clave = System.getenv().getOrDefault("KINALRH_TEST_PASSWORD", "");
    private final String host = System.getenv().getOrDefault("KINALRH_TEST_HOST", "127.0.0.1");
    private final String puerto = System.getenv().getOrDefault("KINALRH_TEST_PORT", "3317");
    private final Path config = Path.of("config.properties");
    public final DataSource datos;

    public PruebaBD(Path raiz, boolean migraciones) throws Exception {
        Path directorioActual=Path.of("").toRealPath();
        if (!directorioActual.startsWith(raiz.toRealPath().resolve("build")) || Files.exists(config)) {
            throw new IllegalStateException("Ejecutar desde un directorio build sin configuración previa.");
        }
        servidor = DriverManager.getConnection(url(""), usuario, clave);
        try {
            ejecutarScript(Files.readString(raiz.resolve("database/dbkinalrh_dev.sql")).replace("IN4CM", nombre));
            if (migraciones) {
                ejecutarScript(Files.readString(raiz.resolve("database/migrations/002_exportacion_reporte.sql")));
            }
            Properties p = new Properties();
            p.setProperty("db.host", host); p.setProperty("db.port", puerto);
            p.setProperty("db.name", nombre); p.setProperty("db.user", usuario); p.setProperty("db.password", clave);
            try (var out = Files.newOutputStream(config, StandardOpenOption.CREATE_NEW)) { p.store(out, "Pruebas descartables"); }
        } catch (Exception ex) {
            try (Statement s = servidor.createStatement()) { s.executeUpdate("DROP DATABASE IF EXISTS " + nombre); }
            servidor.close();
            throw ex;
        }
        datos = new DataSource() {
            public Connection getConnection() throws SQLException { return DriverManager.getConnection(url(nombre), usuario, clave); }
            public Connection getConnection(String u, String p) throws SQLException { return DriverManager.getConnection(url(nombre), u, p); }
            public PrintWriter getLogWriter() { return null; }
            public void setLogWriter(PrintWriter w) { }
            public void setLoginTimeout(int t) { }
            public int getLoginTimeout() { return 0; }
            public Logger getParentLogger() { return Logger.getGlobal(); }
            public <T> T unwrap(Class<T> tipo) throws SQLException { throw new SQLException("No disponible"); }
            public boolean isWrapperFor(Class<?> tipo) { return false; }
        };
    }
    private String url(String bd) { return "jdbc:mysql://" + host + ":" + puerto + "/" + bd + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"; }
    private void ejecutarScript(String texto) throws SQLException {
        try (Statement s = servidor.createStatement()) {
            for (String sql : texto.replaceAll("(?m)--[^\\r\\n]*", "").split(";")) if (!sql.isBlank()) s.execute(sql);
        }
    }
    public long numero(String sql) throws SQLException {
        try (Connection c = datos.getConnection(); Statement s = c.createStatement(); ResultSet r = s.executeQuery(sql)) { r.next(); return r.getLong(1); }
    }
    public void semilla(Path raiz) throws Exception {
        ejecutarScript(Files.readString(raiz.resolve("database/seed_dev.sql")).replace("IN4CM",nombre));
    }
    public void sql(String sql) throws SQLException {
        try (Connection c = datos.getConnection(); Statement s = c.createStatement()) { s.executeUpdate(sql); }
    }
    public static void verificar(boolean condicion, String caso) { if (!condicion) throw new AssertionError(caso); }
    public void close() throws Exception {
        org.kinalrh.util.SessionManager.getInstance().logout();
        try { Files.deleteIfExists(config); }
        finally {
            try (servidor; Statement s = servidor.createStatement()) { s.executeUpdate("DROP DATABASE " + nombre); }
        }
    }
}
