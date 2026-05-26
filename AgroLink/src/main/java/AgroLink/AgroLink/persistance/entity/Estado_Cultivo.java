package AgroLink.AgroLink.persistance.entity;

import jakarta.persistence.*;
import lombok.Data;

/**
 * Catálogo de estados posibles de un cultivo.
 * Ejemplos: Recién cultivado, En crecimiento, Listo para cosechar
 */
@Data
@Entity
@Table(name = "estado_cultivo")
public class Estado_Cultivo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_estado_cultivo")
    private Long id;

    @Column(name = "descripcion_estado_cultivo", length = 50, nullable = false, unique = true)
    private String descripcionEstadoCultivo;
}