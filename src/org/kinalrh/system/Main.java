package org.kinalrh.system;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.image.Image;

public class Main extends Application {

    private static Stage escenarioPrincipal;

    @Override
    public void start(Stage stage) throws Exception {
        escenarioPrincipal = stage;
        try {
            stage.getIcons().add(new Image(Main.class.getResourceAsStream("/org/kinalrh/image/icon.png")));
        } catch(Exception e) { System.out.println("Error cargando icono"); }
        cambiarVista("/org/kinalrh/view/Login.fxml");
        stage.setTitle("Kinal RH - Iniciar Sesión");
        stage.show();
    }

    public static void cambiarVista(String fxml) throws Exception {
        Parent root = FXMLLoader.load(Main.class.getResource(fxml));
        escenarioPrincipal.setScene(new Scene(root));
    }

    public static Stage getEscenarioPrincipal() {
        return escenarioPrincipal;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
