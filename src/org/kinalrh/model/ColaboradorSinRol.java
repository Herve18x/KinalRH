package org.kinalrh.model;

/**
 * DTO para representar colaboradores que han sido creados en RRHH
 * pero aún carecen de credenciales de usuario o no tienen un rol asignado.
 */
public class ColaboradorSinRol {

    private Long idEmpleado;
    private String dpi;
    private String nombreCompleto;
    private String correoPersonal;
    private Long idUsuario;
    private String nombreUsuario;
    private String estadoAcceso;

    public ColaboradorSinRol() {
    }

    public ColaboradorSinRol(Long idEmpleado, String dpi, String nombreCompleto,
                             String correoPersonal, Long idUsuario, String nombreUsuario,
                             String estadoAcceso) {
        this.idEmpleado = idEmpleado;
        this.dpi = dpi;
        this.nombreCompleto = nombreCompleto;
        this.correoPersonal = correoPersonal;
        this.idUsuario = idUsuario;
        this.nombreUsuario = nombreUsuario;
        this.estadoAcceso = estadoAcceso;
    }

    public Long getIdEmpleado() {
        return idEmpleado;
    }

    public void setIdEmpleado(Long idEmpleado) {
        this.idEmpleado = idEmpleado;
    }

    public String getDpi() {
        return dpi;
    }

    public void setDpi(String dpi) {
        this.dpi = dpi;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    public String getCorreoPersonal() {
        return correoPersonal;
    }

    public void setCorreoPersonal(String correoPersonal) {
        this.correoPersonal = correoPersonal;
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

    public String getEstadoAcceso() {
        return estadoAcceso;
    }

    public void setEstadoAcceso(String estadoAcceso) {
        this.estadoAcceso = estadoAcceso;
    }

    @Override
    public String toString() {
        String doc = (dpi != null && !dpi.trim().isEmpty()) ? " [DPI: " + dpi + "]" : "";
        String est = (estadoAcceso != null && !estadoAcceso.trim().isEmpty()) ? " - " + estadoAcceso : "";
        return (nombreCompleto != null ? nombreCompleto : "Colaborador #" + idEmpleado) + doc + est;
    }
}
