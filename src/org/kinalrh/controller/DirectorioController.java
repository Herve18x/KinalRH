package org.kinalrh.controller;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import org.kinalrh.model.Empleado;

public class DirectorioController implements Initializable {

    @FXML private TextField txtBusqueda;
    @FXML private TilePane tileDirectorio;

    private List<Empleado> todosEmpleados = new ArrayList<>();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cargarEmpleados();
        txtBusqueda.textProperty().addListener((obs, oldVal, newVal) -> filtrarTarjetas(newVal));
    }

    private void cargarEmpleados() {
        new org.kinalrh.service.AutorizacionService().exigirPermiso("EMPLEADO_VER");
        try {
            todosEmpleados = new org.kinalrh.service.EmpleadoService().listarEmpleados(
                    org.kinalrh.util.SessionManager.getInstance().getCurrentUser().getRol());
            renderizarTarjetas(todosEmpleados);
        } catch (Exception e) {
            System.err.println("Error al cargar colaboradores para el directorio: " + e.getMessage());
        }
    }

    private void filtrarTarjetas(String filtro) {
        if (filtro == null || filtro.trim().isEmpty()) {
            renderizarTarjetas(todosEmpleados);
            return;
        }

        String f = filtro.toLowerCase().trim();
        List<Empleado> filtrados = todosEmpleados.stream()
            .filter(e -> (e.getNombre() != null && e.getNombre().toLowerCase().contains(f)) ||
                         (e.getPuesto() != null && e.getPuesto().toLowerCase().contains(f)) ||
                         (e.getArea() != null && e.getArea().toLowerCase().contains(f)))
            .toList();

        renderizarTarjetas(filtrados);
    }

    private void renderizarTarjetas(List<Empleado> empleados) {
        tileDirectorio.getChildren().clear();

        if (empleados.isEmpty()) {
            Label vacio = new Label("No se encontraron colaboradores que coincidan con la búsqueda.");
            vacio.setStyle("-fx-font-size: 14px; -fx-text-fill: #64748b; -fx-padding: 30;");
            tileDirectorio.getChildren().add(vacio);
            return;
        }

        for (Empleado e : empleados) {
            VBox tarjeta = new VBox(8.0);
            tarjeta.setAlignment(Pos.CENTER);
            tarjeta.setPrefWidth(220.0);
            tarjeta.setPadding(new Insets(15.0));
            tarjeta.setStyle("-fx-border-color: #cbd5e1; -fx-border-radius: 8px; -fx-background-radius: 8px; " +
                             "-fx-background-color: #ffffff; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 5, 0, 0, 2);");

            Label avatar = new Label("👤");
            avatar.setStyle("-fx-font-size: 40px;");

            Label nombre = new Label(e.getNombre() != null ? e.getNombre() : "Colaborador");
            nombre.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-alignment: center; -fx-wrap-text: true;");

            Label puesto = new Label(e.getPuesto() != null ? e.getPuesto() : "Puesto no asignado");
            puesto.setStyle("-fx-font-size: 11px; -fx-text-fill: #334155; -fx-text-alignment: center;");

            Label area = new Label(e.getArea() != null ? e.getArea() : "Área no asignada");
            area.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b; -fx-text-alignment: center;");

            Label estado = new Label(e.getEstado() != null ? "● " + e.getEstado() : "● Activo");
            estado.setStyle("-fx-font-size: 10px; -fx-text-fill: #16a34a; -fx-font-weight: bold;");

            tarjeta.getChildren().addAll(avatar, nombre, puesto, area, estado);
            tileDirectorio.getChildren().add(tarjeta);
        }
    }
}
