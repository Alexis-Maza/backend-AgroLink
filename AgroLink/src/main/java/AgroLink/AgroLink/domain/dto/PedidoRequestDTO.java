package AgroLink.AgroLink.domain.dto;

import java.util.List;

public class PedidoRequestDTO {
    
    private Long compradorId;
    private Long idUnidadMedida;
    private List<ItemCarritoDTO> items;

    public PedidoRequestDTO() {}

    public Long getCompradorId() { return compradorId; }
    public void setCompradorId(Long compradorId) { this.compradorId = compradorId; }

    public Long getIdUnidadMedida() { return idUnidadMedida; }
    public void setIdUnidadMedida(Long idUnidadMedida) { this.idUnidadMedida = idUnidadMedida; }

    public List<ItemCarritoDTO> getItems() { return items; }
    public void setItems(List<ItemCarritoDTO> items) { this.items = items; }

    // Clase interna estática corregida
    public static class ItemCarritoDTO {
        private Long cultivoId;
        private Double cantidad;
        private Double precioPactado;

        public ItemCarritoDTO() {}

        public Long getCultivoId() { return cultivoId; }
        public void setCultivoId(Long cultivoId) { this.cultivoId = cultivoId; }

        public Double getCantidad() { return cantidad; }
        public void setCantidad(Double cantidad) { this.cantidad = cantidad; }

        public Double getPrecioPactado() { return precioPactado; }
        public void setPrecioPactado(Double precioPactado) { this.precioPactado = precioPactado; }
    }
}