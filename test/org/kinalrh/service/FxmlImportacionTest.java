package org.kinalrh.service;

import java.util.concurrent.*;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

public class FxmlImportacionTest {
    public static void main(String[] args) throws Exception {
        CountDownLatch inicio=new CountDownLatch(1);
        Platform.startup(inicio::countDown);
        if(!inicio.await(20,TimeUnit.SECONDS)) throw new AssertionError("JavaFX no inició");
        FutureTask<Void> tarea=new FutureTask<>(()-> {
            FXMLLoader loader=new FXMLLoader(FxmlImportacionTest.class.getResource("/org/kinalrh/view/Importacion.fxml"));
            Parent vista=loader.load();
            if(vista==null || !(loader.getController() instanceof org.kinalrh.controller.ImportacionController)) throw new AssertionError("FXML sin controlador");
            System.out.println("FxmlImportacionTest: FXMLLoader carga la vista y sus acciones.");
            return null;
        });
        Platform.runLater(tarea);
        try{tarea.get(20,TimeUnit.SECONDS);}finally{Platform.exit();}
    }
}
