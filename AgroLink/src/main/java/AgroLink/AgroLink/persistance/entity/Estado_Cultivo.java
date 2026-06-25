package AgroLink.AgroLink.persistance.entity;

import jakarta.persistence.*;
import lombok.Data;

/**
 * Catálogo de estados posibles de un cultivo.
 * Ejemplos: PLANIFICADO, EN_CURSO, COSECHADO, CANCELADO
 */
@Data
@Entity
@Table(name = "estado_cultivo")
public class Estado_Cultivo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Descripción del estado, ej: "EN_CURSO", "PLANIFICADO", "COSECHADO"
    @Column(name = "descripcion_estado_cultivo", length = 50, nullable = false, unique = true)
    private String descripcionEstadoCultivo;
}
