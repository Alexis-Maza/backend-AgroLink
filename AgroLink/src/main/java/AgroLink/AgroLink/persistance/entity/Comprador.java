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


        public Long getId() {
            return this.id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getDniRuc() {
            return this.dniRuc;
        }

        public void setDniRuc(String dniRuc) {
            this.dniRuc = dniRuc;
        }

        public String getNombreNegocio() {
            return this.nombreNegocio;
        }

        public void setNombreNegocio(String nombreNegocio) {
            this.nombreNegocio = nombreNegocio;
        }

        public String getTelefono() {
            return this.telefono;
        }

        public void setTelefono(String telefono) {
            this.telefono = telefono;
        }

        public String getUbicacion() {
            return this.ubicacion;
        }

        public void setUbicacion(String ubicacion) {
            this.ubicacion = ubicacion;
        }

        public String getDireccion() {
            return this.direccion;
        }

        public void setDireccion(String direccion) {
            this.direccion = direccion;
        }

        public String getPuntoReferencia() {
            return this.puntoReferencia;
        }

        public void setPuntoReferencia(String puntoReferencia) {
            this.puntoReferencia = puntoReferencia;
        }

        public Tipo_Comprador getTipoComprador() {
            return this.tipoComprador;
        }

        public void setTipoComprador(Tipo_Comprador tipoComprador) {
            this.tipoComprador = tipoComprador;
        }

        public Usuario getUsuario() {
            return this.usuario;
        }

        public void setUsuario(Usuario usuario) {
            this.usuario = usuario;
        }

    }