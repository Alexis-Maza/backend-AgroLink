package AgroLink.AgroLink.persistance.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * RF-A2-02: Entidad principal del control de cultivos.
 * Representa un lote o parcela sembrada por un agricultor.
 */
@Data
@Entity
@Table(name = "cultivo")
public class Cultivo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Fecha en que se inició la siembra
    @Column(name = "fecha_siembra", nullable = false)
    private LocalDate fechaSiembra;

    // Área sembrada del cultivo (en hectáreas)
    @Column(name = "area_sembrada", precision = 10, scale = 2)
    private BigDecimal areaSembrada;

    // Días totales estimados para el ciclo de cultivo
    @Column(name = "dias_totales_estimados", nullable = false)
    private Integer diasTotalesEstimados;

    // Indica si el lote está disponible para preventa en el catálogo de compradores
    @Column(name = "disponible", nullable = false)
    private Boolean disponible = false;

    // --- Relaciones ---

    // Agricultor propietario del cultivo (relación con Dev 1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_agricultor", nullable = false)
    private Agricultor agricultor;

    // Estado actual del cultivo
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_estado_cultivo", nullable = false)
    private Estado_Cultivo estadoCultivo;

    // Variedad de producto sembrado
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_producto_variedad", nullable = false)
    private Producto_Variedad productoVariedad;
}
