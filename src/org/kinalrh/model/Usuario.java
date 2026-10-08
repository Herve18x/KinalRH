package org.kinalrh.model;

import java.sql.Timestamp;
import java.util.UUID;

/**
 * Modelo de entidad para la tabla 'usuario'.
 * Representa las cuentas de usuario del sistema Kinal RH.
 * 
 * Regla de negocio T1.21 / HU US-34:
 * - 'id_usuario' se mantiene como Primary Key (autoincrement).
 * - 'uuid_usuario' es un identificador aleatorio único e INMUTABLE asignado
 *   una sola vez al crear la cuenta (mediante UUID.randomUUID()).
 * - No se expone setter público ni se ofrece en formularios de edición.
 */
public class Usuario {

    private Long idUsuario;
    private final String uuidUsuario; // Inmutable: final garantiza que no cambie tras instanciarse
    private String nombreUsuario;
    private String passwordHash;
    private String nombreCompleto;
    private String correo;
    private boolean activo;
    private Long idEmpleado;
    private Timestamp ultimoAccesoEn;
    private Timestamp creadoEn;
    private Timestamp actualizadoEn;

    /**
     * Constructor por defecto para NUEVAS cuentas.
     * Genera automáticamente un UUID aleatorio único una sola vez.
     */
    public Usuario() {
        this.uuidUsuario = UUID.randomUUID().toString();
        this.activo = true;
    }

    /**
     * Constructor de conveniencia para registrar una nueva cuenta con datos iniciales.
     */
    public Usuario(String nombreUsuario, String passwordHash, String nombreCompleto, String correo) {
        this();
        this.nombreUsuario = nombreUsuario;
        this.passwordHash = passwordHash;
        this.nombreCompleto = nombreCompleto;
        this.correo = correo;
    }

    /**
     * Constructor para hidratar/reconstruir el modelo desde la base de datos (queries SELECT).
     */
    public Usuario(Long idUsuario, String uuidUsuario, String nombreUsuario, String passwordHash,
                   String nombreCompleto, String correo, boolean activo, Long idEmpleado,
                   Timestamp ultimoAccesoEn, Timestamp creadoEn, Timestamp actualizadoEn) {
        this.idUsuario = idUsuario;
        this.uuidUsuario = (uuidUsuario != null && !uuidUsuario.trim().isEmpty())
                ? uuidUsuario
                : UUID.randomUUID().toString();
        this.nombreUsuario = nombreUsuario;
        this.passwordHash = passwordHash;
        this.nombreCompleto = nombreCompleto;
        this.correo = correo;
        this.activo = activo;
        this.idEmpleado = idEmpleado;
        this.ultimoAccesoEn = ultimoAccesoEn;
        this.creadoEn = creadoEn;
        this.actualizadoEn = actualizadoEn;
    }

    // ==========================================
    // GETTERS Y SETTERS
    // ==========================================

    public Long getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Long idUsuario) {
        this.idUsuario = idUsuario;
    }

    /**
     * Retorna el UUID inmutable de la cuenta.
     * NOTA DE ARQUITECTURA: No se provee 'setUuidUsuario' público para garantizar
     * la inmutabilidad y evitar que sea alterado en formularios o controladores.
     */
    public String getUuidUsuario() {
        return uuidUsuario;
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

    @Override
    public String toString() {
        return "Usuario{" +
                "idUsuario=" + idUsuario +
                ", uuidUsuario='" + uuidUsuario + '\'' +
                ", nombreUsuario='" + nombreUsuario + '\'' +
                ", correo='" + correo + '\'' +
                ", activo=" + activo +
                '}';
    }
}
