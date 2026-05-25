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
    public void setUnidadMedida(Long idUnidadMedida) { this.idUnidadMedida = idUnidadMedida; }

    public List<ItemCarritoDTO> getItems() { return items; }
    public void setItems(List<ItemCarritoDTO> items) { this.items = items; }

    // Clase interna estática ampliada para la transacción real
    public static class ItemCarritoDTO {
        private Long cultivoId;
        private Double cantidad;
        private Double precioPactado;
        private Integer porcentajeAdelanto;  
        private String metodoPago;          
        private String direccionEntrega;    
        private String fechaEntregaEstimada; 

        public ItemCarritoDTO() {}

        public Long getCultivoId() { return cultivoId; }
        public void setCultivoId(Long cultivoId) { this.cultivoId = cultivoId; }

        public Double getCantidad() { return cantidad; }
        public void setCantidad(Double cantidad) { this.cantidad = cantidad; }

        public Double getPrecioPactado() { return precioPactado; }
        public void setPrecioPactado(Double precioPactado) { this.precioPactado = precioPactado; }

        public Integer getPorcentajeAdelanto() { return porcentajeAdelanto; }
        public void setPorcentajeAdelanto(Integer porcentajeAdelanto) { this.porcentajeAdelanto = porcentajeAdelanto; }

        public String getMetodoPago() { return metodoPago; }
        public void setMetodoPago(String metodoPago) { this.metodoPago = metodoPago; }

        public String getDireccionEntrega() { return direccionEntrega; }
        public void setDireccionEntrega(String direccionEntrega) { this.direccionEntrega = direccionEntrega; }

        public String getFechaEntregaEstimada() { return fechaEntregaEstimada; }
        public void setFechaEntregaEstimada(String fechaEntregaEstimada) { this.fechaEntregaEstimada = fechaEntregaEstimada; }
    }
}