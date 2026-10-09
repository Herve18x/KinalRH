package org.kinalrh.model;

import java.time.LocalDate;

public class Empleado {
    // Basic identifiers
    private Long idEmpleado;
    private String dpi;
    private String nit;
    private String codigoEmpleado;

    // Names
    private String primerNombre;
    private String segundoNombre;
    private String tercerNombre;
    private String primerApellido;
    private String segundoApellido;
    private String apellidoCasada;
    private String nombreCompleto;

    // Personal details
    private LocalDate fechaNacimiento;
    private String estadoCivil;
    private String religion;

    // Address
    private String direccion;
    private String zona;
    private String municipio;
    private String departamento;

    // Contact
    private String telefonoMovil;
    private String telefonoFijo;
    private String correoPersonal;

    // Internal Kinal
    private LocalDate fechaIngresoKinal;

    // Relational IDs
    private Long idAreaPrincipal;
    private Long idPuestoActual;
    private Long idEstadoEmpleado;
    private Long idNivelAcademico;
    private Long idJefeInmediato;

    // Display fields for UI (from JOINs)
    private String nombre; // Fallback for tableview
    private String estado;
    private String area;
    private String puesto;

    public Empleado() {}

    public Long getIdEmpleado() { return idEmpleado; }
    public void setIdEmpleado(Long idEmpleado) { this.idEmpleado = idEmpleado; }

    public String getDpi() { return dpi; }
    public void setDpi(String dpi) { this.dpi = dpi; }

    public String getNit() { return nit; }
    public void setNit(String nit) { this.nit = nit; }

    public String getCodigoEmpleado() { return codigoEmpleado; }
    public void setCodigoEmpleado(String codigoEmpleado) { this.codigoEmpleado = codigoEmpleado; }

    public String getPrimerNombre() { return primerNombre; }
    public void setPrimerNombre(String primerNombre) { this.primerNombre = primerNombre; }

    public String getSegundoNombre() { return segundoNombre; }
    public void setSegundoNombre(String segundoNombre) { this.segundoNombre = segundoNombre; }

    public String getTercerNombre() { return tercerNombre; }
    public void setTercerNombre(String tercerNombre) { this.tercerNombre = tercerNombre; }

    public String getPrimerApellido() { return primerApellido; }
    public void setPrimerApellido(String primerApellido) { this.primerApellido = primerApellido; }

    public String getSegundoApellido() { return segundoApellido; }
    public void setSegundoApellido(String segundoApellido) { this.segundoApellido = segundoApellido; }

    public String getApellidoCasada() { return apellidoCasada; }
    public void setApellidoCasada(String apellidoCasada) { this.apellidoCasada = apellidoCasada; }

    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }

    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(LocalDate fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }

    public String getEstadoCivil() { return estadoCivil; }
    public void setEstadoCivil(String estadoCivil) { this.estadoCivil = estadoCivil; }

    public String getReligion() { return religion; }
    public void setReligion(String religion) { this.religion = religion; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getZona() { return zona; }
    public void setZona(String zona) { this.zona = zona; }

    public String getMunicipio() { return municipio; }
    public void setMunicipio(String municipio) { this.municipio = municipio; }

    public String getDepartamento() { return departamento; }
    public void setDepartamento(String departamento) { this.departamento = departamento; }

    public String getTelefonoMovil() { return telefonoMovil; }
    public void setTelefonoMovil(String telefonoMovil) { this.telefonoMovil = telefonoMovil; }

    public String getTelefonoFijo() { return telefonoFijo; }
    public void setTelefonoFijo(String telefonoFijo) { this.telefonoFijo = telefonoFijo; }

    public String getCorreoPersonal() { return correoPersonal; }
    public void setCorreoPersonal(String correoPersonal) { this.correoPersonal = correoPersonal; }

    public LocalDate getFechaIngresoKinal() { return fechaIngresoKinal; }
    public void setFechaIngresoKinal(LocalDate fechaIngresoKinal) { this.fechaIngresoKinal = fechaIngresoKinal; }

    public Long getIdAreaPrincipal() { return idAreaPrincipal; }
    public void setIdAreaPrincipal(Long idAreaPrincipal) { this.idAreaPrincipal = idAreaPrincipal; }

    public Long getIdPuestoActual() { return idPuestoActual; }
    public void setIdPuestoActual(Long idPuestoActual) { this.idPuestoActual = idPuestoActual; }

    public Long getIdEstadoEmpleado() { return idEstadoEmpleado; }
    public void setIdEstadoEmpleado(Long idEstadoEmpleado) { this.idEstadoEmpleado = idEstadoEmpleado; }

    public Long getIdNivelAcademico() { return idNivelAcademico; }
    public void setIdNivelAcademico(Long idNivelAcademico) { this.idNivelAcademico = idNivelAcademico; }

    public Long getIdJefeInmediato() { return idJefeInmediato; }
    public void setIdJefeInmediato(Long idJefeInmediato) { this.idJefeInmediato = idJefeInmediato; }

    // Getters/Setters for UI (TableView backwards compatibility)
    public String getNombre() { 
        if (nombreCompleto != null && !nombreCompleto.isEmpty()) return nombreCompleto;
        return (primerNombre != null ? primerNombre : "") + " " + (primerApellido != null ? primerApellido : ""); 
    }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getArea() { return area; }
    public void setArea(String area) { this.area = area; }
    public String getPuesto() { return puesto; }
    public void setPuesto(String puesto) { this.puesto = puesto; }
}