package org.kinalrh.model;

public class NivelAcademico {
    private Long idNivelAcademico;
    private String codigo;
    private String nombre;
    private int ordenNivel;
    private boolean activo = true;

    public NivelAcademico() {
    }

    public NivelAcademico(Long idNivelAcademico, String nombre) {
        this.idNivelAcademico = idNivelAcademico;
        this.nombre = nombre;
    }

    public NivelAcademico(Long idNivelAcademico, String codigo, String nombre, int ordenNivel, boolean activo) {
        this.idNivelAcademico = idNivelAcademico;
        this.codigo = codigo;
        this.nombre = nombre;
        this.ordenNivel = ordenNivel;
        this.activo = activo;
    }

    public Long getIdNivelAcademico() { return idNivelAcademico; }
    public void setIdNivelAcademico(Long idNivelAcademico) { this.idNivelAcademico = idNivelAcademico; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public int getOrdenNivel() { return ordenNivel; }
    public void setOrdenNivel(int ordenNivel) { this.ordenNivel = ordenNivel; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    @Override
    public String toString() {
        return nombre == null ? "" : nombre;
    }
}