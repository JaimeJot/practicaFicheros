package com.alquilerpianos.repositorio;

/** Envuelve las SQLException (que son checked) para no contaminar las interfaces. */
public class RepositorioException extends RuntimeException {
    public RepositorioException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
