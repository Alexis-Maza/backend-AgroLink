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
    @Column(name = "disponible", nullable = true)
    private Boolean disponible = true;

    @Column(name = "lote", length = 50)
    private String lote;

    @Column(name = "precio", precision = 10, scale = 2)
    private BigDecimal precio;

    @Column(name = "minimo_venta", precision = 10, scale = 2)
    private BigDecimal minimoVenta;

    @Column(name = "cantidad_estimada", precision = 10, scale = 2)
    private BigDecimal cantidadEstimada;

    @Column(name = "cantidad_disponible", precision = 10, scale = 2)
    private BigDecimal cantidadDisponible;

    @Column(name = "unidad", length = 20)
    private String unidad;

    @Column(name = "imagen_url", columnDefinition = "TEXT")
    private String imagenUrl;

    // Observaciones generales del cultivo (notas del agricultor)
    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

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
