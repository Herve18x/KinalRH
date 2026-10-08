package org.kinalrh.model;
 
/**
* Modelo entidad Puesto para la gestión de cargos en la empresa.
*
* @author danielcatalan
*/
public class Puesto {
 
    private Long idPuesto;
    private String codigo;
    private String nombre;
    private String descripcion;
    private boolean activo = true;
 
    public Puesto() {
    }
 
    public Puesto(Long idPuesto, String codigo, String nombre,
            String descripcion, boolean activo) {
        this.idPuesto = idPuesto;
        this.codigo = codigo;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.activo = activo;
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
 
    @Override
    public String toString() {
        return nombre == null ? "" : nombre;
    }
}