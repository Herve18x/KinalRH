package org.kinalrh.service;

import java.nio.file.Path;
import java.util.*;
import org.kinalrh.PruebaBD;
import org.kinalrh.dao.impl.UsuarioDAOImpl;
import org.kinalrh.model.Usuario;
import static org.kinalrh.PruebaBD.verificar;

public class UsuarioTransaccionTest {
    public static void main(String[] args) throws Exception {
        try (PruebaBD bd = new PruebaBD(Path.of(args[0]), false)) {
            ImportacionXlsxServiceTest.preparar(bd);
            UsuarioService servicio = new UsuarioService();
            Usuario u = new Usuario(); u.setUsername("reintento"); u.setNombre("Cuenta ficticia");
            u.setRol("visor"); u.setPasswordHash("ClaveOriginal!123");
            List<String> fallos = new ArrayList<>();
            bd.sql("CREATE TRIGGER fallo_rol BEFORE INSERT ON usuario_rol FOR EACH ROW SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Fallo controlado'");
            try { servicio.guardarUsuario(u,"ADMIN"); throw new AssertionError("Alta parcial permitida"); }
            catch (IllegalStateException esperado) { }
            verificar(u.getIdUsuario() == null && bd.numero("SELECT COUNT(*) FROM usuario") == 2, "Rollback de alta y su ID en memoria");
            bd.sql("DROP TRIGGER fallo_rol");
            servicio.guardarUsuario(u,"ADMIN");
            try { new AutenticacionService().autenticar("reintento","ClaveOriginal!123"); }
            catch (SecurityException ex) { fallos.add("Reintentar alta después de rollback altera la contraseña"); }

            Usuario edicion = new UsuarioDAOImpl().buscarPorId(u.getIdUsuario()).orElseThrow();
            edicion.setPasswordHash("ClaveNueva!456"); edicion.setNombre("Cambio ficticio");
            bd.sql("CREATE TRIGGER fallo_rol BEFORE INSERT ON usuario_rol FOR EACH ROW SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Fallo controlado'");
            try { servicio.actualizarUsuario(edicion,"ADMIN"); throw new AssertionError("Edición parcial permitida"); }
            catch (IllegalStateException esperado) { }
            verificar(bd.numero("SELECT COUNT(*) FROM usuario WHERE nombre_completo='Cambio ficticio'") == 0, "Rollback de edición");
            verificar(bd.numero("SELECT COUNT(*) FROM usuario_rol WHERE id_usuario=" + u.getIdUsuario()) == 1, "Rollback conserva el rol");
            bd.sql("DROP TRIGGER fallo_rol");
            servicio.actualizarUsuario(edicion,"ADMIN");
            try { new AutenticacionService().autenticar("reintento","ClaveNueva!456"); }
            catch (SecurityException ex) { fallos.add("Reintentar edición después de rollback altera la contraseña"); }
            fallos.forEach(f -> System.out.println("FAIL: " + f));
            verificar(fallos.isEmpty(), "Fallos en reintentos: " + fallos.size());
            System.out.println("UsuarioTransaccionTest: rollback de cuenta/rol y reintentos sin cambiar contraseñas correctos.");
        }
    }
}
