package ar.edu.utn.frc.dsi.bolsines.strategy;

import ar.edu.utn.frc.dsi.bolsines.entity.Bolsin;
import ar.edu.utn.frc.dsi.bolsines.entity.Empleado;
import ar.edu.utn.frc.dsi.bolsines.entity.Estado;
import ar.edu.utn.frc.dsi.bolsines.entity.Remito;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <<ConcreteStrategy>> Opción 2: no se recibe toda la documentación asociada a los remitos.
 * La documentación faltante queda No Recibida y el remito que la contenía queda
 * Recibido y Aceptado Parcial. El resto se acepta.
 */
public class RecepcionParcial implements EstrategiaRecepcion {

    @Override
    public String getDescripcion() {
        return "No se recibe toda la documentación asociada a los remitos que contiene el bolsín";
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
        Estado docNoRecibida = EstrategiaRecepcion.buscarEstado(estados,
                e -> e.esAmbitoDocumentacion() && e.esNoRecibida());

        for (Remito remito : bolsin.getRemitos()) {
            Estado estadoRemito = remito.contieneAlgunaDocumentacion(numerosDocumentacion)
                    ? remitoParcial : remitoAceptado;
            remito.recibir(estadoRemito, docAceptada, numerosDocumentacion, docNoRecibida, null,
                    responsable, fechaHora);
        }
    }
}
