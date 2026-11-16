package ar.edu.utn.frc.dsi.bolsines.strategy;

import ar.edu.utn.frc.dsi.bolsines.entity.Bolsin;
import ar.edu.utn.frc.dsi.bolsines.entity.Empleado;
import ar.edu.utn.frc.dsi.bolsines.entity.Estado;
import ar.edu.utn.frc.dsi.bolsines.entity.Remito;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <<ConcreteStrategy>> Opción 3: existe documentación que no corresponde al destino.
 * Esa documentación queda Recibida y Rechazada (debe volver al origen); el resto se acepta.
 * El remito que contenía documentación rechazada queda Recibido y Aceptado Parcial.
 */
public class RecepcionNoCorrespondeDestino implements EstrategiaRecepcion {

    @Override
    public String getDescripcion() {
        return "Existe documentación que no corresponde al destino";
    }

    @Override
    public boolean requiereSeleccionDocumentacion() {
        return true;
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
        Estado remitoParcial = EstrategiaRecepcion.buscarEstado(estados,
                e -> e.esAmbitoRemito() && e.esRecibidoYAceptadoParcial());
        Estado docAceptada = EstrategiaRecepcion.buscarEstado(estados,
                e -> e.esAmbitoDocumentacion() && e.esRecibidoYAceptado());
        Estado docRechazada = EstrategiaRecepcion.buscarEstado(estados,
                e -> e.esAmbitoDocumentacion() && e.esRecibidoYRechazado());

        for (Remito remito : bolsin.getRemitos()) {
            Estado estadoRemito = remito.contieneAlgunaDocumentacion(numerosDocumentacion)
                    ? remitoParcial : remitoAceptado;
            remito.recibir(estadoRemito, docAceptada, numerosDocumentacion, docRechazada, null,
                    responsable, fechaHora);
        }
    }
}
