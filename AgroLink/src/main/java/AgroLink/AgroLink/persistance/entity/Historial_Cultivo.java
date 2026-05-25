package AgroLink.AgroLink.persistance.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

/**
 * RF-A2-02 / RF-A2-05: Registro cronológico de las fases (etapas)
 * por las que va pasando un cultivo. Cada registro representa
 * el inicio de una nueva etapa en el historial del lote.
 */
@Data
@Entity
@Table(name = "historial_cultivo")
public class Historial_Cultivo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Fecha en que se inició esta etapa
    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    // Fecha en que finalizó esta etapa (null si todavía está activa)
    @Column(name = "fecha_fin")
    private LocalDate fechaFin;


    // --- Relaciones ---

    // Cultivo al que pertenece este registro del historial
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cultivo", nullable = false)
    private Cultivo cultivo;

    // Estado del cultivo en esta etapa
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_estado_cultivo", nullable = false)
    private Estado_Cultivo estadoCultivo;

    // Etapa estándar de referencia para calcular retrasos (RF-A2-05)
    // FK corregida a id_etapa_productos_variedad (plural)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_etapa_productos_variedad")
    private Etapa_Producto_Variedad etapaProductoVariedad;
}
