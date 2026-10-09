package org.kinalrh.model;

import java.time.LocalDate;

public class LogroEmpleadoDTO {
    private Long idLogro;
    private String nombreEmpleado;
    private String nombreLogro;
    private String institucion;
    private LocalDate fechaObtencion;

    public LogroEmpleadoDTO(Long idLogro, String nombreEmpleado, String nombreLogro, String institucion, LocalDate fechaObtencion) {
        this.idLogro = idLogro;
        this.nombreEmpleado = nombreEmpleado;
        this.nombreLogro = nombreLogro;
        this.institucion = institucion;
        this.fechaObtencion = fechaObtencion;
    }

    public Long getIdLogro() { return idLogro; }
    public String getNombreEmpleado() { return nombreEmpleado; }
    public String getNombreLogro() { return nombreLogro; }
    public String getInstitucion() { return institucion; }
    public LocalDate getFechaObtencion() { return fechaObtencion; }
}
