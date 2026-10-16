package org.kinalrh.service;

import java.nio.file.Path;
import java.util.concurrent.*;
import org.kinalrh.PruebaBD;
import org.kinalrh.dao.impl.UsuarioDAOImpl;
import static org.kinalrh.PruebaBD.verificar;

/** Dos conexiones reales compiten por retirar las últimas dos cuentas admin. */
public class UsuarioConcurrenciaTest {
    public static void main(String[] args) throws Exception {
        try (PruebaBD bd = new PruebaBD(Path.of(args[0]), false)) {
            ImportacionXlsxServiceTest.preparar(bd);
            bd.sql("INSERT IGNORE INTO usuario_rol SELECT u.id_usuario,r.id_rol FROM usuario u JOIN rol r ON r.nombre='admin'");
            try (ExecutorService hilos = Executors.newFixedThreadPool(2)) {
                for (int vuelta = 0; vuelta < 10; vuelta++) {
                    bd.sql("UPDATE usuario SET activo=TRUE");
                    CountDownLatch inicio = new CountDownLatch(1);
                    Future<Boolean> uno = hilos.submit(() -> retirar(1, inicio));
                    Future<Boolean> dos = hilos.submit(() -> retirar(2, inicio));
                    inicio.countDown();
                    boolean a = uno.get(15, TimeUnit.SECONDS), b = dos.get(15, TimeUnit.SECONDS);
                    verificar(a != b, "Solo una de las bajas concurrentes debe confirmarse");
                    verificar(new UsuarioDAOImpl().contarAdministradoresActivos() == 1, "Debe conservarse un administrador");
                }
            }
            System.out.println("UsuarioConcurrenciaTest: diez carreras con dos conexiones conservan el último administrador.");
        }
    }
    private static boolean retirar(long id, CountDownLatch inicio) throws InterruptedException {
        inicio.await();
        try { new UsuarioDAOImpl().desactivar(id); return true; }
        catch (IllegalStateException esperado) {
            verificar(esperado.getMessage().contains("última cuenta"), "Rechazo por último admin, no por un fallo SQL");
            return false;
        }
    }
}
