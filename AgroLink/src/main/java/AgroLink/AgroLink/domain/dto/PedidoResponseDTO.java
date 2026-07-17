package AgroLink.AgroLink.domain.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class PedidoResponseDTO {
    private Long id;
    private LocalDateTime fechaCreacion;
    private String estadoPedido;
    private List<DetalleResponseDTO> detalles;

    @Data
    @AllArgsConstructor
    public static class DetalleResponseDTO {
        private Long idCultivo;
        private String nombreProducto;
        private String nombreProductoVariedad;
        private BigDecimal cantidadSolicitada;
        private BigDecimal precioPactado;
        private String direccion;
    }
}
