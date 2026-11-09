package com.alquilerpianos;

import com.alquilerpianos.dominio.*;
import com.alquilerpianos.dominio.valor.Dinero;
import com.alquilerpianos.dominio.valor.PeriodoAlquiler;
import com.alquilerpianos.repositorio.*;
import com.alquilerpianos.repositorio.fichero.*;
import com.alquilerpianos.servicio.AlquilerServicio;
import java.time.LocalDate;

/** Demostración de todos los métodos de los repositorios (útil para la defensa). */
public class Main {

    public static void main(String[] args) {
        ClienteRepositorio clientes = new ClienteRepositorioFichero();
        PianoRepositorio pianos = new PianoRepositorioFichero();
        AlquilerRepositorio alquileres = new AlquilerRepositorioFichero();
        AlquilerServicio servicio = new AlquilerServicio(clientes, pianos, alquileres);

        // 0. Limpiar
        alquileres.deleteAll();
        clientes.deleteAll();
        pianos.deleteAll();
        System.out.println("Tras limpiar -> clientes=" + clientes.count() + ", pianos=" + pianos.count()
                + ", alquileres=" + alquileres.count());

        // 1. save (insertar)
        Cliente ana = clientes.save(new Cliente("Ana", "García López", "ana@correo.com", "600111222"));
        Cliente luis = clientes.save(new Cliente("Luis", "Pérez Ruiz", "luis@correo.com", "600333444"));
        Piano yamaha = pianos.save(new Piano("Yamaha", "U1", TipoPiano.VERTICAL, Dinero.euros("25.00")));
        Piano steinway = pianos.save(new Piano("Steinway", "Model B", TipoPiano.DE_COLA, Dinero.euros("90.00")));
        Piano roland = pianos.save(new Piano("Roland", "FP-30X", TipoPiano.DIGITAL, Dinero.euros("12.50")));
        System.out.println("Insertados: " + ana + " | " + yamaha);

        // 2. Alquileres mediante el servicio (reglas entre agregados)
        PeriodoAlquiler semana = new PeriodoAlquiler(LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 7));
        Alquiler a1 = servicio.alquilarPiano(ana.getIdCliente(), yamaha.getIdPiano(), semana);
        System.out.println("Alquiler creado: " + a1);
        try {
            servicio.alquilarPiano(luis.getIdCliente(), yamaha.getIdPiano(),
                    new PeriodoAlquiler(LocalDate.of(2026, 12, 5), LocalDate.of(2026, 12, 10)));
        } catch (IllegalStateException e) {
            System.out.println("Rechazado correctamente: " + e.getMessage());
        }

        // 3. Consultas de IRepositorio
        System.out.println("count clientes=" + clientes.count() + ", pianos=" + pianos.count()
                + ", alquileres=" + alquileres.count());
        System.out.println("existsById(ana)=" + clientes.existsById(ana.getIdCliente())
                + ", existsById(9999)=" + clientes.existsById(9999));
        System.out.println("findById(piano)=" + pianos.findById(yamaha.getIdPiano()));
        System.out.println("findAll pianos:");
        for (Piano p : pianos.findAll()) System.out.println("  " + p);

        // 4. Consultas de IRepositorioExtend
        System.out.println("findByIdOptional(9999)=" + clientes.findByIdOptional(9999));
        System.out.println("findAllToList clientes=" + clientes.findAllToList());

        // 5. Actualizar = buscar + modificar + guardar
        Piano p = pianos.findById(roland.getIdPiano());
        p.cambiarEstado(EstadoPiano.MANTENIMIENTO);
        pianos.save(p);
        System.out.println("Actualizado: " + pianos.findById(p.getIdPiano()));

        // 6. Métodos propios
        System.out.println("findByEmail: " + clientes.findByEmail("luis@correo.com").orElse(null));
        System.out.println("findByApellidos('Garc'): " + clientes.findByApellidos("Garc"));
        System.out.println("findByEstado(MANTENIMIENTO): " + pianos.findByEstado(EstadoPiano.MANTENIMIENTO));
        System.out.println("findByMarca('yam'): " + pianos.findByMarca("yam"));
        System.out.println("findByTipo(DE_COLA): " + pianos.findByTipo(TipoPiano.DE_COLA));
        System.out.println("Disponibles 5-6 dic: " + servicio.pianosDisponibles(
                new PeriodoAlquiler(LocalDate.of(2026, 12, 5), LocalDate.of(2026, 12, 6))));
        System.out.println("Alquileres de Ana: " + alquileres.findByClienteId(ana.getIdCliente()));
        System.out.println("Alquileres del Yamaha: " + alquileres.findByPianoId(yamaha.getIdPiano()));

        // 7. Cancelar y borrar
        servicio.cancelarAlquiler(a1.getIdAlquiler());
        System.out.println("Tras cancelar: " + alquileres.findById(a1.getIdAlquiler()));
        try {
            servicio.borrarCliente(ana.getIdCliente());
        } catch (IllegalStateException e) {
            System.out.println("Rechazado correctamente: " + e.getMessage());
        }
        alquileres.deleteById(a1.getIdAlquiler());
        pianos.deleteById(steinway.getIdPiano());
        System.out.println("Tras borrar -> alquileres=" + alquileres.count() + ", pianos=" + pianos.count());
        System.out.println("Abre la carpeta datos/ para ver los ficheros generados.");


    }
}
