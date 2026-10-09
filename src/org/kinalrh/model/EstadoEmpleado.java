package org.kinalrh.model;

import java.sql.Timestamp;

public class EstadoEmpleado {

    private Long idEstadoEmpleado;
    private String codigo;
    private String nombre;
    private String descripcion;
    private Boolean esEstadoActivo;
    private Boolean activo;
    private Timestamp creadoEn;
    private Timestamp actualizadoEn;

    public EstadoEmpleado() {
    }

    public EstadoEmpleado(Long idEstadoEmpleado, String codigo, String nombre, String descripcion, Boolean esEstadoActivo, Boolean activo) {
        this.idEstadoEmpleado = idEstadoEmpleado;
        this.codigo = codigo;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.esEstadoActivo = esEstadoActivo;
        this.activo = activo;
    }

    public Long getIdEstadoEmpleado() { return idEstadoEmpleado; }
    public void setIdEstadoEmpleado(Long idEstadoEmpleado) { this.idEstadoEmpleado = idEstadoEmpleado; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public Boolean getEsEstadoActivo() { return esEstadoActivo; }
    public void setEsEstadoActivo(Boolean esEstadoActivo) { this.esEstadoActivo = esEstadoActivo; }

    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }

    public Timestamp getCreadoEn() { return creadoEn; }
    public void setCreadoEn(Timestamp creadoEn) { this.creadoEn = creadoEn; }

    public Timestamp getActualizadoEn() { return actualizadoEn; }
    public void setActualizadoEn(Timestamp actualizadoEn) { this.actualizadoEn = actualizadoEn; }
}