package com.alquilerpianos.servicio;

import com.alquilerpianos.dominio.Alquiler;
import com.alquilerpianos.dominio.EstadoPiano;
import com.alquilerpianos.dominio.Piano;
import com.alquilerpianos.dominio.valor.PeriodoAlquiler;
import com.alquilerpianos.repositorio.AlquilerRepositorio;
import com.alquilerpianos.repositorio.ClienteRepositorio;
import com.alquilerpianos.repositorio.PianoRepositorio;
import java.util.List;

/**
 * Servicio de aplicación: coordina agregados y aplica las reglas entre ellos.
 * Con ficheros no hay claves foráneas, así que la integridad referencial
 * (no borrar clientes/pianos con alquileres) se controla aquí.
 */
public class AlquilerServicio {

    private final ClienteRepositorio clientes;
    private final PianoRepositorio pianos;
    private final AlquilerRepositorio alquileres;

    public AlquilerServicio(ClienteRepositorio clientes, PianoRepositorio pianos,
                            AlquilerRepositorio alquileres) {
        this.clientes = clientes;
        this.pianos = pianos;
        this.alquileres = alquileres;
    }

    public Alquiler alquilarPiano(Integer idCliente, Integer idPiano, PeriodoAlquiler periodo) {
        if (!clientes.existsById(idCliente)) {
            throw new IllegalArgumentException("No existe el cliente " + idCliente);
        }
        Piano piano = pianos.findByIdOptional(idPiano)
                .orElseThrow(() -> new IllegalArgumentException("No existe el piano " + idPiano));
        if (!piano.estaEnServicio()) {
            throw new IllegalStateException("El piano no está en servicio (" + piano.getEstado() + ")");
        }
        if (alquileres.existeSolapamiento(idPiano, periodo)) {
            throw new IllegalStateException("El piano ya está alquilado en esas fechas");
        }
        return alquileres.save(Alquiler.crear(idCliente, idPiano, periodo, piano.getPrecioDia()));
    }

    public void cancelarAlquiler(Integer idAlquiler) {
        Alquiler alquiler = alquileres.findByIdOptional(idAlquiler)
                .orElseThrow(() -> new IllegalArgumentException("No existe el alquiler " + idAlquiler));
        alquiler.cancelar();
        alquileres.save(alquiler);
    }

    /** Pianos en servicio sin ningún alquiler activo que solape con el periodo. */
    public List<Piano> pianosDisponibles(PeriodoAlquiler periodo) {
        return pianos.findByEstado(EstadoPiano.DISPONIBLE).stream()
                .filter(p -> !alquileres.existeSolapamiento(p.getIdPiano(), periodo))
                .toList();
    }

    public void borrarCliente(Integer idCliente) {
        if (!alquileres.findByClienteId(idCliente).isEmpty()) {
            throw new IllegalStateException("No se puede borrar: el cliente tiene alquileres");
        }
        clientes.deleteById(idCliente);
    }

    public void borrarPiano(Integer idPiano) {
        if (!alquileres.findByPianoId(idPiano).isEmpty()) {
            throw new IllegalStateException("No se puede borrar: el piano tiene alquileres");
        }
        pianos.deleteById(idPiano);
    }
}
