package com.alquilerpianos.repositorio;

import com.alquilerpianos.dominio.EstadoPiano;
import com.alquilerpianos.dominio.Piano;
import com.alquilerpianos.dominio.TipoPiano;
import java.util.List;

public interface PianoRepositorio extends IRepositorioExtend<Piano, Integer> {
    List<Piano> findByEstado(EstadoPiano estado);
    List<Piano> findByMarca(String marca);
    List<Piano> findByTipo(TipoPiano tipo);
}
