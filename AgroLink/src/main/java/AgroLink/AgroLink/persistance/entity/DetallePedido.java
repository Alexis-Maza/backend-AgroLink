package AgroLink.AgroLink.persistance.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "detalle_pedido")
public class DetallePedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detalle_pedido")
    private Long idDetallePedido;

    @Column(name = "cantidad_solicitada", precision = 10, scale = 2)
    private Double cantidadSolicitada;

    @Column(name = "precio_pactado", precision = 10, scale = 2)
    private Double precioPactado;

    @Column(name = "cantidad_entrega", precision = 10, scale = 2)
    private Double cantidadEntrega;

    @Column(name = "direccion", length = 255)
    private String direccion;

    @ManyToOne
    @JoinColumn(name = "id_pedido", nullable = false)
    private Pedido pedido;

    @ManyToOne
    @JoinColumn(name = "id_cultivo", nullable = false)
    private Cultivo cultivo;

    @ManyToOne
    @JoinColumn(name = "id_unidad_medida_producto", nullable = false)
    private UnidadMedidaProducto  unidadMedidaProducto;

    // Constructor vacío
    public DetallePedido() {
    }

    // Getters y Setters
    public Long getIdDetallePedido() {
        return idDetallePedido;
    }

    public void setIdDetallePedido(Long idDetallePedido) {
        this.idDetallePedido = idDetallePedido;
    }

    public Double getCantidadSolicitada() {
        return cantidadSolicitada;
    }

    public void setCantidadSolicitada(Double cantidadSolicitada) {
        this.cantidadSolicitada = cantidadSolicitada;
    }

    public Double getPrecioPactado() {
        return precioPactado;
    }

    public void setPrecioPactado(Double precioPactado) {
        this.precioPactado = precioPactado;
    }

    public Double getCantidadEntrega() {
        return cantidadEntrega;
    }

    public void setCantidadEntrega(Double cantidadEntrega) {
        this.cantidadEntrega = cantidadEntrega;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public Pedido getPedido() {
        return pedido;
    }

    public void setPedido(Pedido pedido) {
        this.pedido = pedido;
    }

    public Cultivo getCultivo() {
        return cultivo;
    }

    public void setCultivo(Cultivo cultivo) {
        this.cultivo = cultivo;
    }

    public UnidadMedidaProducto getUnidadMedidaProducto() {
        return unidadMedidaProducto;
    }

    public void setUnidadMedidaProducto(UnidadMedidaProducto unidadMedidaProducto) {
        this.unidadMedidaProducto = unidadMedidaProducto;
    }
}