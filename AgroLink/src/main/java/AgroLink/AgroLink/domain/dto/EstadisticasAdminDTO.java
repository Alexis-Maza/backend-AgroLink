package AgroLink.AgroLink.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EstadisticasAdminDTO {
    private long total;
    private long registradosHoy;
}