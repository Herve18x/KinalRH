package org.kinalrh.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Modelo para la entidad y registros de la tabla auditoria.
 * T1.23, T3.18 Log de Auditoría y Trazabilidad.
 */
public class Auditoria {

    private Long idAuditoria;
    private Long idUsuario;
    private String nombreUsuario;
    private String accion;
    private String entidad;
    private Long registroId;
    private String campo;
    private String valorAnterior;
    private String valorNuevo;
    private String ip;
    private String userAgent;
    private LocalDateTime fechaHora;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    public Auditoria() {
    }

    public Auditoria(Long idAuditoria, Long idUsuario, String nombreUsuario, String accion, String entidad,
                     Long registroId, String campo, String valorAnterior, String valorNuevo,
                     String ip, String userAgent, LocalDateTime fechaHora) {
        this.idAuditoria = idAuditoria;
        this.idUsuario = idUsuario;
        this.nombreUsuario = nombreUsuario;
        this.accion = accion;
        this.entidad = entidad;
        this.registroId = registroId;
        this.campo = campo;
        this.valorAnterior = valorAnterior;
        this.valorNuevo = valorNuevo;
        this.ip = ip;
        this.userAgent = userAgent;
        this.fechaHora = fechaHora;
    }

    public Long getIdAuditoria() {
        return idAuditoria;
    }

    public void setIdAuditoria(Long idAuditoria) {
        this.idAuditoria = idAuditoria;
    }

    public Long getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Long idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNombreUsuario() {
        return nombreUsuario != null ? nombreUsuario : "SISTEMA";
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public String getAccion() {
        return accion;
    }

    public void setAccion(String accion) {
        this.accion = accion;
    }

    public String getEntidad() {
        return entidad;
    }

    public void setEntidad(String entidad) {
        this.entidad = entidad;
    }

    public Long getRegistroId() {
        return registroId;
    }

    public void setRegistroId(Long registroId) {
        this.registroId = registroId;
    }

    public String getCampo() {
        return campo != null ? campo : "-";
    }

    public void setCampo(String campo) {
        this.campo = campo;
    }

    public String getValorAnterior() {
        return valorAnterior != null ? valorAnterior : "-";
    }

    public void setValorAnterior(String valorAnterior) {
        this.valorAnterior = valorAnterior;
    }

    public String getValorNuevo() {
        return valorNuevo != null ? valorNuevo : "-";
    }

    public void setValorNuevo(String valorNuevo) {
        this.valorNuevo = valorNuevo;
    }

    public String getIp() {
        return ip != null ? ip : "127.0.0.1";
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getUserAgent() {
        return userAgent != null ? userAgent : "";
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public String getFechaHoraFormateada() {
        return fechaHora != null ? fechaHora.format(FORMATTER) : "-";
    }
}
