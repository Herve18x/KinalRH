package org.kinalrh.dao;

import java.util.List;
import org.kinalrh.model.Area;

public interface AreaDAO {
    List<Area> listarActivas();
    Area buscarPorId(Long id);
}