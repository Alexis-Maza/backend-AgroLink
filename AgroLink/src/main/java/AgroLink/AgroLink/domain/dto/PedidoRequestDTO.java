package AgroLink.AgroLink.domain.dto;

import lombok.Data;
import java.util.List;

@Data
public class PedidoRequestDTO {

    private List<ItemCarritoDTO> items;

    @Data
    public static class ItemCarritoDTO {
        private Long cultivoId;
        private Double cantidad;
        private Double precioPactado;
        private String direccionEntrega;
        private String metodoPago;
        private Integer porcentajeAdelanto;
    }
}