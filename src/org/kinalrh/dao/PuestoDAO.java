package org.kinalrh.dao;

import java.util.List;
import org.kinalrh.model.Puesto;

public interface PuestoDAO {
    List<Puesto> listarActivos();
    Puesto buscarPorId(Long id);
}