package com.alquilerpianos.repositorio.fichero;

import com.alquilerpianos.dominio.EstadoPiano;
import com.alquilerpianos.dominio.Piano;
import com.alquilerpianos.dominio.TipoPiano;
import com.alquilerpianos.dominio.valor.Dinero;
import com.alquilerpianos.repositorio.PianoRepositorio;
import com.alquilerpianos.repositorio.RepositorioException;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Persistencia en datos/pianos.txt. Una línea por piano:
 * id;marca;modelo;tipo;precioDia;moneda;estado
 */
public class PianoRepositorioFichero implements PianoRepositorio {

    private static final Path FICHERO = Path.of("datos", "pianos.txt");
    private static final Path FICHERO_ID = Path.of("datos", "pianos_id.txt");
    private static final String SEPARADOR = ";";

    // ===================== IRepositorio =====================

    @Override
    public long count() {
        return leerTodos().size();
    }

    @Override
    public void deleteById(Integer id) {
        if (id == null) throw new IllegalArgumentException("El id no puede ser nulo");
        List<Piano> todos = leerTodos();
        if (todos.removeIf(p -> p.getIdPiano().equals(id))) {
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
        for (Piano p : leerTodos()) {
            if (p.getIdPiano().equals(id)) return true;
        }
        return false;
    }

    @Override
    public Piano findById(Integer id) {
        if (id == null) throw new IllegalArgumentException("El id no puede ser nulo");
        for (Piano p : leerTodos()) {
            if (p.getIdPiano().equals(id)) return p;
        }
        return null;
    }

    @Override
    public Iterable<Piano> findAll() {
        return leerTodos();
    }

    @Override
    public <S extends Piano> S save(S entity) {
        if (entity == null) throw new IllegalArgumentException("La entidad no puede ser nula");
        comprobarTexto(entity.getMarca(), entity.getModelo());

        List<Piano> todos = leerTodos();
        if (entity.getIdPiano() != null) {
            for (int i = 0; i < todos.size(); i++) {
                if (todos.get(i).getIdPiano().equals(entity.getIdPiano())) {
                    todos.set(i, entity);
                    escribirTodos(todos);
                    return entity;
                }
            }
        }
        entity.setIdPiano(siguienteId(todos));
        todos.add(entity);
        escribirTodos(todos);
        return entity;
    }

    // ===================== IRepositorioExtend =====================

    @Override
    public Optional<Piano> findByIdOptional(Integer id) {
        return Optional.ofNullable(findById(id));
    }

    @Override
    public List<Piano> findAllToList() {
        return leerTodos();
    }

    // ===================== Métodos propios =====================

    @Override
    public List<Piano> findByEstado(EstadoPiano estado) {
        List<Piano> resultado = new ArrayList<>();
        for (Piano p : leerTodos()) {
            if (p.getEstado() == estado) resultado.add(p);
        }
        return resultado;
    }

    @Override
    public List<Piano> findByMarca(String marca) {
        List<Piano> resultado = new ArrayList<>();
        for (Piano p : leerTodos()) {
            if (p.getMarca().toLowerCase().contains(marca.toLowerCase())) resultado.add(p);
        }
        return resultado;
    }

    @Override
    public List<Piano> findByTipo(TipoPiano tipo) {
        List<Piano> resultado = new ArrayList<>();
        for (Piano p : leerTodos()) {
            if (p.getTipo() == tipo) resultado.add(p);
        }
        return resultado;
    }

    // ===================== Auxiliares (privados) =====================

    private List<Piano> leerTodos() {
        List<Piano> lista = new ArrayList<>();
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

    private void escribirTodos(List<Piano> pianos) {
        List<String> lineas = new ArrayList<>();
        for (Piano p : pianos) lineas.add(serializar(p));
        try {
            Files.createDirectories(FICHERO.getParent());
            Files.write(FICHERO, lineas, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RepositorioException("No se pudo escribir " + FICHERO, e);
        }
    }

    private String serializar(Piano p) {
        return String.join(SEPARADOR,
                String.valueOf(p.getIdPiano()),
                p.getMarca(),
                p.getModelo(),
                p.getTipo().name(),
                p.getPrecioDia().cantidad().toPlainString(),
                p.getPrecioDia().moneda(),
                p.getEstado().name());
    }

    private Piano deserializar(String linea) {
        String[] f = linea.split(SEPARADOR, -1);
        Dinero precio = new Dinero(new BigDecimal(f[4]), f[5]);
        return new Piano(Integer.valueOf(f[0]), f[1], f[2],
                TipoPiano.valueOf(f[3]), precio, EstadoPiano.valueOf(f[6]));
    }

    private void comprobarTexto(String... textos) {
        for (String t : textos) {
            if (t != null && (t.contains(SEPARADOR) || t.contains("\n") || t.contains("\r"))) {
                throw new IllegalArgumentException("Los datos no pueden contener ';' ni saltos de línea");
            }
        }
    }

    private int siguienteId(List<Piano> existentes) {
        try {
            int maximo = 0;
            for (Piano p : existentes) maximo = Math.max(maximo, p.getIdPiano());
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
