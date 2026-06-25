package AgroLink.AgroLink.persistance.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "tipos_comprador")
public class Tipo_Comprador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre_tipo_comprador", length = 50)
    private String nombreTipoComprador;

    @Column(name = "descripcion_tipo_comprador", length = 255)
    private String descripcionTipoComprador;
}
