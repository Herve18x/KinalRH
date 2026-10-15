package org.kinalrh.model;

public class PermisoDTO {
    private String codigo;
    private String nombre;
    private String modulo;
    private String descripcion;

    public PermisoDTO() {}

    public PermisoDTO(String codigo, String nombre, String modulo, String descripcion) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.modulo = modulo;
        this.descripcion = descripcion;
    }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getModulo() { return modulo; }
    public void setModulo(String modulo) { this.modulo = modulo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
}
