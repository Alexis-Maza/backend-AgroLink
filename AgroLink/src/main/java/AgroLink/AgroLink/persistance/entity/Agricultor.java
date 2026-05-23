package AgroLink.AgroLink.persistance.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Entity
@Table(name = "agricultores")
public class Agricultor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "dni_ruc", length = 20)
    private String dniRuc;

    @Column(length = 255)
    private String ubicacion;

    @Column(name = "hectareas_totales", precision = 10, scale = 2)
    private BigDecimal hectareasTotales;

    @Column(columnDefinition = "TEXT")
    private String certificaciones;

    @Column(name = "anos_experiencia")
    private Integer anosExperiencia;

    @OneToOne
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;
}
