package org.kinalrh.model;

import java.sql.Timestamp;

/**
 * Entidad modelo que representa un Área de la institución en la tabla 'area'.
 */
public class Area {

    private Long idArea;
    private String codigo;
    private String nombre;
    private String descripcion;
    private boolean activo;
    private Timestamp creadoEn;
    private Timestamp actualizadoEn;

    public Area() {
        this.activo = true;
    }

    public Area(String codigo, String nombre, String descripcion) {
        this();
        this.codigo = codigo;
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    public Area(Long idArea, String codigo, String nombre, String descripcion, boolean activo) {
        this.idArea = idArea;
        this.codigo = codigo;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.activo = activo;
    }

    public Area(Long idArea, String codigo, String nombre, String descripcion, boolean activo,
                Timestamp creadoEn, Timestamp actualizadoEn) {
        this.idArea = idArea;
        this.codigo = codigo;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.activo = activo;
        this.creadoEn = creadoEn;
        this.actualizadoEn = actualizadoEn;
    }

    public Long getIdArea() {
        return idArea;
    }

    public void setIdArea(Long idArea) {
        this.idArea = idArea;
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

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public Timestamp getCreadoEn() {
        return creadoEn;
    }

    public void setCreadoEn(Timestamp creadoEn) {
        this.creadoEn = creadoEn;
    }

    public Timestamp getActualizadoEn() {
        return actualizadoEn;
    }

    public void setActualizadoEn(Timestamp actualizadoEn) {
        this.actualizadoEn = actualizadoEn;
    }

    @Override
    public String toString() {
        return "Area{" +
                "idArea=" + idArea +
                ", codigo='" + codigo + '\'' +
                ", nombre='" + nombre + '\'' +
                ", activo=" + activo +
                '}';
    }
}
