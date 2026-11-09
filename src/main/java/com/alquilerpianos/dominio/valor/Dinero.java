package com.alquilerpianos.dominio.valor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/** Objeto valor: inmutable, sin identidad, igualdad por contenido. */
public record Dinero(BigDecimal cantidad, String moneda) {

    public Dinero {
        Objects.requireNonNull(cantidad, "La cantidad no puede ser nula");
        Objects.requireNonNull(moneda, "La moneda no puede ser nula");
        if (cantidad.signum() < 0) {
            throw new IllegalArgumentException("La cantidad no puede ser negativa");
        }
        if (!moneda.matches("[A-Z]{3}")) {
            throw new IllegalArgumentException("La moneda debe ser un código ISO de 3 letras (EUR)");
        }
        cantidad = cantidad.setScale(2, RoundingMode.HALF_UP);
    }

    public static Dinero euros(String cantidad) {
        return new Dinero(new BigDecimal(cantidad), "EUR");
    }

    public Dinero multiplicar(long factor) {
        return new Dinero(cantidad.multiply(BigDecimal.valueOf(factor)), moneda);
    }

    public Dinero sumar(Dinero otro) {
        if (!moneda.equals(otro.moneda)) {
            throw new IllegalArgumentException("No se pueden sumar monedas distintas");
        }
        return new Dinero(cantidad.add(otro.cantidad), moneda);
    }

    @Override
    public String toString() {
        return cantidad + " " + moneda;
    }
}
