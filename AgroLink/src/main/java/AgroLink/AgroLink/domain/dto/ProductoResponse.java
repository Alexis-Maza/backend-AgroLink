package AgroLink.AgroLink.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProductoResponse {
    private Long id;
    private String nombre;
}