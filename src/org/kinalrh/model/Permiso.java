package org.kinalrh.model;

import java.sql.Timestamp;

public class Permiso {

    private Long idPermiso;
    private String codigo;
    private String nombre;
    private String descripcion;
    private String modulo;
    private boolean activo = true;
    private Timestamp creadoEn;
    private Timestamp actualizadoEn;

    public Permiso() {
    }

    public Permiso(Long idPermiso, String codigo, String nombre, boolean activo) {
        this.idPermiso = idPermiso;
        this.codigo = codigo;
        this.nombre = nombre;
        this.activo = activo;
    }

    public Permiso(Long idPermiso, String codigo, String nombre,
            String descripcion, String modulo, boolean activo,
            Timestamp creadoEn, Timestamp actualizadoEn) {
        this(idPermiso, codigo, nombre, activo);
        this.descripcion = descripcion;
        this.modulo = modulo;
        this.creadoEn = creadoEn;
        this.actualizadoEn = actualizadoEn;
    }

    public Long getIdPermiso() {
        return idPermiso;
    }

    public void setIdPermiso(Long idPermiso) {
        this.idPermiso = idPermiso;
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

    public String getModulo() {
        return modulo;
    }

    public void setModulo(String modulo) {
        this.modulo = modulo;
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
}