package AgroLink.AgroLink.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class VentaAgricultorDTO {
    private Long id;
    private String producto;
    private String comprador;
    private String empresa;
    private BigDecimal cantidad;
    private String unidad;
    private String costo;
    private String fechaCompra;
    private String lote;
    private String estado;
    private String direccionEntrega;
}