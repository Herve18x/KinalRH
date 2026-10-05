package org.kinalrh.model;

public class Empleado {
    private String codigo;
    private String nombre;
    private String apellido;
    private String puesto;
    private String departamento;

    public Empleado() {}

    public Empleado(String codigo, String nombre, String apellido, String puesto, String departamento) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.apellido = apellido;
        this.puesto = puesto;
        this.departamento = departamento;
    }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }

    public String getPuesto() { return puesto; }
    public void setPuesto(String puesto) { this.puesto = puesto; }

    public String getDepartamento() { return departamento; }
    public void setDepartamento(String departamento) { this.departamento = departamento; }
}
