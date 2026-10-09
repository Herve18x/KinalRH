package org.kinalrh.model;

public class Empleado {
    private Long idEmpleado;
    private String dpi;
    private String primerNombre;
    private String primerApellido;
    
    private Long idAreaPrincipal;
    private Long idPuestoActual;
    private Long idEstadoEmpleado;

    // Display fields for UI
    private String nombre;
    private String estado;
    private String area;
    private String puesto;

    public Empleado() {}

    public Long getIdEmpleado() { return idEmpleado; }
    public void setIdEmpleado(Long idEmpleado) { this.idEmpleado = idEmpleado; }

    public String getDpi() { return dpi; }
    public void setDpi(String dpi) { this.dpi = dpi; }

    public String getPrimerNombre() { return primerNombre; }
    public void setPrimerNombre(String primerNombre) { this.primerNombre = primerNombre; }

    public String getPrimerApellido() { return primerApellido; }
    public void setPrimerApellido(String primerApellido) { this.primerApellido = primerApellido; }

    public Long getIdAreaPrincipal() { return idAreaPrincipal; }
    public void setIdAreaPrincipal(Long idAreaPrincipal) { this.idAreaPrincipal = idAreaPrincipal; }

    public Long getIdPuestoActual() { return idPuestoActual; }
    public void setIdPuestoActual(Long idPuestoActual) { this.idPuestoActual = idPuestoActual; }

    public Long getIdEstadoEmpleado() { return idEstadoEmpleado; }
    public void setIdEstadoEmpleado(Long idEstadoEmpleado) { this.idEstadoEmpleado = idEstadoEmpleado; }

    // Getters/Setters for UI
    public String getNombre() { return nombre != null ? nombre : (primerNombre + " " + primerApellido); }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getArea() { return area; }
    public void setArea(String area) { this.area = area; }

    public String getPuesto() { return puesto; }
    public void setPuesto(String puesto) { this.puesto = puesto; }
}