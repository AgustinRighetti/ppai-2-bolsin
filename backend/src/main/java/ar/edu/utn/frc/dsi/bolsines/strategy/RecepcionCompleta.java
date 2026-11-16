package ar.edu.utn.frc.dsi.bolsines.strategy;

import ar.edu.utn.frc.dsi.bolsines.entity.Bolsin;
import ar.edu.utn.frc.dsi.bolsines.entity.Empleado;
import ar.edu.utn.frc.dsi.bolsines.entity.Estado;
import ar.edu.utn.frc.dsi.bolsines.entity.Remito;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <<ConcreteStrategy>> Opción 1: el contenido del bolsín es igual al registrado.
 * Todos los remitos quedan Recibido y Aceptado y toda la documentación Recibida y Aceptada.
 */
public class RecepcionCompleta implements EstrategiaRecepcion {

    @Override
    public String getDescripcion() {
        return "El contenido del bolsín es igual al registrado";
    }

    @Override
    public boolean requiereSeleccionDocumentacion() {
        return false;
    }

    @Override
    public boolean requiereAreaDestino() {
        return false;
    }

    @Override
    public boolean esAplicable(Bolsin bolsin) {
        return true;
    }

    @Override
    public void registrarRecepcion(Bolsin bolsin, List<Integer> numerosDocumentacion, String areaDestino,
                                   List<Estado> estados, Empleado responsable, LocalDateTime fechaHora) {
        Estado remitoAceptado = EstrategiaRecepcion.buscarEstado(estados,
                e -> e.esAmbitoRemito() && e.esRecibidoYAceptado());
        Estado docAceptada = EstrategiaRecepcion.buscarEstado(estados,
                e -> e.esAmbitoDocumentacion() && e.esRecibidoYAceptado());

        for (Remito remito : bolsin.getRemitos()) {
            remito.recibir(remitoAceptado, docAceptada, List.of(), docAceptada, null, responsable, fechaHora);
        }
    }
}
