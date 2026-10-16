package org.kinalrh.dao.impl;

import java.io.PrintWriter;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;
import javax.sql.DataSource;
import org.kinalrh.model.Empleado;
import org.kinalrh.model.Usuario;
import org.kinalrh.service.EmpleadoService;
import org.kinalrh.util.SessionManager;

/** Pruebas de integración de T2.11, T2.15 y T2.17 en un esquema desechable. */
public class EmpleadoDAOImplTest {
    private static int comprobaciones;

    public static void main(String[] args) throws Exception {
        if (args.length != 2 || !Path.of("").toAbsolutePath().toString().contains("build")) {
            throw new IllegalArgumentException("Ejecutar desde build con la ruta del SQL y la URL JDBC del servidor de pruebas.");
        }
        String usuario = System.getenv().getOrDefault("KINALRH_TEST_USER", "root");
        String clave = System.getenv().getOrDefault("KINALRH_TEST_PASSWORD", "");
        String esquema = "kinalrh_prueba_sprint2_" + Long.toUnsignedString(System.nanoTime());
        Path configuracion = Path.of("config.properties");
        if (Files.exists(configuracion)) {
            throw new IllegalStateException("La prueba requiere un directorio sin config.properties previo.");
        }
        boolean esquemaCreado = false;
        boolean configuracionCreada = false;
        try (Connection servidor = DriverManager.getConnection(args[1], usuario, clave);
             Statement stmt = servidor.createStatement()) {
            try {
                stmt.executeUpdate("CREATE DATABASE " + esquema);
                esquemaCreado = true;
                String script = Files.readString(Path.of(args[0])).replaceAll("(?m)--[^\r\n]*", "")
                        .replace("IN4CM", esquema);
                for (String sentencia : script.split(";")) {
                    if (!sentencia.isBlank()) stmt.execute(sentencia);
                }
                String url = args[1].replace("/?", "/" + esquema + "?");
                if (url.equals(args[1])) throw new IllegalArgumentException("La URL debe terminar con /? y sus opciones JDBC.");
                java.net.URI direccion = java.net.URI.create(args[1].substring(5));
                Properties propiedades = new Properties();
                propiedades.setProperty("db.host", direccion.getHost());
                propiedades.setProperty("db.port", String.valueOf(direccion.getPort()));
                propiedades.setProperty("db.name", esquema);
                propiedades.setProperty("db.user", usuario);
                propiedades.setProperty("db.password", clave);
                try (var salida = Files.newOutputStream(configuracion, StandardOpenOption.CREATE_NEW)) {
                    configuracionCreada = true;
                    propiedades.store(salida, "Conexión temporal de pruebas");
                }
                DataSource datos = new DatosPrueba(url, usuario, clave);
                ejecutar(datos);
                System.out.println("RESULTADO: " + comprobaciones + " comprobaciones correctas en MySQL real.");
            } finally {
                SessionManager.getInstance().logout();
                if (configuracionCreada) Files.delete(configuracion);
                if (esquemaCreado) stmt.executeUpdate("DROP DATABASE " + esquema);
            }
        }
    }

    private static void ejecutar(DataSource datos) throws Exception {
        EmpleadoDAOImpl dao = new EmpleadoDAOImpl(datos);
        long activo = numero(datos, "SELECT id_estado_empleado FROM estado_empleado WHERE codigo = 'ACTIVO'");
        long retirado = numero(datos, "SELECT id_estado_empleado FROM estado_empleado WHERE codigo = 'RETIRADO'");
        sql(datos, "INSERT INTO area (codigo, nombre) VALUES ('PRUEBA', 'Área de prueba')");
        sql(datos, "INSERT INTO puesto (codigo, nombre) VALUES ('PRUEBA', 'Puesto de prueba')");
        sql(datos, "INSERT INTO usuario (nombre_usuario, password_hash, nombre_completo) VALUES ('prueba', 'sin acceso', 'Cuenta de prueba')");
        long area = numero(datos, "SELECT id_area FROM area WHERE codigo = 'PRUEBA'");
        long puesto = numero(datos, "SELECT id_puesto FROM puesto WHERE codigo = 'PRUEBA'");

        Empleado completo = empleado(1, activo);
        completo.setNit("123-4");
        completo.setCodigoEmpleado("PRUEBA-1");
        completo.setSegundoNombre("María");
        completo.setTercerNombre("José");
        completo.setSegundoApellido("López");
        completo.setApellidoCasada("de Pérez");
        completo.setFechaNacimiento(LocalDate.of(1990, 2, 3));
        completo.setEstadoCivil("Casado(a)");
        completo.setReligion("Dato de prueba");
        completo.setDireccion("Dirección de prueba");
        completo.setZona("7");
        completo.setMunicipio("Guatemala");
        completo.setDepartamento("Guatemala");
        completo.setTelefonoMovil("55551111");
        completo.setTelefonoFijo("22223333");
        completo.setCorreoPersonal("prueba@example.test");
        completo.setFechaIngresoKinal(LocalDate.of(2020, 1, 2));
        completo.setIdAreaPrincipal(area);
        completo.setIdPuestoActual(puesto);
        completo.setIdNivelAcademico(1L);
        long id = dao.insertar(completo);
        completo.setIdEmpleado(id);
        comprobar(id > 0, "Alta devuelve clave generada positiva");
        comprobarFicha(completo, dao.buscarPorId(id).orElseThrow());
        comprobar("Ana María José García López de Pérez".equals(dao.buscarPorId(id).orElseThrow().getNombreCompleto()), "Nombre completo con todos los nombres y apellidos");
        sql(datos, "UPDATE empleado SET fotografia_path = 'foto.png', dpi_documento_path = 'dpi.pdf' WHERE id_empleado = " + id);
        List<Long> ids = new ArrayList<>();
        ids.add(id);
        for (int i = 2; i <= 30; i++) {
            Empleado otro = empleado(i, activo);
            otro.setNit("");
            otro.setCodigoEmpleado("");
            ids.add(dao.insertar(otro));
        }
        List<Empleado> primera = dao.listarPaginado(25, 0);
        List<Empleado> segunda = dao.listarPaginado(25, 25);
        comprobar(primera.size() == 25 && segunda.size() == 5, "Páginas de 25 y 5 con 30 empleados");
        List<Long> paginados = new ArrayList<>();
        primera.forEach(e -> paginados.add(e.getIdEmpleado()));
        segunda.forEach(e -> paginados.add(e.getIdEmpleado()));
        comprobar(paginados.equals(ids) && new HashSet<>(paginados).size() == 30, "Orden ascendente sin duplicados ni omisiones");
        comprobar(dao.listarPaginado(25, 30).isEmpty(), "Página después del último registro vacía");
        comprobar(dao.listarPaginado(1, 0).size() == 1, "Tamaño mínimo permitido");
        comprobar("Área de prueba".equals(primera.get(0).getArea()) && "Puesto de prueba".equals(primera.get(0).getPuesto()), "Resumen incluye área y puesto");
        comprobar(primera.get(1).getArea() == null && primera.get(1).getPuesto() == null, "Resumen admite catálogos opcionales ausentes");
        for (int[] pagina : new int[][]{{0, 0}, {-1, 0}, {26, 0}, {25, -1}}) {
            esperar(IllegalArgumentException.class, () -> dao.listarPaginado(pagina[0], pagina[1]));
            esperar(IllegalArgumentException.class, () -> dao.listarActivosPaginado(pagina[0], pagina[1]));
        }
        esperar(IllegalArgumentException.class, () -> dao.buscarPorId(0));
        esperar(IllegalArgumentException.class, () -> dao.retirar(-1));
        esperar(IllegalArgumentException.class, () -> dao.insertar(null));
        Empleado estadoInvalido = empleado(31, Long.MAX_VALUE);
        esperar(IllegalArgumentException.class, () -> dao.insertar(estadoInvalido));
        Empleado sinEstado = empleado(31, activo);
        sinEstado.setIdEstadoEmpleado(null);
        esperar(IllegalArgumentException.class, () -> dao.insertar(sinEstado));
        esperar(IllegalArgumentException.class, () -> dao.insertar(empleado(1, activo)));

        Empleado editar = dao.buscarPorId(id).orElseThrow();
        editar.setPrimerNombre("Elena");
        editar.setTercerNombre("Lucía");
        editar.setIdJefeInmediato(ids.get(1));
        comprobar(dao.actualizar(editar), "Edición afecta el empleado indicado");
        comprobarFicha(editar, dao.buscarPorId(id).orElseThrow());
        comprobar(dao.buscarPorId(id).orElseThrow().getNombreCompleto().contains("Lucía"), "Edición sincroniza nombre completo");
        comprobar(dao.actualizar(dao.buscarPorId(id).orElseThrow()), "Actualizar sin cambios devuelve true con useAffectedRows");
        Empleado parcial = empleado(1, activo);
        parcial.setIdEmpleado(id);
        parcial.setPrimerNombre("Julia");
        comprobar(dao.actualizar(parcial), "Edición con opcionales omitidos");
        Empleado conservado = dao.buscarPorId(id).orElseThrow();
        comprobar("Dato de prueba".equals(conservado.getReligion()) && conservado.getFechaNacimiento().equals(completo.getFechaNacimiento())
                && conservado.getIdJefeInmediato().equals(ids.get(1)) && conservado.getIdAreaPrincipal().equals(area), "Opcionales omitidos conservan textos, fechas y relaciones");
        comprobar(conservado.getNombreCompleto().equals("Julia María Lucía García López de Pérez"), "Nombre completo conserva nombres opcionales omitidos");
        Map<String, String> fila = fila(datos, id);
        comprobar("foto.png".equals(fila.get("fotografia_path")) && "dpi.pdf".equals(fila.get("dpi_documento_path")), "Edición conserva columnas sin representación en la ficha");
        conservado.setFechaNacimiento(null);
        conservado.setIdJefeInmediato(null);
        conservado.setIdAreaPrincipal(null);
        conservado.setReligion(null);
        comprobar(dao.actualizar(conservado), "Borrado explícito de opcionales");
        Empleado borrado = dao.buscarPorId(id).orElseThrow();
        comprobar(borrado.getFechaNacimiento() == null && borrado.getIdJefeInmediato() == null
                && borrado.getIdAreaPrincipal() == null && borrado.getReligion() == null, "Null explícito elimina solo los opcionales informados");
        comprobar(numero(datos, "SELECT COUNT(*) FROM empleado_area WHERE es_principal = TRUE AND id_empleado = " + id) == 0, "Quitar área limpia la asociación principal");
        Empleado inexistente = empleado(99, activo);
        inexistente.setIdEmpleado(Long.MAX_VALUE);
        comprobar(!dao.actualizar(inexistente) && dao.buscarPorId(Long.MAX_VALUE).isEmpty(), "Edición y búsqueda de ID inexistente");
        Map<String, String> anteriorDuplicado = fila(datos, ids.get(1));
        Empleado duplicado = dao.buscarPorId(ids.get(1)).orElseThrow();
        duplicado.setDpi(completo.getDpi());
        esperar(IllegalArgumentException.class, () -> dao.actualizar(duplicado));
        comprobar(anteriorDuplicado.equals(fila(datos, ids.get(1))), "DPI duplicado al editar no cambia datos");

        sql(datos, "CREATE TRIGGER rechazar_area BEFORE INSERT ON empleado_area FOR EACH ROW SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Fallo de prueba'");
        Empleado fallido = empleado(40, activo);
        fallido.setIdAreaPrincipal(area);
        esperar(SQLException.class, () -> dao.insertar(fallido));
        comprobar(numero(datos, "SELECT COUNT(*) FROM empleado WHERE dpi = '" + fallido.getDpi() + "'") == 0, "Alta se revierte si falla la asociación");
        Map<String, String> antesFallo = fila(datos, id);
        Empleado edicionFallida = dao.buscarPorId(id).orElseThrow();
        edicionFallida.setPrimerNombre("No guardar");
        edicionFallida.setIdAreaPrincipal(area);
        esperar(SQLException.class, () -> dao.actualizar(edicionFallida));
        comprobar(antesFallo.equals(fila(datos, id)), "Edición se revierte si falla la asociación");
        sql(datos, "DROP TRIGGER rechazar_area");

        EmpleadoDAOImpl sinClaves = new EmpleadoDAOImpl(sinClavesGeneradas(datos));
        esperar(SQLException.class, () -> sinClaves.insertar(empleado(41, activo)));
        comprobar(numero(datos, "SELECT COUNT(*) FROM empleado") == 30, "Falta de clave generada revierte el alta");
        EmpleadoService servicio = new EmpleadoService(dao);
        Map<String, String> antesRetiro = fila(datos, id);
        SessionManager.getInstance().logout();
        esperar(SecurityException.class, () -> servicio.retirarEmpleado(id));
        Usuario cuenta = new Usuario();
        cuenta.setIdUsuario(1L);
        cuenta.setRol("VISOR");
        SessionManager.getInstance().login(cuenta);
        esperar(SecurityException.class, () -> servicio.retirarEmpleado(id));
        cuenta.setRol("ADMIN");
        cuenta.setActivo(false);
        esperar(SecurityException.class, () -> servicio.retirarEmpleado(id));
        comprobar(antesRetiro.equals(fila(datos, id)), "Intentos sin sesión o permiso no cambian al empleado");
        cuenta.setActivo(true);
        sql(datos, "INSERT INTO usuario_rol SELECT 1,id_rol FROM rol WHERE nombre='visor'");
        sql(datos, "INSERT INTO rol_permiso SELECT r.id_rol,p.id_permiso FROM rol r JOIN permiso p ON p.codigo='EMPLEADO_CAMBIAR_ESTADO' WHERE r.nombre='visor'");
        servicio.retirarEmpleado(id);
        Empleado retiradoLeido = dao.buscarPorId(id).orElseThrow();
        comprobar(retiradoLeido.getIdEstadoEmpleado() == retirado, "Retiro localiza el estado por código");
        Map<String, String> despuesRetiro = fila(datos, id);
        antesRetiro.remove("id_estado_empleado");
        despuesRetiro.remove("id_estado_empleado");
        comprobar(antesRetiro.equals(despuesRetiro), "Retiro conserva todas las otras columnas de negocio");
        comprobar(numero(datos, "SELECT COUNT(*) FROM empleado") == 30, "Retiro conserva el registro");
        comprobar(numero(datos, "SELECT COUNT(*) FROM vw_empleados_activos WHERE id_empleado = " + id) == 0, "Retirado fuera de la vista SQL de activos");
        Set<Long> activos = new HashSet<>();
        dao.listarActivosPaginado(25, 0).forEach(e -> activos.add(e.getIdEmpleado()));
        dao.listarActivosPaginado(25, 25).forEach(e -> activos.add(e.getIdEmpleado()));
        comprobar(activos.size() == 29 && !activos.contains(id), "Retirado fuera del listado paginado de activos");
        comprobar(dao.listarPaginado(25, 0).stream().anyMatch(e -> e.getIdEmpleado() == id), "Retirado sigue en consultas generales");
        servicio.retirarEmpleado(id);
        comprobar(!dao.retirar(Long.MAX_VALUE), "Retiro inexistente devuelve false");
        esperar(IllegalArgumentException.class, () -> servicio.retirarEmpleado(Long.MAX_VALUE));
        sql(datos, "UPDATE estado_empleado SET codigo = 'RETIRADO_NO_DISPONIBLE' WHERE codigo = 'RETIRADO'");
        esperar(IllegalStateException.class, () -> servicio.retirarEmpleado(ids.get(1)));
        comprobar(dao.buscarPorId(ids.get(1)).orElseThrow().getIdEstadoEmpleado() == activo, "Estado RETIRADO ausente no modifica datos");
        System.out.println("T2.11, T2.15 y T2.17: pruebas de integración correctas.");
    }

    private static Empleado empleado(int numero, long estado) {
        Empleado e = new Empleado();
        e.setDpi(String.format("%013d", numero));
        e.setPrimerNombre("Ana");
        e.setPrimerApellido("García");
        e.setIdEstadoEmpleado(estado);
        return e;
    }

    private static void comprobarFicha(Empleado esperado, Empleado obtenido) throws Exception {
        for (var metodo : Empleado.class.getMethods()) {
            if (metodo.getName().startsWith("get") && metodo.getParameterCount() == 0
                    && !Set.of("getClass", "getNombre", "getNombreCompleto", "getEstado", "getArea", "getPuesto", "getNivelAcademico", "getJefeInmediato").contains(metodo.getName())) {
                comprobar(java.util.Objects.equals(metodo.invoke(esperado), metodo.invoke(obtenido)), "Relectura: " + metodo.getName());
            }
        }
    }

    private static Map<String, String> fila(DataSource datos, long id) throws SQLException {
        Map<String, String> valores = new LinkedHashMap<>();
        try (Connection conn = datos.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM empleado WHERE id_empleado = ?")) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) throw new AssertionError("No existe el empleado de prueba");
                for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                    String columna = rs.getMetaData().getColumnName(i);
                    if (!columna.equals("actualizado_en")) valores.put(columna, rs.getString(i));
                }
            }
        }
        return valores;
    }

    private static long numero(DataSource datos, String sql) throws SQLException {
        try (Connection conn = datos.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            rs.next();
            return rs.getLong(1);
        }
    }

    private static void sql(DataSource datos, String sql) throws SQLException {
        try (Connection conn = datos.getConnection(); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        }
    }

    private static void comprobar(boolean condicion, String caso) {
        if (!condicion) throw new AssertionError(caso);
        comprobaciones++;
    }

    private static void esperar(Class<? extends Exception> tipo, Accion accion) throws Exception {
        try {
            accion.ejecutar();
        } catch (Exception e) {
            comprobar(tipo.isInstance(e), "Se esperaba " + tipo.getSimpleName() + ", se obtuvo " + e);
            return;
        }
        throw new AssertionError("No se produjo " + tipo.getSimpleName());
    }

    private static DataSource sinClavesGeneradas(DataSource datos) {
        return (DataSource) Proxy.newProxyInstance(DataSource.class.getClassLoader(), new Class<?>[]{DataSource.class}, (proxy, metodo, args) -> {
            if (!metodo.getName().equals("getConnection")) return metodo.invoke(datos, args);
            Connection conn = datos.getConnection();
            return Proxy.newProxyInstance(Connection.class.getClassLoader(), new Class<?>[]{Connection.class}, (p, m, a) -> {
                Object resultado;
                try { resultado = m.invoke(conn, a); }
                catch (java.lang.reflect.InvocationTargetException e) { throw e.getCause(); }
                if (m.getName().equals("prepareStatement") && a.length == 2 && a[1].equals(Statement.RETURN_GENERATED_KEYS)) {
                    PreparedStatement stmt = (PreparedStatement) resultado;
                    return Proxy.newProxyInstance(PreparedStatement.class.getClassLoader(), new Class<?>[]{PreparedStatement.class}, (ps, sm, sa) -> {
                        if (sm.getName().equals("getGeneratedKeys")) {
                            Statement consulta = conn.createStatement();
                            consulta.closeOnCompletion();
                            return consulta.executeQuery("SELECT id_empleado FROM empleado WHERE 1 = 0");
                        }
                        try { return sm.invoke(stmt, sa); }
                        catch (java.lang.reflect.InvocationTargetException e) { throw e.getCause(); }
                    });
                }
                return resultado;
            });
        });
    }

    @FunctionalInterface
    private interface Accion { void ejecutar() throws Exception; }

    private record DatosPrueba(String url, String usuario, String clave) implements DataSource {
        public Connection getConnection() throws SQLException { return DriverManager.getConnection(url, usuario, clave); }
        public Connection getConnection(String usuario, String clave) throws SQLException { return DriverManager.getConnection(url, usuario, clave); }
        public PrintWriter getLogWriter() { return null; }
        public void setLogWriter(PrintWriter salida) { }
        public void setLoginTimeout(int segundos) { }
        public int getLoginTimeout() { return 0; }
        public Logger getParentLogger() throws SQLFeatureNotSupportedException { throw new SQLFeatureNotSupportedException(); }
        public <T> T unwrap(Class<T> tipo) throws SQLException { throw new SQLException("No disponible"); }
        public boolean isWrapperFor(Class<?> tipo) { return false; }
    }
}
