package org.kinalrh.model;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class Rol {

    private Long idRol;
    private String nombre;
    private String descripcion;
    private boolean activo = true;
    private Timestamp creadoEn;
    private Timestamp actualizadoEn;
    private List<Permiso> permisos = new ArrayList<>();

    public Rol() {
    }

    public Rol(Long idRol, String nombre, boolean activo) {
        this.idRol = idRol;
        this.nombre = nombre;
        this.activo = activo;
    }

    public Rol(Long idRol, String nombre, String descripcion, boolean activo,
            Timestamp creadoEn, Timestamp actualizadoEn, List<Permiso> permisos) {
        this(idRol, nombre, activo);
        this.descripcion = descripcion;
        this.creadoEn = creadoEn;
        this.actualizadoEn = actualizadoEn;
        setPermisos(permisos);
    }

    public Long getIdRol() {
        return idRol;
    }

    public void setIdRol(Long idRol) {
        this.idRol = idRol;
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

    public List<Permiso> getPermisos() {
        return permisos;
    }

    public void setPermisos(List<Permiso> permisos) {
        this.permisos = permisos == null ? new ArrayList<>() : new ArrayList<>(permisos);
    }
}