package AgroLink.AgroLink.persistance.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "compradores")
public class Comprador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "dni_ruc", length = 20)
    private String dniRuc;

    @Column(name = "nombre_negocio", length = 150)
    private String nombreNegocio;

    @Column(length = 20)
    private String telefono;

    @Column(length = 255)
    private String ubicacion;

    @Column(length = 255)
    private String direccion;

    @Column(name = "punto_referencia", length = 255)
    private String puntoReferencia;

    @ManyToOne
    @JoinColumn(name = "id_tipo_comprador")
    private Tipo_Comprador tipoComprador;

    @OneToOne
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;
}