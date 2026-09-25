package org.kinalrh.controller;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.kinalrh.dao.UsuarioDAO;
import org.kinalrh.dao.impl.UsuarioDAOImpl;
import org.kinalrh.model.Usuario;
import org.kinalrh.system.Main;
import org.kinalrh.util.SecurityUtil;

/**
 * Controlador del Login.
 *
 * Seguridad (T1.14 / T1.06):
 * - El mensaje de error es GENÉRICO ("Usuario o contraseña incorrectos.")
 *   para no revelar si el usuario existe o si la contraseña es incorrecta.
 * - El campo de contraseña usa PasswordField (caracteres enmascarados).
 * - La contraseña NUNCA se imprime en consola ni en logs.
 */
public class LoginController implements Initializable {

    @FXML private TextField     txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private Button        btnEntrar;
    @FXML private Label         lblError;

    private UsuarioDAO usuarioDAO;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        usuarioDAO = new UsuarioDAOImpl();
        lblError.setVisible(false);
    }

    // ── Evento: botón ENTRAR ───────────────────────────────────────────────────
    @FXML
    public void eventoInicioSesion(ActionEvent evento) {
        String usuarioIngresado  = txtUsername.getText().trim();
        String passwordIngresada = txtPassword.getText().trim();

        if (usuarioIngresado.isEmpty() || passwordIngresada.isEmpty()) {
            mostrarError("Por favor, complete todos los campos.");
            return;
        }

        // Buscar usuario en BD (solo retorna si está activo)
        Usuario usuarioEncontrado = usuarioDAO.buscarPorUsername(usuarioIngresado);

        // Verificar credenciales — mensaje GENÉRICO, no revela detalles (T1.14)
        if (usuarioEncontrado == null) {
            mostrarError("Usuario o contraseña incorrectos.");
            return;
        }

        String hash = SecurityUtil.hashSHA256(passwordIngresada);
        boolean credencialesValidas = usuarioEncontrado.getPasswordHash() != null
            && (usuarioEncontrado.getPasswordHash().equals(hash)
                || usuarioEncontrado.getPasswordHash().equals(passwordIngresada));

        if (!credencialesValidas) {
            mostrarError("Usuario o contraseña incorrectos.");
            return;
        }

        abrirDashboard(usuarioEncontrado);
    }

    // ── Navegación por rol (T1.17) ─────────────────────────────────────────────
    private void abrirDashboard(Usuario usuario) {
        if (usuario.getRol() == null) {
            mostrarError("No se pudo determinar su nivel de acceso.");
            return;
        }

        String rutaFXML;
        String titulo;
        String rol = usuario.getRol().trim().toLowerCase();

        switch (rol) {
            case "admin":
                rutaFXML = "/org/kinalrh/view/Dashboard.fxml";
                titulo   = "Kinal RH – Administración";
                break;
            case "rrhh":
                rutaFXML = "/org/kinalrh/view/Dashboard.fxml";
                titulo   = "Kinal RH – Recursos Humanos";
                break;
            case "supervisor":
                rutaFXML = "/org/kinalrh/view/Dashboard.fxml";
                titulo   = "Kinal RH – Supervisor";
                break;
            default:
                mostrarError("Su cuenta no tiene acceso al sistema.");
                return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(rutaFXML));
            Parent raiz = loader.load();

            Object controlador = loader.getController();
            if (controlador instanceof BaseDashboardController) {
                ((BaseDashboardController) controlador).iniciarUsuario(usuario);
            }

            Stage stage = Main.getEscenarioPrincipal();
            stage.setScene(new Scene(raiz));
            stage.setTitle(titulo);
            stage.setResizable(true);
            stage.centerOnScreen();

        } catch (Exception e) {
            System.err.println("[LoginController] Error al cargar vista: " + e.getMessage());
            mostrarError("Error interno al abrir el panel. Contacte al administrador.");
        }
    }

    // ── Helpers ────────────────────────────────────────────────────────────────
    private void mostrarError(String mensaje) {
        lblError.setText(mensaje);
        lblError.setVisible(true);
    }
}
