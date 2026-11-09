package com.alquilerpianos.repositorio.fichero;

import com.alquilerpianos.dominio.Alquiler;
import com.alquilerpianos.dominio.EstadoAlquiler;
import com.alquilerpianos.dominio.valor.Dinero;
import com.alquilerpianos.dominio.valor.PeriodoAlquiler;
import com.alquilerpianos.repositorio.AlquilerRepositorio;
import com.alquilerpianos.repositorio.RepositorioException;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Persistencia en datos/alquileres.txt. Una línea por alquiler:
 * id;fechaInicio;fechaFin;precioTotal;moneda;estado;idCliente;idPiano
 */
public class AlquilerRepositorioFichero implements AlquilerRepositorio {

    private static final Path FICHERO = Path.of("datos", "alquileres.txt");
    private static final Path FICHERO_ID = Path.of("datos", "alquileres_id.txt");
    private static final String SEPARADOR = ";";

    // ===================== IRepositorio =====================

    @Override
    public long count() {
        return leerTodos().size();
    }

    @Override
    public void deleteById(Integer id) {
        if (id == null) throw new IllegalArgumentException("El id no puede ser nulo");
        List<Alquiler> todos = leerTodos();
        if (todos.removeIf(a -> a.getIdAlquiler().equals(id))) {
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
        for (Alquiler a : leerTodos()) {
            if (a.getIdAlquiler().equals(id)) return true;
        }
        return false;
    }

    @Override
    public Alquiler findById(Integer id) {
        if (id == null) throw new IllegalArgumentException("El id no puede ser nulo");
        for (Alquiler a : leerTodos()) {
            if (a.getIdAlquiler().equals(id)) return a;
        }
        return null;
    }

    @Override
    public Iterable<Alquiler> findAll() {
        return leerTodos();
    }

    @Override
    public <S extends Alquiler> S save(S entity) {
        if (entity == null) throw new IllegalArgumentException("La entidad no puede ser nula");

        List<Alquiler> todos = leerTodos();
        if (entity.getIdAlquiler() != null) {
            for (int i = 0; i < todos.size(); i++) {
                if (todos.get(i).getIdAlquiler().equals(entity.getIdAlquiler())) {
                    todos.set(i, entity);
                    escribirTodos(todos);
                    return entity;
                }
            }
        }
        entity.setIdAlquiler(siguienteId(todos));
        todos.add(entity);
        escribirTodos(todos);
        return entity;
    }

    // ===================== IRepositorioExtend =====================

    @Override
    public Optional<Alquiler> findByIdOptional(Integer id) {
        return Optional.ofNullable(findById(id));
    }

    @Override
    public List<Alquiler> findAllToList() {
        return leerTodos();
    }

    // ===================== Métodos propios =====================

    @Override
    public List<Alquiler> findByClienteId(Integer idCliente) {
        if (idCliente == null) throw new IllegalArgumentException("El id no puede ser nulo");
        List<Alquiler> resultado = new ArrayList<>();
        for (Alquiler a : leerTodos()) {
            if (a.getIdCliente().equals(idCliente)) resultado.add(a);
        }
        return resultado;
    }

    @Override
    public List<Alquiler> findByPianoId(Integer idPiano) {
        if (idPiano == null) throw new IllegalArgumentException("El id no puede ser nulo");
        List<Alquiler> resultado = new ArrayList<>();
        for (Alquiler a : leerTodos()) {
            if (a.getIdPiano().equals(idPiano)) resultado.add(a);
        }
        return resultado;
    }

    @Override
    public boolean existeSolapamiento(Integer idPiano, PeriodoAlquiler periodo) {
        if (idPiano == null) throw new IllegalArgumentException("El id no puede ser nulo");
        for (Alquiler a : leerTodos()) {
            if (a.getIdPiano().equals(idPiano)
                    && a.getEstado() == EstadoAlquiler.ACTIVO
                    && a.getPeriodo().solapaCon(periodo)) {
                return true;
            }
        }
        return false;
    }

    // ===================== Auxiliares (privados) =====================

    private List<Alquiler> leerTodos() {
        List<Alquiler> lista = new ArrayList<>();
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

    private void escribirTodos(List<Alquiler> alquileres) {
        List<String> lineas = new ArrayList<>();
        for (Alquiler a : alquileres) lineas.add(serializar(a));
        try {
            Files.createDirectories(FICHERO.getParent());
            Files.write(FICHERO, lineas, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RepositorioException("No se pudo escribir " + FICHERO, e);
        }
    }

    private String serializar(Alquiler a) {
        return String.join(SEPARADOR,
                String.valueOf(a.getIdAlquiler()),
                a.getPeriodo().fechaInicio().toString(),
                a.getPeriodo().fechaFin().toString(),
                a.getPrecioTotal().cantidad().toPlainString(),
                a.getPrecioTotal().moneda(),
                a.getEstado().name(),
                String.valueOf(a.getIdCliente()),
                String.valueOf(a.getIdPiano()));
    }

    private Alquiler deserializar(String linea) {
        String[] f = linea.split(SEPARADOR, -1);
        PeriodoAlquiler periodo = new PeriodoAlquiler(LocalDate.parse(f[1]), LocalDate.parse(f[2]));
        Dinero total = new Dinero(new BigDecimal(f[3]), f[4]);
        return new Alquiler(Integer.valueOf(f[0]), periodo, EstadoAlquiler.valueOf(f[5]),
                Integer.valueOf(f[6]), Integer.valueOf(f[7]), total);
    }

    private int siguienteId(List<Alquiler> existentes) {
        try {
            int maximo = 0;
            for (Alquiler a : existentes) maximo = Math.max(maximo, a.getIdAlquiler());
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
