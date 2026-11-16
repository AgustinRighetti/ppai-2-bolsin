package ar.edu.utn.frc.dsi.bolsines.strategy;

import ar.edu.utn.frc.dsi.bolsines.entity.Bolsin;
import ar.edu.utn.frc.dsi.bolsines.entity.Empleado;
import ar.edu.utn.frc.dsi.bolsines.entity.Estado;
import ar.edu.utn.frc.dsi.bolsines.entity.Remito;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <<ConcreteStrategy>> Opción 4: la documentación se debe redirigir a otra área.
 * Solo aplica cuando la CM destino es la Comisión Médica Central. La documentación indicada
 * queda Para Redirigir (se guarda el área destino en el detalle del remito); el resto se acepta.
 */
public class RecepcionParaRedirigir implements EstrategiaRecepcion {

    @Override
    public String getDescripcion() {
        return "La documentación se debe redirigir a otra área";
    }

    @Override
    public boolean requiereSeleccionDocumentacion() {
        return true;
    }

    @Override
    public boolean requiereAreaDestino() {
        return true;
    }

    @Override
    public boolean esAplicable(Bolsin bolsin) {
        return bolsin.tuDestinoEsCentral();
    }

    @Override
    public void registrarRecepcion(Bolsin bolsin, List<Integer> numerosDocumentacion, String areaDestino,
                                   List<Estado> estados, Empleado responsable, LocalDateTime fechaHora) {
        Estado remitoAceptado = EstrategiaRecepcion.buscarEstado(estados,
                e -> e.esAmbitoRemito() && e.esRecibidoYAceptado());
        Estado docAceptada = EstrategiaRecepcion.buscarEstado(estados,
                e -> e.esAmbitoDocumentacion() && e.esRecibidoYAceptado());
        Estado docParaRedirigir = EstrategiaRecepcion.buscarEstado(estados,
                e -> e.esAmbitoDocumentacion() && e.esParaRedirigir());

        for (Remito remito : bolsin.getRemitos()) {
            remito.recibir(remitoAceptado, docAceptada, numerosDocumentacion, docParaRedirigir, areaDestino,
                    responsable, fechaHora);
        }
    }
}
