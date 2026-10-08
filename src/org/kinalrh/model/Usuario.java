package org.kinalrh.model;
 
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
 
public class Usuario {
 
    private Long idUsuario;
    private String nombreUsuario;
    private String passwordHash;
    private String nombreCompleto;
    private String correo;
    private boolean activo = true;
    private Long idEmpleado;
    private Timestamp ultimoAccesoEn;
    private Timestamp creadoEn;
    private Timestamp actualizadoEn;
    private List<Rol> roles = new ArrayList<>();
 
    public Usuario() {
    }
 
    public Usuario(Long idUsuario, String nombreUsuario, String passwordHash,
            String nombreCompleto, String correo, boolean activo, Long idEmpleado) {
        this.idUsuario = idUsuario;
        this.nombreUsuario = nombreUsuario;
        this.passwordHash = passwordHash;
        this.nombreCompleto = nombreCompleto;
        this.correo = correo;
        this.activo = activo;
        this.idEmpleado = idEmpleado;
    }
 
    public Usuario(Long idUsuario, String nombreUsuario, String passwordHash,
            String nombreCompleto, String correo, boolean activo, Long idEmpleado,
            Timestamp ultimoAccesoEn, Timestamp creadoEn, Timestamp actualizadoEn,
            List<Rol> roles) {
        this(idUsuario, nombreUsuario, passwordHash, nombreCompleto,
                correo, activo, idEmpleado);
        this.ultimoAccesoEn = ultimoAccesoEn;
        this.creadoEn = creadoEn;
        this.actualizadoEn = actualizadoEn;
        setRoles(roles);
    }
 
    public Long getIdUsuario() {
        return idUsuario;
    }
 
    public void setIdUsuario(Long idUsuario) {
        this.idUsuario = idUsuario;
    }
 
    public String getNombreUsuario() {
        return nombreUsuario;
    }
 
    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }
 
    public String getPasswordHash() {
        return passwordHash;
    }
 
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
 
    public String getNombreCompleto() {
        return nombreCompleto;
    }
 
    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }
 
    public String getCorreo() {
        return correo;
    }
 
    public void setCorreo(String correo) {
        this.correo = correo;
    }
 
    public boolean isActivo() {
        return activo;
    }
 
    public void setActivo(boolean activo) {
        this.activo = activo;
    }
 
    public Long getIdEmpleado() {
        return idEmpleado;
    }
 
    public void setIdEmpleado(Long idEmpleado) {
        this.idEmpleado = idEmpleado;
    }
 
    public Timestamp getUltimoAccesoEn() {
        return ultimoAccesoEn;
    }
 
    public void setUltimoAccesoEn(Timestamp ultimoAccesoEn) {
        this.ultimoAccesoEn = ultimoAccesoEn;
    }
 
    public Timestamp getCreadoEn() {
        return creadoEn;
    }
 
    public void setCreadoEn(Timestamp creadoEn) {
        this.creadoEn = creadoEn;
    }
 
    public Timestamp getActualizadoEn() {
        return actualizadoEn;
    }
 
    public void setActualizadoEn(Timestamp actualizadoEn) {
        this.actualizadoEn = actualizadoEn;
    }
 
    public List<Rol> getRoles() {
        return roles;
    }
 
    public void setRoles(List<Rol> roles) {
        this.roles = roles == null ? new ArrayList<>() : new ArrayList<>(roles);
    }
}
