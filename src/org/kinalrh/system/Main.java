package org.kinalrh.system;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    private static Stage escenarioPrincipal;

    @Override
    public void start(Stage stage) throws Exception {
        escenarioPrincipal = stage;
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
