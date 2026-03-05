package com.ecommerce.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "direcciones")
@Data
@NoArgsConstructor
public class Direccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Boolean preferida = false;

    @Column(length = 20)
    private String alias;

    @Column(nullable = false, length = 25)
    private String calle;

    @Column(nullable = false, length = 10)
    private String numeroExterior;

    @Column(length = 10)
    private String numeroInterior;

    @Column(length = 75)
    private String referencia;

    @Column(nullable = false, length = 40)
    private String colonia;

    @Column(nullable = false, length = 45)
    private String ciudad;

    @Column(nullable = false, length = 45)
    private String municipio;

    @Column(nullable = false, length = 25)
    private String estado;

    @Column(nullable = false, length = 5)
    @Pattern(regexp = "^\\d{5}$", message = "El código postal debe tener exactamente 5 dígitos")
    private String codigoPostal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ElementCollection
    @CollectionTable(name = "direccion_telefonos", joinColumns = @JoinColumn(name = "direccion_id"))
    @Column(name = "telefono")
    private List<@Pattern(regexp = "^\\d{10}$", message = "El teléfono debe tener exactamente 10 dígitos") String> telefonos = new ArrayList<>();

}
