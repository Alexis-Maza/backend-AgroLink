package AgroLink.AgroLink.persistance.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pedido")
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pedido")
    private Long idPedido;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @ManyToOne
    @JoinColumn(name = "id_comprador", nullable = false)
    private Comprador comprador;

    @ManyToOne
    @JoinColumn(name = "id_estado_pedido", nullable = false)
    private Estado_Pedido estadoPedido;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetallePedido> detalles = new ArrayList<>();

    // Constructor vacío
    public Pedido() {}

    // Getters y Setters
    public Long getIdPedido() { return idPedido; }
    public void setIdPedido(Long idPedido) { this.idPedido = idPedido; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public Comprador getComprador() { return comprador; }
    public void setComprador(Comprador comprador) { this.comprador = comprador; }

    public Estado_Pedido getEstadoPedido() { return estadoPedido; }
    public void setEstadoPedido(Estado_Pedido estadoPedido) { this.estadoPedido = estadoPedido; }

    public List<DetallePedido> getDetalles() { return detalles; }
    public void setDetalles(List<DetallePedido> detalles) { this.detalles = detalles; }
}