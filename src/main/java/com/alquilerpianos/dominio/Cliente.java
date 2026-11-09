package com.alquilerpianos.dominio;

/** Raíz agregada: Cliente. */
public class Cliente {
    private Integer idCliente;
    private String nombre;
    private String apellidos;
    private String email;
    private String telefono;

    /** Cliente nuevo (aún sin id: lo genera la BD al guardar). */
    public Cliente(String nombre, String apellidos, String email, String telefono) {
        this(null, nombre, apellidos, email, telefono);
    }

    /** Cliente ya existente (cargado de la BD). */
    public Cliente(Integer idCliente, String nombre, String apellidos, String email, String telefono) {
        this.idCliente = idCliente;
        asignar(nombre, apellidos, email, telefono);
    }

    public void modificarDatos(String nombre, String apellidos, String email, String telefono) {
        asignar(nombre, apellidos, email, telefono);
    }

    private void asignar(String nombre, String apellidos, String email, String telefono) {
        this.nombre = requerido(nombre, "nombre");
        this.apellidos = requerido(apellidos, "apellidos");
        this.email = requerido(email, "email");
        if (!this.email.contains("@")) {
            throw new IllegalArgumentException("El email no es válido");
        }
        this.telefono = telefono;
    }

    private static String requerido(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("El campo " + campo + " es obligatorio");
        }
        return valor.trim();
    }

    public Integer getIdCliente() { return idCliente; }
    public void setIdCliente(Integer idCliente) { this.idCliente = idCliente; }
    public String getNombre() { return nombre; }
    public String getApellidos() { return apellidos; }
    public String getEmail() { return email; }
    public String getTelefono() { return telefono; }

    @Override
    public String toString() {
        return "Cliente[" + idCliente + ", " + nombre + " " + apellidos + ", " + email + "]";
    }
}
