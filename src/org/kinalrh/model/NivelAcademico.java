package org.kinalrh.model;

public class NivelAcademico {
    private Long idNivelAcademico;
    private String nombre;

    public NivelAcademico(Long idNivelAcademico, String nombre) {
        this.idNivelAcademico = idNivelAcademico;
        this.nombre = nombre;
    }

    public Long getIdNivelAcademico() { return idNivelAcademico; }
    public void setIdNivelAcademico(Long idNivelAcademico) { this.idNivelAcademico = idNivelAcademico; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    @Override
    public String toString() {
        return nombre;
    }
}