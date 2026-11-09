package com.alquilerpianos.repositorio;

import com.alquilerpianos.dominio.Cliente;
import java.util.List;
import java.util.Optional;

public interface ClienteRepositorio extends IRepositorioExtend<Cliente, Integer> {
    Optional<Cliente> findByEmail(String email);
    List<Cliente> findByApellidos(String apellidos);
}
