package org.kinalrh.model;

import java.sql.Timestamp;

/**
 * Entidad modelo que representa un Puesto laboral en la tabla 'puesto'.
 */
public class Puesto {

    private Long idPuesto;
    private String codigo;
    private String nombre;
    private String descripcion;
    private boolean activo;
    private Timestamp creadoEn;
    private Timestamp actualizadoEn;

    public Puesto() {
        this.activo = true;
    }

    public Puesto(String codigo, String nombre, String descripcion) {
        this();
        this.codigo = codigo;
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    public Puesto(Long idPuesto, String codigo, String nombre, String descripcion, boolean activo) {
        this.idPuesto = idPuesto;
        this.codigo = codigo;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.activo = activo;
    }

    public Puesto(Long idPuesto, String codigo, String nombre, String descripcion, boolean activo,
                  Timestamp creadoEn, Timestamp actualizadoEn) {
        this.idPuesto = idPuesto;
        this.codigo = codigo;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.activo = activo;
        this.creadoEn = creadoEn;
        this.actualizadoEn = actualizadoEn;
    }

    public Long getIdPuesto() {
        return idPuesto;
    }

    public void setIdPuesto(Long idPuesto) {
        this.idPuesto = idPuesto;
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
        return "Puesto{" +
                "idPuesto=" + idPuesto +
                ", codigo='" + codigo + '\'' +
                ", nombre='" + nombre + '\'' +
                ", activo=" + activo +
                '}';
    }
}
