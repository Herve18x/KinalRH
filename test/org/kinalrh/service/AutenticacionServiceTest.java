package org.kinalrh.service;

import java.nio.file.Path;
import java.sql.*;
import java.util.UUID;
import org.kinalrh.PruebaBD;
import org.kinalrh.model.Usuario;
import org.kinalrh.util.PasswordHasher;
import org.kinalrh.util.SessionManager;
import static org.kinalrh.PruebaBD.verificar;
import static org.kinalrh.service.ImportacionXlsxServiceTest.denegado;

public class AutenticacionServiceTest {
    public static void main(String[] args) throws Exception {
        try(PruebaBD bd=new PruebaBD(Path.of(args[0]),false)) {
            String clave=UUID.randomUUID().toString();
            try(Connection c=bd.datos.getConnection();PreparedStatement s=c.prepareStatement(
                    "INSERT INTO usuario(nombre_usuario,password_hash,nombre_completo) VALUES ('autenticacion_test',?,'Cuenta ficticia')")) {
                s.setString(1,PasswordHasher.hashPassword(clave));s.executeUpdate();
            }
            AutenticacionService servicio=new AutenticacionService();
            Usuario u=servicio.autenticar("autenticacion_test",clave);
            verificar(u.getIdUsuario()!=null,"Credencial correcta");
            denegado(()->servicio.autenticar("autenticacion_test","incorrecta"));
            denegado(()->servicio.autenticar("no_existe",clave));
            bd.sql("UPDATE usuario SET activo=FALSE WHERE nombre_usuario='autenticacion_test'");
            denegado(()->servicio.autenticar("autenticacion_test",clave));
            bd.sql("UPDATE usuario SET activo=TRUE WHERE nombre_usuario='autenticacion_test'");
            SesionService.getInstance().iniciarSesion(u);
            verificar(SessionManager.getInstance().getCurrentUser()==u,"Inicio de sesión");
            String anterior=SessionManager.getInstance().getSessionUUID();
            SesionService.getInstance().cerrarSesion();
            verificar(SessionManager.getInstance().getCurrentUser()==null && SessionManager.getInstance().getSessionUUID()==null,"Logout");
            verificar(SesionService.getInstance().getUsuarioAutenticado()==null && !SesionService.getInstance().tienePermiso("EMPLEADO_VER"),"Sin sesión residual");
            SesionService.getInstance().iniciarSesion(u);
            verificar(!anterior.equals(SessionManager.getInstance().getSessionUUID()),"Nueva sesión distinta");
            SessionManager.getInstance().logout();
            verificar(SesionService.getInstance().getUsuarioAutenticado()==null,"Logout directo también invalida sesión");
            System.out.println("AutenticacionServiceTest: credencial correcta/incorrecta, inexistente, inactiva, sesión y logout correctos.");
        }
    }
}
