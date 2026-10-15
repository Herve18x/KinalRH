package org.kinalrh.model;

import java.sql.Timestamp;

public class SolicitudCambio {
    private Long idSolicitud;
    private String fecha;
    private String colaborador;
    private String motivo;
    private String campoAnterior;
    private String campoNuevo;
    private String estado;

    public SolicitudCambio() {}

    public SolicitudCambio(Long idSolicitud, String fecha, String colaborador, String motivo, String campoAnterior, String campoNuevo, String estado) {
        this.idSolicitud = idSolicitud;
        this.fecha = fecha;
        this.colaborador = colaborador;
        this.motivo = motivo;
        this.campoAnterior = campoAnterior;
        this.campoNuevo = campoNuevo;
        this.estado = estado;
    }

    public Long getIdSolicitud() { return idSolicitud; }
    public void setIdSolicitud(Long idSolicitud) { this.idSolicitud = idSolicitud; }

    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }

    public String getColaborador() { return colaborador; }
    public void setColaborador(String colaborador) { this.colaborador = colaborador; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }

    public String getCampoAnterior() { return campoAnterior; }
    public void setCampoAnterior(String campoAnterior) { this.campoAnterior = campoAnterior; }

    public String getCampoNuevo() { return campoNuevo; }
    public void setCampoNuevo(String campoNuevo) { this.campoNuevo = campoNuevo; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}
