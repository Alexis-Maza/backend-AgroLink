package AgroLink.AgroLink.persistance.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "detalle_pedido")
public class DetallePedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detalle_pedido")
    private Long id;

    @Column(name = "cantidad_solicitada", precision = 10, scale = 2)
    private BigDecimal cantidadSolicitada;

    @Column(name = "precio_pactado", precision = 10, scale = 2)
    private BigDecimal precioPactado;

    @Column(name = "cantidad_entrega", precision = 10, scale = 2)
    private BigDecimal cantidadEntrega;

    @Column(name = "direccion", length = 255)
    private String direccion;

    @Column(name = "metodo_pago", length = 50)
    private String metodoPago;

    @Column(name = "porcentaje_adelanto")
    private Integer porcentajeAdelanto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_pedido", nullable = false)
    private Pedido pedido;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cultivo", nullable = false)
    private Cultivo cultivo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_unidad_medida_producto", nullable = false)
    private UnidadMedidaProducto unidadMedidaProducto;
}