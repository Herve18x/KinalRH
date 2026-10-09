package org.kinalrh.model;

public class Empleado {
    private int idEmpleado;
    private String nombre;
    private String estado;
    private String area;
    private String puesto;

    public Empleado() {}

    public Empleado(int idEmpleado, String nombre, String estado, String area, String puesto) {
        this.idEmpleado = idEmpleado;
        this.nombre = nombre;
        this.estado = estado;
        this.area = area;
        this.puesto = puesto;
    }

    public int getIdEmpleado() { return idEmpleado; }
    public void setIdEmpleado(int idEmpleado) { this.idEmpleado = idEmpleado; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getArea() { return area; }
    public void setArea(String area) { this.area = area; }

    public String getPuesto() { return puesto; }
    public void setPuesto(String puesto) { this.puesto = puesto; }
}