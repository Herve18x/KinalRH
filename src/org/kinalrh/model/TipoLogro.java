package org.kinalrh.model;

/**
 * Modelo para el catálogo tipo_logro.
 * T3.09 Lectura/gestión básica de nivel académico y tipo de logro.
 */
public class TipoLogro {

    private Long idTipoLogro;
    private String codigo;
    private String nombre;
    private String descripcion;
    private boolean activo = true;

    public TipoLogro() {
    }

    public TipoLogro(Long idTipoLogro, String nombre) {
        this.idTipoLogro = idTipoLogro;
        this.nombre = nombre;
    }

    public TipoLogro(Long idTipoLogro, String codigo, String nombre, String descripcion, boolean activo) {
        this.idTipoLogro = idTipoLogro;
        this.codigo = codigo;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.activo = activo;
    }

    public Long getIdTipoLogro() {
        return idTipoLogro;
    }

    public void setIdTipoLogro(Long idTipoLogro) {
        this.idTipoLogro = idTipoLogro;
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

    @Override
    public String toString() {
        return nombre != null ? nombre : "";
    }
}
