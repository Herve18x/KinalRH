package org.kinalrh.service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.kinalrh.dao.NivelAcademicoDAO;
import org.kinalrh.dao.TipoLogroDAO;
import org.kinalrh.dao.impl.NivelAcademicoDAOImpl;
import org.kinalrh.dao.impl.TipoLogroDAOImpl;
import org.kinalrh.model.NivelAcademico;
import org.kinalrh.model.TipoLogro;

/**
 * Servicio para la gestión centralizada de catálogos (Nivel Académico, Tipo de Logro, etc.).
 * T3.09 Lectura/gestión básica de nivel académico y tipo de logro.
 * T3.12 Verificar que solo se guarda un nivel válido y activo.
 */
public class CatalogoService {

    private final NivelAcademicoDAO nivelAcademicoDAO;
    private final TipoLogroDAO tipoLogroDAO;
    private final AutorizacionService autorizacionService;
    private final AuditoriaService auditoriaService;

    public CatalogoService() {
        this.nivelAcademicoDAO = new NivelAcademicoDAOImpl();
        this.tipoLogroDAO = new TipoLogroDAOImpl();
        this.autorizacionService = new AutorizacionService();
        this.auditoriaService = new AuditoriaService();
    }

    private void validarPermisoConsulta(String rolActual) {
        if (rolActual == null || rolActual.trim().isEmpty()) {
            throw new SecurityException("Acceso denegado: Sesión no válida.");
        }
    }

    private void validarPermisoGestion(String rolActual) {
        if (rolActual == null || rolActual.trim().isEmpty()) {
            throw new SecurityException("Acceso denegado: Sesión no válida.");
        }
        String r = rolActual.trim().toUpperCase();
        if (!autorizacionService.tienePermiso(rolActual, "CATALOGO_GESTIONAR")
                && !r.equals("ADMIN") && !r.equals("ENCARGADO") && !r.equals("RRHH")) {
            throw new SecurityException("Acceso denegado: Su cuenta no tiene permisos para administrar catálogos.");
        }
    }

    // --- NIVEL ACADÉMICO ---
    public List<NivelAcademico> listarNivelesAcademicos(String rolActual) {
        validarPermisoConsulta(rolActual);
        try {
            return nivelAcademicoDAO.listarTodos();
        } catch (SQLException e) {
            System.err.println("Error al listar niveles académicos: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<NivelAcademico> listarNivelesAcademicosActivos() {
        return nivelAcademicoDAO.listarActivos();
    }

    public long guardarNivelAcademico(NivelAcademico nivel, String rolActual) {
        validarPermisoGestion(rolActual);
        if (nivel == null) {
            throw new IllegalArgumentException("Error: Datos del nivel académico inválidos.");
        }
        if (nivel.getCodigo() == null || nivel.getCodigo().trim().isEmpty()) {
            throw new IllegalArgumentException("Error: El código del nivel académico es obligatorio.");
        }
        if (nivel.getNombre() == null || nivel.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("Error: El nombre del nivel académico es obligatorio.");
        }

        Long idExcluido = (nivel.getIdNivelAcademico() != null && nivel.getIdNivelAcademico() > 0) ? nivel.getIdNivelAcademico() : null;
        if (nivelAcademicoDAO.existeCodigo(nivel.getCodigo(), idExcluido)) {
            throw new IllegalArgumentException("Error: Ya existe un nivel académico con el código " + nivel.getCodigo());
        }
        if (nivelAcademicoDAO.existeNombre(nivel.getNombre(), idExcluido)) {
            throw new IllegalArgumentException("Error: Ya existe un nivel académico con el nombre " + nivel.getNombre());
        }

        try {
            if (idExcluido == null) {
                long id = nivelAcademicoDAO.insertar(nivel);
                auditoriaService.registrarEvento("CREAR_NIVEL_ACADEMICO", "nivel_academico", id, null, null, nivel.getNombre());
                return id;
            } else {
                nivelAcademicoDAO.actualizar(nivel);
                auditoriaService.registrarEvento("ACTUALIZAR_NIVEL_ACADEMICO", "nivel_academico", nivel.getIdNivelAcademico(), null, null, nivel.getNombre());
                return nivel.getIdNivelAcademico();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar nivel académico: " + e.getMessage(), e);
        }
    }

    public void cambiarEstadoNivelAcademico(Long idNivel, boolean activo, String rolActual) {
        validarPermisoGestion(rolActual);
        if (idNivel == null || idNivel <= 0) {
            throw new IllegalArgumentException("Error: ID de nivel académico inválido.");
        }
        try {
            nivelAcademicoDAO.cambiarEstado(idNivel, activo);
            auditoriaService.registrarEvento("ESTADO_NIVEL_ACADEMICO", "nivel_academico", idNivel, "activo", null, String.valueOf(activo));
        } catch (SQLException e) {
            throw new RuntimeException("Error al cambiar estado de nivel académico: " + e.getMessage(), e);
        }
    }

    public boolean esNivelValidoYActivo(Long idNivel) {
        return nivelAcademicoDAO.esNivelValidoYActivo(idNivel);
    }

    // --- TIPO DE LOGRO ---
    public List<TipoLogro> listarTiposLogro(String rolActual) {
        validarPermisoConsulta(rolActual);
        try {
            return tipoLogroDAO.listarTodos();
        } catch (SQLException e) {
            System.err.println("Error al listar tipos de logro: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<TipoLogro> listarTiposLogroActivos() {
        return tipoLogroDAO.listarActivos();
    }

    public long guardarTipoLogro(TipoLogro tipo, String rolActual) {
        validarPermisoGestion(rolActual);
        if (tipo == null) {
            throw new IllegalArgumentException("Error: Datos del tipo de logro inválidos.");
        }
        if (tipo.getCodigo() == null || tipo.getCodigo().trim().isEmpty()) {
            throw new IllegalArgumentException("Error: El código del tipo de logro es obligatorio.");
        }
        if (tipo.getNombre() == null || tipo.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("Error: El nombre del tipo de logro es obligatorio.");
        }

        Long idExcluido = (tipo.getIdTipoLogro() != null && tipo.getIdTipoLogro() > 0) ? tipo.getIdTipoLogro() : null;
        if (tipoLogroDAO.existeCodigo(tipo.getCodigo(), idExcluido)) {
            throw new IllegalArgumentException("Error: Ya existe un tipo de logro con el código " + tipo.getCodigo());
        }
        if (tipoLogroDAO.existeNombre(tipo.getNombre(), idExcluido)) {
            throw new IllegalArgumentException("Error: Ya existe un tipo de logro con el nombre " + tipo.getNombre());
        }

        try {
            if (idExcluido == null) {
                long id = tipoLogroDAO.insertar(tipo);
                auditoriaService.registrarEvento("CREAR_TIPO_LOGRO", "tipo_logro", id, null, null, tipo.getNombre());
                return id;
            } else {
                tipoLogroDAO.actualizar(tipo);
                auditoriaService.registrarEvento("ACTUALIZAR_TIPO_LOGRO", "tipo_logro", tipo.getIdTipoLogro(), null, null, tipo.getNombre());
                return tipo.getIdTipoLogro();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar tipo de logro: " + e.getMessage(), e);
        }
    }

    public void cambiarEstadoTipoLogro(Long idTipo, boolean activo, String rolActual) {
        validarPermisoGestion(rolActual);
        if (idTipo == null || idTipo <= 0) {
            throw new IllegalArgumentException("Error: ID de tipo de logro inválido.");
        }
        try {
            tipoLogroDAO.cambiarEstado(idTipo, activo);
            auditoriaService.registrarEvento("ESTADO_TIPO_LOGRO", "tipo_logro", idTipo, "activo", null, String.valueOf(activo));
        } catch (SQLException e) {
            throw new RuntimeException("Error al cambiar estado de tipo de logro: " + e.getMessage(), e);
        }
    }
}
