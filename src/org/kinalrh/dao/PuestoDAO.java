package org.kinalrh.dao;

import java.util.List;
import org.kinalrh.model.Puesto;

/**
 * Interfaz de Acceso a Datos (DAO) para el catálogo de Puestos.
 * Tarea T2.05 · HU US-05 / US-06
 */
public interface PuestoDAO {

    /**
     * Obtiene el listado de puestos activos ordenados alfabéticamente por nombre.
     */
    List<Puesto> listarActivos();

    /**
     * Busca un puesto específico por su identificador primario (id_puesto).
     */
    Puesto buscarPorId(Long idPuesto);

    /**
     * Registra un nuevo puesto en el catálogo.
     * Utiliza consultas parametrizadas (PreparedStatement).
     * @throws org.kinalrh.util.DuplicateRecordException Si se violan uq_puesto_codigo o uq_puesto_nombre.
     */
    boolean crear(Puesto puesto);

    /**
     * Actualiza los datos de un puesto existente (codigo, nombre, descripcion, activo).
     * Utiliza consultas parametrizadas (PreparedStatement).
     * @throws org.kinalrh.util.DuplicateRecordException Si se violan uq_puesto_codigo o uq_puesto_nombre.
     */
    boolean actualizar(Puesto puesto);

    /**
     * Elimina físicamente un puesto si y solo si no está asignado a ningún colaborador.
     * @throws org.kinalrh.util.IntegrityViolationException Si el puesto está referenciado por algún empleado.
     */
    boolean eliminar(Long idPuesto);

    /**
     * Verifica si existen colaboradores asignados al puesto (en id_puesto_actual).
     */
    boolean tieneEmpleadosAsociados(Long idPuesto);
}
