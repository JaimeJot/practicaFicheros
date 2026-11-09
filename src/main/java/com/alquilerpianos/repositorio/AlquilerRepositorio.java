package com.alquilerpianos.repositorio;

import com.alquilerpianos.dominio.Alquiler;
import com.alquilerpianos.dominio.valor.PeriodoAlquiler;
import java.util.List;

public interface AlquilerRepositorio extends IRepositorioExtend<Alquiler, Integer> {
    List<Alquiler> findByClienteId(Integer idCliente);
    List<Alquiler> findByPianoId(Integer idPiano);
    boolean existeSolapamiento(Integer idPiano, PeriodoAlquiler periodo);
}
