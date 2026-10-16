package org.kinalrh.controller;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.regex.Pattern;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.kinalrh.model.Empleado;
import org.kinalrh.model.Area;
import org.kinalrh.model.Puesto;
import org.kinalrh.model.NivelAcademico;
import org.kinalrh.dao.impl.NivelAcademicoDAOImpl;
import org.kinalrh.dao.impl.EmpleadoDAOImpl;
import org.kinalrh.model.EstadoEmpleado;
import org.kinalrh.dao.impl.AreaDAOImpl;
import org.kinalrh.dao.impl.PuestoDAOImpl;
import org.kinalrh.dao.impl.EstadoEmpleadoDAOImpl;
import org.kinalrh.service.EmpleadoService;

public class EmpleadoFormController implements Initializable {

    @FXML private Label lblTitulo;
    @FXML private Label lblError;
    
    // Identificación
    @FXML private TextField txtId;
    @FXML private TextField txtDpi;
    @FXML private TextField txtNit;
    @FXML private TextField txtCodigo;
    
    // Nombres y Apellidos
    @FXML private TextField txtPrimerNombre;
    @FXML private TextField txtSegundoNombre;
    @FXML private TextField txtTercerNombre;
    @FXML private TextField txtPrimerApellido;
    @FXML private TextField txtSegundoApellido;
    
    // Personales
    @FXML private DatePicker dpFechaNac;
    @FXML private ComboBox<String> cmbEstadoCivil;
    @FXML private TextField txtTelefonoMovil;
    @FXML private TextField txtTelefonoFijo;
    @FXML private TextField txtCorreo;
    
    // Dirección
    @FXML private TextField txtDireccion;
    @FXML private TextField txtZona;
    @FXML private TextField txtMunicipio;
    @FXML private TextField txtDepartamento;
    
    // Institucionales
    @FXML private DatePicker dpFechaIngreso;
    @FXML private ComboBox<Area> cmbArea;
    @FXML private ComboBox<Puesto> cmbPuesto;
    @FXML private ComboBox<EstadoEmpleado> cmbEstado;
    @FXML private ComboBox<NivelAcademico> cmbNivelAcademico;
    @FXML private ComboBox<Empleado> cmbJefe;
    
    private Empleado empleado;
    private Stage stage;
    private EmpleadoService empleadoService;
    
    private AreaDAOImpl areaDAO = new AreaDAOImpl();
    private PuestoDAOImpl puestoDAO = new PuestoDAOImpl();
    private EstadoEmpleadoDAOImpl estadoDAO = new EstadoEmpleadoDAOImpl();
    private NivelAcademicoDAOImpl nivelAcademicoDAO = new NivelAcademicoDAOImpl();
    private EmpleadoDAOImpl empleadoDAO = new EmpleadoDAOImpl();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        empleadoService = new EmpleadoService();
        cargarCatalogos();
    }
    
    private void cargarCatalogos() {
        new org.kinalrh.service.AutorizacionService().exigirAlgunPermiso("EMPLEADO_VER", "EMPLEADO_CREAR", "EMPLEADO_EDITAR");
        cmbArea.getItems().setAll(areaDAO.listarActivas());
        cmbPuesto.getItems().setAll(puestoDAO.listarActivos());
        cmbEstado.getItems().setAll(estadoDAO.listarTodos());
        
        // Pre-seleccionar estado Activo por defecto para nuevos registros
        preseleccionarEstadoActivo();
        
        try {
            cmbNivelAcademico.getItems().setAll(nivelAcademicoDAO.listarActivos());
            cmbJefe.getItems().setAll(empleadoDAO.listarTodos());
        } catch (Exception ex) { ex.printStackTrace(); }
        cmbEstadoCivil.getItems().addAll("Soltero(a)", "Casado(a)", "Divorciado(a)", "Viudo(a)", "Unido(a)");
    }

    private void preseleccionarEstadoActivo() {
        if (cmbEstado == null) return;
        for (EstadoEmpleado est : cmbEstado.getItems()) {
            if ("ACTIVO".equalsIgnoreCase(est.getCodigo()) || "Activo".equalsIgnoreCase(est.getNombre())) {
                cmbEstado.setValue(est);
                return;
            }
        }
        if (!cmbEstado.getItems().isEmpty()) {
            cmbEstado.getSelectionModel().selectFirst();
        }
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setEmpleado(Empleado emp) {
        if (emp == null) {
            this.empleado = new Empleado();
        } else {
            this.empleado = emp;
        }
        
        if (emp != null && emp.getIdEmpleado() != null && emp.getIdEmpleado() > 0) {
            lblTitulo.setText("Editar Empleado");
            txtId.setText(String.valueOf(emp.getIdEmpleado()));
            txtDpi.setText(emp.getDpi());
            txtNit.setText(emp.getNit());
            txtCodigo.setText(emp.getCodigoEmpleado());
            
            txtPrimerNombre.setText(emp.getPrimerNombre());
            txtSegundoNombre.setText(emp.getSegundoNombre());
            txtTercerNombre.setText(emp.getTercerNombre());
            txtPrimerApellido.setText(emp.getPrimerApellido());
            txtSegundoApellido.setText(emp.getSegundoApellido());
            
            dpFechaNac.setValue(emp.getFechaNacimiento());
            cmbEstadoCivil.setValue(emp.getEstadoCivil());
            txtTelefonoMovil.setText(emp.getTelefonoMovil());
            txtTelefonoFijo.setText(emp.getTelefonoFijo());
            txtCorreo.setText(emp.getCorreoPersonal());
            
            txtDireccion.setText(emp.getDireccion());
            txtZona.setText(emp.getZona());
            txtMunicipio.setText(emp.getMunicipio());
            txtDepartamento.setText(emp.getDepartamento());
            
            dpFechaIngreso.setValue(emp.getFechaIngresoKinal());
            
            seleccionarArea(emp.getIdAreaPrincipal());
            seleccionarPuesto(emp.getIdPuestoActual());
            seleccionarEstado(emp.getIdEstadoEmpleado());
            seleccionarNivelAcademico(emp.getIdNivelAcademico());
            seleccionarJefe(emp.getIdJefeInmediato());
        } else {
            lblTitulo.setText("Nuevo Empleado");
            this.empleado = new Empleado();
            preseleccionarEstadoActivo();
        }
    }
    
    private void seleccionarArea(Long id) {
        if(id == null) return;
        for(Area a : cmbArea.getItems()) {
            if(a.getIdArea().equals(id)) {
                cmbArea.getSelectionModel().select(a);
                break;
            }
        }
    }
    
    private void seleccionarPuesto(Long id) {
        if(id == null) return;
        for(Puesto p : cmbPuesto.getItems()) {
            if(p.getIdPuesto().equals(id)) {
                cmbPuesto.getSelectionModel().select(p);
                break;
            }
        }
    }
    
    private void seleccionarNivelAcademico(Long id) {
        if (id == null) return;
        for (NivelAcademico n : cmbNivelAcademico.getItems()) {
            if (n.getIdNivelAcademico().equals(id)) {
                cmbNivelAcademico.getSelectionModel().select(n);
                return;
            }
        }
        NivelAcademico hist = nivelAcademicoDAO.buscarPorId(id);
        if (hist != null) {
            cmbNivelAcademico.getItems().add(hist);
            cmbNivelAcademico.getSelectionModel().select(hist);
        }
    }
    private void seleccionarJefe(Long id) {
        if(id == null) return;
        for(Empleado e : cmbJefe.getItems()) {
            if(e.getIdEmpleado().equals(id)) {
                cmbJefe.getSelectionModel().select(e);
                break;
            }
        }
    }
    private void seleccionarEstado(Long id) {
        if(id == null) return;
        for(EstadoEmpleado e : cmbEstado.getItems()) {
            if(e.getIdEstadoEmpleado().equals(id)) {
                cmbEstado.getSelectionModel().select(e);
                break;
            }
        }
    }

    private String limpiarTexto(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    @FXML
    public void eventoGuardar(ActionEvent event) {
        if (!validarEntradas()) {
            return;
        }

        empleado.setDpi(limpiarTexto(txtDpi.getText()));
        empleado.setNit(limpiarTexto(txtNit.getText()));
        empleado.setCodigoEmpleado(limpiarTexto(txtCodigo.getText()));
        
        empleado.setPrimerNombre(limpiarTexto(txtPrimerNombre.getText()));
        empleado.setSegundoNombre(limpiarTexto(txtSegundoNombre.getText()));
        empleado.setTercerNombre(limpiarTexto(txtTercerNombre.getText()));
        empleado.setPrimerApellido(limpiarTexto(txtPrimerApellido.getText()));
        empleado.setSegundoApellido(limpiarTexto(txtSegundoApellido.getText()));
        empleado.setApellidoCasada(null); // Eliminado del formulario
        
        empleado.setFechaNacimiento(dpFechaNac.getValue());
        empleado.setEstadoCivil(cmbEstadoCivil.getValue());
        empleado.setTelefonoMovil(limpiarTexto(txtTelefonoMovil.getText()));
        empleado.setTelefonoFijo(limpiarTexto(txtTelefonoFijo.getText()));
        empleado.setCorreoPersonal(limpiarTexto(txtCorreo.getText()));
        
        empleado.setDireccion(limpiarTexto(txtDireccion.getText()));
        empleado.setZona(limpiarTexto(txtZona.getText()));
        empleado.setMunicipio(limpiarTexto(txtMunicipio.getText()));
        empleado.setDepartamento(limpiarTexto(txtDepartamento.getText()));
        
        empleado.setFechaIngresoKinal(dpFechaIngreso.getValue());
        
        empleado.setIdAreaPrincipal(cmbArea.getValue() != null ? cmbArea.getValue().getIdArea() : null);
        empleado.setIdPuestoActual(cmbPuesto.getValue() != null ? cmbPuesto.getValue().getIdPuesto() : null);
        empleado.setIdEstadoEmpleado(cmbEstado.getValue().getIdEstadoEmpleado());
        empleado.setIdNivelAcademico(cmbNivelAcademico.getValue() != null ? cmbNivelAcademico.getValue().getIdNivelAcademico() : null);
        empleado.setIdJefeInmediato(cmbJefe.getValue() != null ? cmbJefe.getValue().getIdEmpleado() : null);

        try {
            String rolActual = "ADMIN";
            if (org.kinalrh.service.SesionService.getInstance().getUsuarioAutenticado() != null) {
                rolActual = org.kinalrh.service.SesionService.getInstance().getUsuarioAutenticado().getRol();
            } else if (org.kinalrh.util.SessionManager.getInstance().getCurrentUser() != null) {
                rolActual = org.kinalrh.util.SessionManager.getInstance().getCurrentUser().getRol();
            }

            empleadoService.guardarEmpleado(empleado, rolActual);
            if (stage != null) {
                stage.close();
            }
        } catch (IllegalArgumentException | SecurityException ex) {
            lblError.setText(ex.getMessage());
            lblError.setVisible(true);
        } catch (RuntimeException ex) {
            lblError.setText(ex.getMessage() != null ? ex.getMessage() : "Error al guardar el empleado.");
            lblError.setVisible(true);
        }
    }

    private boolean validarEntradas() {
        String dpi = txtDpi.getText() != null ? txtDpi.getText().trim() : "";
        String pNombre = txtPrimerNombre.getText() != null ? txtPrimerNombre.getText().trim() : "";
        String pApellido = txtPrimerApellido.getText() != null ? txtPrimerApellido.getText().trim() : "";
        String correo = txtCorreo.getText() != null ? txtCorreo.getText().trim() : "";

        // 1. Validar campo DPI
        if (dpi.isEmpty()) {
            lblError.setText("Error: El campo DPI es obligatorio.");
            lblError.setVisible(true);
            txtDpi.requestFocus();
            return false;
        }
        if (!Pattern.matches("^\\d{13}$", dpi)) {
            lblError.setText("Error: El DPI debe contener exactamente 13 dígitos numéricos.");
            lblError.setVisible(true);
            txtDpi.requestFocus();
            return false;
        }

        // 2. Validar nombres y apellidos obligatorios
        if (pNombre.isEmpty()) {
            lblError.setText("Error: El campo Primer Nombre es obligatorio.");
            lblError.setVisible(true);
            txtPrimerNombre.requestFocus();
            return false;
        }
        if (pApellido.isEmpty()) {
            lblError.setText("Error: El campo Primer Apellido es obligatorio.");
            lblError.setVisible(true);
            txtPrimerApellido.requestFocus();
            return false;
        }

        // 3. Validar Estado Laboral institucional
        if (cmbEstado.getValue() == null) {
            lblError.setText("Error: Debe seleccionar un Estado Laboral en la sección de Datos Institucionales.");
            lblError.setVisible(true);
            cmbEstado.requestFocus();
            return false;
        }

        // 4. Validar formato de correo si se ingresó
        if (!correo.isEmpty() && !Pattern.matches("^[A-Za-z0-9+_.-]+@(.+)$", correo)) {
            lblError.setText("Error: El formato del correo personal no es válido (ej: usuario@dominio.com).");
            lblError.setVisible(true);
            txtCorreo.requestFocus();
            return false;
        }

        lblError.setVisible(false);
        return true;
    }

    @FXML
    public void eventoCancelar(ActionEvent event) {
        if (stage != null) {
            stage.close();
        }
    }
}
