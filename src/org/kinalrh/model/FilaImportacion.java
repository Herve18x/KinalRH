package org.kinalrh.model;

import java.time.LocalDate;
import java.util.List;

public final class FilaImportacion {
    public enum Accion { NUEVO, ACTUALIZAR, ERROR }
    public record Error(int fila, String columna, String mensaje) {
        @Override public String toString() { return "Fila " + fila + ", " + columna + ": " + mensaje; }
    }
    private final int numeroFila;
    private final List<String> valores;
    private final List<Error> errores;
    private final Accion accion;
    private final Long idExistente, idEstado, idArea, idPuesto;

    public FilaImportacion(int numeroFila, List<String> valores) {
        this(numeroFila, valores, List.of(), Accion.ERROR, null, null, null, null);
    }
    public FilaImportacion(int numeroFila, List<String> valores, List<Error> errores, Accion accion,
            Long idExistente, Long idEstado, Long idArea, Long idPuesto) {
        this.numeroFila = numeroFila; this.valores = List.copyOf(valores); this.errores = List.copyOf(errores);
        this.accion = accion; this.idExistente = idExistente;
        this.idEstado = idEstado; this.idArea = idArea; this.idPuesto = idPuesto;
    }
    public int getNumeroFila() { return numeroFila; }
    public List<String> getValores() { return valores; }
    public String valor(int indice) { return indice < valores.size() ? valores.get(indice) : ""; }
    public List<Error> getErrores() { return errores; }
    public String getMensajes() { return String.join("; ", errores.stream().map(Error::toString).toList()); }
    public Accion getAccion() { return accion; }
    public Long getIdExistente() { return idExistente; }
    public Long getIdEstado() { return idEstado; }
    public Long getIdArea() { return idArea; }
    public Long getIdPuesto() { return idPuesto; }
    public Empleado empleadoPropuesto() {
        if (accion == Accion.ERROR || !errores.isEmpty()) throw new IllegalStateException("La fila contiene errores.");
        Empleado e = new Empleado();
        e.setIdEmpleado(idExistente); e.setDpi(valor(0)); e.setPrimerNombre(valor(1));
        e.setPrimerApellido(valor(2)); e.setIdEstadoEmpleado(idEstado);
        if (idArea != null) e.setIdAreaPrincipal(idArea);
        if (idPuesto != null) e.setIdPuestoActual(idPuesto);
        if (!valor(6).isEmpty()) e.setFechaIngresoKinal(LocalDate.parse(valor(6)));
        return e;
    }
}
