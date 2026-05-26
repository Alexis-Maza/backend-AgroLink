package AgroLink.AgroLink.domain.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class MermaRequest {
    private BigDecimal cantidadPerdida;
    private String causa;
    private String observacion;
}