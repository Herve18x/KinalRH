package org.kinalrh.model;

public class Empleado {

    private Long idEmpleado;
    private String dpi;
    private String primerNombre;
    private String primerApellido;
    private Long idEstadoEmpleado;
    private Long idPuestoActual;
    private Long idAreaPrincipal;

    public Empleado() {
    }

    public Empleado(Long idEmpleado, String dpi, String primerNombre, String primerApellido,
                    Long idEstadoEmpleado, Long idPuestoActual, Long idAreaPrincipal) {
        this.idEmpleado = idEmpleado;
        this.dpi = dpi;
        this.primerNombre = primerNombre;
        this.primerApellido = primerApellido;
        this.idEstadoEmpleado = idEstadoEmpleado;
        this.idPuestoActual = idPuestoActual;
        this.idAreaPrincipal = idAreaPrincipal;
    }

    public Long getIdEmpleado() {
        return idEmpleado;
    }

    public void setIdEmpleado(Long idEmpleado) {
        this.idEmpleado = idEmpleado;
    }

    public String getDpi() {
        return dpi;
    }

    public void setDpi(String dpi) {
        this.dpi = dpi;
    }

    public String getPrimerNombre() {
        return primerNombre;
    }

    public void setPrimerNombre(String primerNombre) {
        this.primerNombre = primerNombre;
    }

    public String getPrimerApellido() {
        return primerApellido;
    }

    public void setPrimerApellido(String primerApellido) {
        this.primerApellido = primerApellido;
    }

    public Long getIdEstadoEmpleado() {
        return idEstadoEmpleado;
    }

    public void setIdEstadoEmpleado(Long idEstadoEmpleado) {
        this.idEstadoEmpleado = idEstadoEmpleado;
    }

    public Long getIdPuestoActual() {
        return idPuestoActual;
    }

    public void setIdPuestoActual(Long idPuestoActual) {
        this.idPuestoActual = idPuestoActual;
    }

    public Long getIdAreaPrincipal() {
        return idAreaPrincipal;
    }

    public void setIdAreaPrincipal(Long idAreaPrincipal) {
        this.idAreaPrincipal = idAreaPrincipal;
    }
}