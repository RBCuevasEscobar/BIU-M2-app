package com.ecommerce.dto;

import com.ecommerce.model.Direccion;
import lombok.Data;

import java.util.List;

@Data
public class DireccionDTO {
    private Long id;
    private Boolean preferida;
    private String alias;
    private String calle;
    private String numeroExterior;
    private String numeroInterior;
    private String referencia;
    private String colonia;
    private String ciudad;
    private String municipio;
    private String estado;
    private String codigoPostal;
    private List<String> telefonos;

    public static DireccionDTO fromEntity(Direccion direccion) {
        DireccionDTO dto = new DireccionDTO();
        dto.setId(direccion.getId());
        dto.setPreferida(direccion.getPreferida());
        dto.setAlias(direccion.getAlias());
        dto.setCalle(direccion.getCalle());
        dto.setNumeroExterior(direccion.getNumeroExterior());
        dto.setNumeroInterior(direccion.getNumeroInterior());
        dto.setReferencia(direccion.getReferencia());
        dto.setColonia(direccion.getColonia());
        dto.setCiudad(direccion.getCiudad());
        dto.setMunicipio(direccion.getMunicipio());
        dto.setEstado(direccion.getEstado());
        dto.setCodigoPostal(direccion.getCodigoPostal());
        dto.setTelefonos(direccion.getTelefonos());
        return dto;
    }
}
