package org.kinalrh.model;

public class EstadoEmpleado {

    private Long idEstadoEmpleado;
    private String codigo;
    private String nombre;
    private Boolean esEstadoActivo;

    public EstadoEmpleado() {
    }

    public EstadoEmpleado(Long idEstadoEmpleado, String codigo, String nombre, Boolean esEstadoActivo) {
        this.idEstadoEmpleado = idEstadoEmpleado;
        this.codigo = codigo;
        this.nombre = nombre;
        this.esEstadoActivo = esEstadoActivo;
    }

    public Long getIdEstadoEmpleado() {
        return idEstadoEmpleado;
    }

    public void setIdEstadoEmpleado(Long idEstadoEmpleado) {
        this.idEstadoEmpleado = idEstadoEmpleado;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Boolean getEsEstadoActivo() {
        return esEstadoActivo;
    }

    public void setEsEstadoActivo(Boolean esEstadoActivo) {
        this.esEstadoActivo = esEstadoActivo;
    }
}