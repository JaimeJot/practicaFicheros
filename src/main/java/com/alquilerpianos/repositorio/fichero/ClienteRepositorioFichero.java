package com.alquilerpianos.repositorio.fichero;

import com.alquilerpianos.dominio.Cliente;
import com.alquilerpianos.repositorio.ClienteRepositorio;
import com.alquilerpianos.repositorio.RepositorioException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Persistencia en datos/clientes.txt. Una línea por cliente:
 * id;nombre;apellidos;email;telefono
 */
public class ClienteRepositorioFichero implements ClienteRepositorio {

    // Rutas RELATIVAS a la carpeta del proyecto
    private static final Path FICHERO = Path.of("datos", "clientes.txt");
    private static final Path FICHERO_ID = Path.of("datos", "clientes_id.txt"); // último id entregado
    private static final String SEPARADOR = ";";

    // ===================== IRepositorio =====================

    @Override
    public long count() {
        return leerTodos().size();
    }

    @Override
    public void deleteById(Integer id) {
        if (id == null) throw new IllegalArgumentException("El id no puede ser nulo");
        List<Cliente> todos = leerTodos();
        if (todos.removeIf(c -> c.getIdCliente().equals(id))) {
            escribirTodos(todos);
        }
    }

    @Override
    public void deleteAll() {
        escribirTodos(new ArrayList<>());
    }

    @Override
    public boolean existsById(Integer id) {
        if (id == null) throw new IllegalArgumentException("El id no puede ser nulo");
        for (Cliente c : leerTodos()) {
            if (c.getIdCliente().equals(id)) return true;
        }
        return false;
    }

    @Override
    public Cliente findById(Integer id) {
        if (id == null) throw new IllegalArgumentException("El id no puede ser nulo");
        for (Cliente c : leerTodos()) {
            if (c.getIdCliente().equals(id)) return c;
        }
        return null;
    }

    @Override
    public Iterable<Cliente> findAll() {
        return leerTodos();
    }

    /** Si el id ya existe se sustituye la línea (actualización); si no, se añade con id nuevo. */
    @Override
    public <S extends Cliente> S save(S entity) {
        if (entity == null) throw new IllegalArgumentException("La entidad no puede ser nula");
        comprobarTexto(entity.getNombre(), entity.getApellidos(), entity.getEmail(), entity.getTelefono());

        List<Cliente> todos = leerTodos();

        // El fichero no tiene restricción UNIQUE: el email único se comprueba aquí
        for (Cliente otro : todos) {
            if (otro.getEmail().equalsIgnoreCase(entity.getEmail())
                    && !otro.getIdCliente().equals(entity.getIdCliente())) {
                throw new IllegalArgumentException("Ya existe un cliente con el email " + entity.getEmail());
            }
        }

        if (entity.getIdCliente() != null) {
            for (int i = 0; i < todos.size(); i++) {
                if (todos.get(i).getIdCliente().equals(entity.getIdCliente())) {
                    todos.set(i, entity);
                    escribirTodos(todos);
                    return entity;
                }
            }
        }
        entity.setIdCliente(siguienteId(todos));
        todos.add(entity);
        escribirTodos(todos);
        return entity;
    }

    // ===================== IRepositorioExtend =====================

    @Override
    public Optional<Cliente> findByIdOptional(Integer id) {
        return Optional.ofNullable(findById(id));
    }

    @Override
    public List<Cliente> findAllToList() {
        return leerTodos();
    }

    // ===================== Métodos propios =====================

    @Override
    public Optional<Cliente> findByEmail(String email) {
        for (Cliente c : leerTodos()) {
            if (c.getEmail().equalsIgnoreCase(email)) return Optional.of(c);
        }
        return Optional.empty();
    }

    @Override
    public List<Cliente> findByApellidos(String apellidos) {
        List<Cliente> resultado = new ArrayList<>();
        for (Cliente c : leerTodos()) {
            if (c.getApellidos().toLowerCase().contains(apellidos.toLowerCase())) resultado.add(c);
        }
        return resultado;
    }

    // ===================== Auxiliares (privados) =====================

    private List<Cliente> leerTodos() {
        List<Cliente> lista = new ArrayList<>();
        if (!Files.exists(FICHERO)) return lista;
        try {
            for (String linea : Files.readAllLines(FICHERO, StandardCharsets.UTF_8)) {
                if (!linea.isBlank()) lista.add(deserializar(linea));
            }
        } catch (IOException e) {
            throw new RepositorioException("No se pudo leer " + FICHERO, e);
        }
        return lista;
    }

    private void escribirTodos(List<Cliente> clientes) {
        List<String> lineas = new ArrayList<>();
        for (Cliente c : clientes) lineas.add(serializar(c));
        try {
            Files.createDirectories(FICHERO.getParent());
            Files.write(FICHERO, lineas, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RepositorioException("No se pudo escribir " + FICHERO, e);
        }
    }

    /** Objeto -> línea de texto con los atributos separados por ';'. */
    private String serializar(Cliente c) {
        return String.join(SEPARADOR,
                String.valueOf(c.getIdCliente()),
                c.getNombre(),
                c.getApellidos(),
                c.getEmail(),
                c.getTelefono() == null ? "" : c.getTelefono());
    }

    /** Línea de texto -> objeto. El -1 de split conserva los campos vacíos finales. */
    private Cliente deserializar(String linea) {
        String[] f = linea.split(SEPARADOR, -1);
        String telefono = f[4].isEmpty() ? null : f[4];
        return new Cliente(Integer.valueOf(f[0]), f[1], f[2], f[3], telefono);
    }

    /** Un dato con ';' o salto de línea rompería el formato del fichero. */
    private void comprobarTexto(String... textos) {
        for (String t : textos) {
            if (t != null && (t.contains(SEPARADOR) || t.contains("\n") || t.contains("\r"))) {
                throw new IllegalArgumentException("Los datos no pueden contener ';' ni saltos de línea");
            }
        }
    }

    /** Autoincremental persistente: no reutiliza el id de un cliente borrado. */
    private int siguienteId(List<Cliente> existentes) {
        try {
            int maximo = 0;
            for (Cliente c : existentes) maximo = Math.max(maximo, c.getIdCliente());
            int ultimo = 0;
            if (Files.exists(FICHERO_ID)) {
                ultimo = Integer.parseInt(Files.readString(FICHERO_ID, StandardCharsets.UTF_8).trim());
            }
            int siguiente = Math.max(maximo, ultimo) + 1;
            Files.createDirectories(FICHERO_ID.getParent());
            Files.writeString(FICHERO_ID, String.valueOf(siguiente), StandardCharsets.UTF_8);
            return siguiente;
        } catch (IOException | NumberFormatException e) {
            throw new RepositorioException("No se pudo gestionar el id en " + FICHERO_ID, e);
        }
    }
}
