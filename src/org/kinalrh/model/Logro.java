package org.kinalrh.model;

import java.time.LocalDate;

public class Logro {
    private Long idLogro;
    private Long idTipoLogro;
    private String nombre;
    private String descripcion;
    private String institucion;
    private LocalDate fechaObtencion;
    private String origen;
    private Boolean financiadoPorKinal;
    private String detalleFinanciacion;
    private Boolean activo;

    public Logro() {}

    public Long getIdLogro() { return idLogro; }
    public void setIdLogro(Long idLogro) { this.idLogro = idLogro; }

    public Long getIdTipoLogro() { return idTipoLogro; }
    public void setIdTipoLogro(Long idTipoLogro) { this.idTipoLogro = idTipoLogro; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getInstitucion() { return institucion; }
    public void setInstitucion(String institucion) { this.institucion = institucion; }

    public LocalDate getFechaObtencion() { return fechaObtencion; }
    public void setFechaObtencion(LocalDate fechaObtencion) { this.fechaObtencion = fechaObtencion; }

    public String getOrigen() { return origen; }
    public void setOrigen(String origen) { this.origen = origen; }

    public Boolean getFinanciadoPorKinal() { return financiadoPorKinal; }
    public void setFinanciadoPorKinal(Boolean financiadoPorKinal) { this.financiadoPorKinal = financiadoPorKinal; }

    public String getDetalleFinanciacion() { return detalleFinanciacion; }
    public void setDetalleFinanciacion(String detalleFinanciacion) { this.detalleFinanciacion = detalleFinanciacion; }

    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
}