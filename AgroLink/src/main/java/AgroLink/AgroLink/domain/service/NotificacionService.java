package AgroLink.AgroLink.domain.service;

import AgroLink.AgroLink.domain.dto.NotificacionDTO;
import AgroLink.AgroLink.domain.repository.*;
import AgroLink.AgroLink.persistance.entity.Agricultor;
import AgroLink.AgroLink.persistance.entity.Comprador;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificacionService {

    private final HistorialEstadoPedidoRepository historialRepository;
    private final HistorialCultivoRepository historialCultivoRepository; // <-- NUEVO, faltaba
    private final CompradorRepository compradorRepository;
    private final AgricultorRepository agricultorRepository;
    // ya no necesitas cultivoRepository aquí si no lo usas en ningún otro lado de esta clase

    public List<NotificacionDTO> obtenerParaComprador(String email) {
        Comprador comprador = compradorRepository.findByUsuarioEmail(email)
                .orElseThrow(() -> new RuntimeException("Comprador no encontrado"));

        return historialRepository.findByPedido_Comprador_IdOrderByFechaRegistroDesc(comprador.getId())
                .stream()
                .filter(h -> h.getEstadoAnterior() != null)
                .map(h -> new NotificacionDTO(
                        "historial-" + h.getIdHistorial(),
                        "PEDIDO_ESTADO",
                        "Tu pedido #" + h.getPedido().getId() + " cambió a: " + h.getEstadoNuevo().getDescripcionEstadoPedido(),
                        h.getFechaRegistro(),
                        h.getPedido().getId()
                ))
                .toList();
    }

    public List<NotificacionDTO> obtenerParaAgricultor(String email) {
        Agricultor agricultor = agricultorRepository.findByUsuarioEmail(email)
                .orElseThrow(() -> new RuntimeException("Agricultor no encontrado"));

        List<NotificacionDTO> resultado = new ArrayList<>();

        historialRepository.findDistinctByPedido_Detalles_Cultivo_Agricultor_IdAndEstadoAnteriorIsNullOrderByFechaRegistroDesc(agricultor.getId())
                .forEach(h -> resultado.add(new NotificacionDTO(
                        "historial-" + h.getIdHistorial(),
                        "PEDIDO_RECIBIDO",
                        "Recibiste un nuevo pedido #" + h.getPedido().getId(),
                        h.getFechaRegistro(),
                        h.getPedido().getId()
                )));

        // --- REEMPLAZADO: ya no se calcula "atrasado" por fecha, se lee del historial real ---
        historialCultivoRepository
                .findByCultivo_Agricultor_IdAndEstadoCultivo_DescripcionEstadoCultivoOrderByFechaInicioDesc(
                        agricultor.getId(), "Listo para cosechar")
                .forEach(hc -> resultado.add(new NotificacionDTO(
                        "historial-cultivo-" + hc.getId(),
                        "CULTIVO_LISTO",
                        "El cultivo del lote '" + hc.getCultivo().getLote() + "' superó su fecha estimada y ya está listo para cosechar",
                        hc.getFechaInicio().atStartOfDay(),
                        hc.getCultivo().getId()
                )));

        resultado.sort(Comparator.comparing(NotificacionDTO::fecha).reversed());
        return resultado;
    }
}