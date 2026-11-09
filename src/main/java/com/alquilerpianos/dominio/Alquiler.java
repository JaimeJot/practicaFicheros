package com.alquilerpianos.dominio;

import com.alquilerpianos.dominio.valor.Dinero;
import com.alquilerpianos.dominio.valor.PeriodoAlquiler;
import java.util.Objects;

/**
 * Raíz agregada: Alquiler.
 * Referencia a Cliente y Piano solo por su identificador (regla DDD entre agregados).
 */
public class Alquiler {
    private Integer idAlquiler;
    private PeriodoAlquiler periodo;
    private EstadoAlquiler estado;
    private Integer idCliente;
    private Integer idPiano;
    private Dinero precioTotal;

    /** Crea un alquiler nuevo calculando el precio con el precio/día del piano en este momento. */
    public static Alquiler crear(Integer idCliente, Integer idPiano,
                                 PeriodoAlquiler periodo, Dinero precioDia) {
        Dinero total = precioDia.multiplicar(periodo.calcularDias());
        return new Alquiler(null, periodo, EstadoAlquiler.ACTIVO, idCliente, idPiano, total);
    }

    /** Alquiler cargado de la BD. */
    public Alquiler(Integer idAlquiler, PeriodoAlquiler periodo, EstadoAlquiler estado,
                    Integer idCliente, Integer idPiano, Dinero precioTotal) {
        this.idAlquiler = idAlquiler;
        this.periodo = Objects.requireNonNull(periodo);
        this.estado = Objects.requireNonNull(estado);
        this.idCliente = Objects.requireNonNull(idCliente);
        this.idPiano = Objects.requireNonNull(idPiano);
        this.precioTotal = Objects.requireNonNull(precioTotal);
    }

    public Dinero calcularPrecio(Dinero precioDia) {
        return precioDia.multiplicar(periodo.calcularDias());
    }

    public void cancelar() {
        if (estado != EstadoAlquiler.ACTIVO) {
            throw new IllegalStateException("Solo se puede cancelar un alquiler ACTIVO");
        }
        estado = EstadoAlquiler.CANCELADO;
    }

    public void finalizar() {
        if (estado != EstadoAlquiler.ACTIVO) {
            throw new IllegalStateException("Solo se puede finalizar un alquiler ACTIVO");
        }
        estado = EstadoAlquiler.FINALIZADO;
    }

    public Integer getIdAlquiler() { return idAlquiler; }
    public void setIdAlquiler(Integer idAlquiler) { this.idAlquiler = idAlquiler; }
    public PeriodoAlquiler getPeriodo() { return periodo; }
    public EstadoAlquiler getEstado() { return estado; }
    public Integer getIdCliente() { return idCliente; }
    public Integer getIdPiano() { return idPiano; }
    public Dinero getPrecioTotal() { return precioTotal; }

    @Override
    public String toString() {
        return "Alquiler[" + idAlquiler + ", cliente=" + idCliente + ", piano=" + idPiano
                + ", " + periodo.fechaInicio() + " -> " + periodo.fechaFin()
                + ", " + precioTotal + ", " + estado + "]";
    }
}
