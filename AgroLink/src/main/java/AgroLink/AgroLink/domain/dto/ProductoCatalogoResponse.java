package AgroLink.AgroLink.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class ProductoCatalogoResponse {
    private Long idProducto;
    private String nombreProducto;
    private List<VariedadConCultivosResponse> variedades;

    @Data
    @AllArgsConstructor
    public static class VariedadConCultivosResponse {
        private Long idVariedad;
        private String nombreVariedad;
        private List<CultivoResumenResponse> cultivos;
    }

    @Data
    @AllArgsConstructor
    public static class CultivoResumenResponse {
        private Long id;
        private String lote;
        private String estadoCultivo;
        private String fechaSiembra;
        private String fechaCosechaEstimada;
        private Double cantidadEstimada;
        private Double cantidadDisponible;
        private String unidad;
        private Double precio;
        private Double areaSembrada;
    }
}