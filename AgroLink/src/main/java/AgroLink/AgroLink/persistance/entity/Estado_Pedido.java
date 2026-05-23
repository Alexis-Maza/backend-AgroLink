package AgroLink.AgroLink.persistance.entity;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "estado_pedido")

public class Estado_Pedido {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_estado_pedido")
    private Long idEstadoPedido;

    @Column(name = "descripcion_estado_pedido", length = 50)
    private String descripcionEstadoPedido;

    public Estado_Pedido() {}

    public Long getIdEstadoPedido() { return idEstadoPedido; }
    public void setIdEstadoPedido(Long idEstadoPedido) { this.idEstadoPedido = idEstadoPedido; }

    public String getDescripcionEstadoPedido() { return descripcionEstadoPedido; }
    public void setDescripcionEstadoPedido(String descripcionEstadoPedido) { this.descripcionEstadoPedido = descripcionEstadoPedido; }
}
