package org.kinalrh.controller;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import org.kinalrh.model.PermisoDTO;
import org.kinalrh.model.Rol;
import org.kinalrh.util.Conexion;

public class RolesController implements Initializable {

    @FXML private TableView<Rol> tablaRoles;
    @FXML private TableColumn<Rol, Long> colRolId;
    @FXML private TableColumn<Rol, String> colRolNombre;
    @FXML private TableColumn<Rol, String> colRolDescripcion;

    @FXML private TableView<PermisoDTO> tablaPermisos;
    @FXML private TableColumn<PermisoDTO, String> colPermisoCodigo;
    @FXML private TableColumn<PermisoDTO, String> colPermisoNombre;
    @FXML private TableColumn<PermisoDTO, String> colPermisoModulo;
    @FXML private TableColumn<PermisoDTO, String> colPermisoDescripcion;

    @FXML private Label lblTituloPermisos;

    private final ObservableList<Rol> listaRoles = FXCollections.observableArrayList();
    private final ObservableList<PermisoDTO> listaPermisos = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colRolId.setCellValueFactory(new PropertyValueFactory<>("idRol"));
        colRolNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colRolDescripcion.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        tablaRoles.setItems(listaRoles);

        colPermisoCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        colPermisoNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colPermisoModulo.setCellValueFactory(new PropertyValueFactory<>("modulo"));
        colPermisoDescripcion.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        tablaPermisos.setItems(listaPermisos);

        tablaRoles.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                cargarPermisosDeRol(newVal);
            }
        });

        tablaPermisos.setPlaceholder(new Label("Seleccione un rol para consultar sus permisos asignados."));
        cargarRoles();
    }

    private void cargarRoles() {
        listaRoles.clear();
        String sql = "SELECT id_rol, nombre, descripcion FROM rol ORDER BY id_rol";
        try (Connection conn = Conexion.getInstancia().conectar();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Rol r = new Rol();
                r.setIdRol(rs.getLong("id_rol"));
                r.setNombre(rs.getString("nombre"));
                r.setDescripcion(rs.getString("descripcion"));
                listaRoles.add(r);
            }

            if (!listaRoles.isEmpty()) {
                tablaRoles.getSelectionModel().selectFirst();
            }
        } catch (SQLException e) {
            System.err.println("Error al cargar roles: " + e.getMessage());
        }
    }

    private void cargarPermisosDeRol(Rol rol) {
        listaPermisos.clear();
        if (lblTituloPermisos != null) {
            lblTituloPermisos.setText("Permisos asignados al rol: " + rol.getNombre().toUpperCase());
        }

        String sql = "SELECT p.codigo, p.nombre, p.modulo, p.descripcion " +
                     "FROM permiso p " +
                     "JOIN rol_permiso rp ON p.id_permiso = rp.id_permiso " +
                     "WHERE rp.id_rol = ? " +
                     "ORDER BY p.modulo, p.codigo";

        try (Connection conn = Conexion.getInstancia().conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, rol.getIdRol());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    listaPermisos.add(new PermisoDTO(
                        rs.getString("codigo"),
                        rs.getString("nombre"),
                        rs.getString("modulo"),
                        rs.getString("descripcion")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al cargar permisos para rol " + rol.getNombre() + ": " + e.getMessage());
        }
    }
}
