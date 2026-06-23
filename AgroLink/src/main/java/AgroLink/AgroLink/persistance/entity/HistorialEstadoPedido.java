package AgroLink.AgroLink.persistance.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Entidad JPA que registra cada transición de estado de un Pedido.
 * Alimenta el Timeline del Frontend con la cronología completa del ciclo de vida del pedido.
 *
 * Tabla: historial_estado_pedido
 */
@Data
@Entity
@Table(name = "historial_estado_pedido")
public class HistorialEstadoPedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_historial")
    private Long idHistorial;

    /**
     * Pedido al que pertenece este registro de historial.
     * Relación unidireccional @ManyToOne: muchos registros de historial apuntan a un mismo Pedido.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_pedido", nullable = false)
    private Pedido pedido;

    /**
     * Estado anterior antes de la transición.
     * Puede ser NULL si se trata del primer estado asignado al pedido.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_estado_anterior", nullable = true)
    private Estado_Pedido estadoAnterior;

    /**
     * Estado nuevo al que transicionó el pedido.
     * No puede ser nulo: siempre debe haber un estado destino registrado.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_estado_nuevo", nullable = false)
    private Estado_Pedido estadoNuevo;

    /**
     * Texto descriptivo libre del proceso o razón del cambio de estado.
     * Ejemplo: "Pago confirmado por pasarela", "Agricultor inició despacho".
     */
    @Column(name = "etapa", columnDefinition = "TEXT")
    private String etapa;

    /**
     * Usuario que gatilló el cambio de estado (agricultor, comprador o sistema).
     * Relación unidireccional @ManyToOne hacia la entidad Usuario.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario_accion", nullable = false)
    private Usuario usuarioAccion;

    /**
     * Marca de tiempo exacta en que se registró la transición.
     * Se establece automáticamente antes de persistir el registro.
     */
    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro;

    /** Establece la fecha de registro automáticamente al momento de inserción. */
    @PrePersist
    public void prePersist() {
        this.fechaRegistro = LocalDateTime.now();
    }
}
