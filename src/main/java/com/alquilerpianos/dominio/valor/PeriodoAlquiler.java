package com.alquilerpianos.dominio.valor;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/** Objeto valor: periodo de alquiler (ambos días incluidos). */
public record PeriodoAlquiler(LocalDate fechaInicio, LocalDate fechaFin) {

    public PeriodoAlquiler {
        Objects.requireNonNull(fechaInicio, "La fecha de inicio no puede ser nula");
        Objects.requireNonNull(fechaFin, "La fecha de fin no puede ser nula");
        if (fechaFin.isBefore(fechaInicio)) {
            throw new IllegalArgumentException("La fecha de fin no puede ser anterior a la de inicio");
        }
    }

    /** Número de días, contando el día de inicio y el de fin. */
    public long calcularDias() {
        return ChronoUnit.DAYS.between(fechaInicio, fechaFin) + 1;
    }

    public boolean solapaCon(PeriodoAlquiler otro) {
        return !fechaInicio.isAfter(otro.fechaFin) && !otro.fechaInicio.isAfter(fechaFin);
    }
}
