package com.alquilerpianos.dominio;

import com.alquilerpianos.dominio.valor.Dinero;
import java.util.Objects;

/** Raíz agregada: Piano (contiene el objeto valor Dinero como precio por día). */
public class Piano {
    private Integer idPiano;
    private String marca;
    private String modelo;
    private TipoPiano tipo;
    private Dinero precioDia;
    private EstadoPiano estado;

    /** Piano nuevo (estado DISPONIBLE). */
    public Piano(String marca, String modelo, TipoPiano tipo, Dinero precioDia) {
        this(null, marca, modelo, tipo, precioDia, EstadoPiano.DISPONIBLE);
    }

    /** Piano cargado de la BD. */
    public Piano(Integer idPiano, String marca, String modelo, TipoPiano tipo,
                 Dinero precioDia, EstadoPiano estado) {
        if (marca == null || marca.isBlank() || modelo == null || modelo.isBlank()) {
            throw new IllegalArgumentException("Marca y modelo son obligatorios");
        }
        this.idPiano = idPiano;
        this.marca = marca.trim();
        this.modelo = modelo.trim();
        this.tipo = Objects.requireNonNull(tipo);
        this.precioDia = Objects.requireNonNull(precioDia);
        this.estado = Objects.requireNonNull(estado);
    }

    /** ¿Está en servicio? (la disponibilidad por fechas la resuelve el repositorio/servicio). */
    public boolean estaEnServicio() {
        return estado == EstadoPiano.DISPONIBLE;
    }

    public void cambiarEstado(EstadoPiano nuevoEstado) {
        this.estado = Objects.requireNonNull(nuevoEstado);
    }

    public void cambiarPrecio(Dinero nuevoPrecio) {
        this.precioDia = Objects.requireNonNull(nuevoPrecio);
    }

    public Integer getIdPiano() { return idPiano; }
    public void setIdPiano(Integer idPiano) { this.idPiano = idPiano; }
    public String getMarca() { return marca; }
    public String getModelo() { return modelo; }
    public TipoPiano getTipo() { return tipo; }
    public Dinero getPrecioDia() { return precioDia; }
    public EstadoPiano getEstado() { return estado; }

    @Override
    public String toString() {
        return "Piano[" + idPiano + ", " + marca + " " + modelo + ", " + tipo
                + ", " + precioDia + "/día, " + estado + "]";
    }
}
