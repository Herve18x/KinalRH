package org.kinalrh.dao;

import java.util.List;
import org.kinalrh.model.Area;

/**
 * Interfaz de Acceso a Datos (DAO) para el catálogo de Áreas.
 * Tarea T2.05 · HU US-05 / US-06
 */
public interface AreaDAO {

    /**
     * Obtiene el listado de áreas activas ordenadas alfabéticamente por nombre.
     */
    List<Area> listarActivos();

    /**
     * Busca un área específica por su identificador primario (id_area).
     */
    Area buscarPorId(Long idArea);

    /**
     * Registra una nueva área en el catálogo.
     * Utiliza consultas parametrizadas (PreparedStatement).
     * @throws org.kinalrh.util.DuplicateRecordException Si se violan uq_area_codigo o uq_area_nombre.
     */
    boolean crear(Area area);

    /**
     * Actualiza los datos de un área existente (codigo, nombre, descripcion, activo).
     * Utiliza consultas parametrizadas (PreparedStatement).
     * @throws org.kinalrh.util.DuplicateRecordException Si se violan uq_area_codigo o uq_area_nombre.
     */
    boolean actualizar(Area area);

    /**
     * Elimina físicamente un área de la base de datos si y solo si no está asignada a ningún colaborador.
     * @throws org.kinalrh.util.IntegrityViolationException Si el área está referenciada por algún empleado.
     */
    boolean eliminar(Long idArea);

    /**
     * Verifica si existen colaboradores asignados al área (como área principal o en empleado_area).
     */
    boolean tieneEmpleadosAsociados(Long idArea);
}
