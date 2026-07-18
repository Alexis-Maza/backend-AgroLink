package AgroLink.AgroLink.domain.dto;

import java.time.LocalDateTime;

public record NotificacionDTO(
        String id,
        String tipo,
        String mensaje,
        LocalDateTime fecha,
        Long entidadId
) {}