package com.ecommerce.dto;

import com.ecommerce.model.Producto;
import com.ecommerce.model.ProductoDigital;
import com.ecommerce.model.ProductoFisico;
import lombok.Data;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class ProductoDTO {
    private Long id;
    private String nombre;
    private Double precio;
    private String descripcion;
    private String proveedor;
    private String tipo;

    // Attributos Fisico
    private Integer stock;
    private Double peso;

    // Attributos Digital
    private String urlDescarga;

    private List<ProductoImagenDTO> imagenes;

    public static ProductoDTO fromEntity(Producto producto) {
        ProductoDTO dto = new ProductoDTO();
        dto.setId(producto.getId());
        dto.setNombre(producto.getNombre());
        dto.setPrecio(producto.getPrecio());
        dto.setDescripcion(producto.getDescripcion());
        dto.setProveedor(producto.getProveedor());

        if (producto instanceof ProductoFisico pf) {
            dto.setTipo("Fisico");
            dto.setStock(pf.getStock());
            dto.setPeso(pf.getPeso());
        } else if (producto instanceof ProductoDigital pd) {
            dto.setTipo("Digital");
            dto.setUrlDescarga(pd.getUrlDescarga());
        }

        if (producto.getImagenes() != null) {
            dto.setImagenes(producto.getImagenes().stream()
                    .map(img -> new ProductoImagenDTO(img.getId(), img.getImagenUrl(), img.getIsDefault()))
                    .collect(Collectors.toList()));
        }

        return dto;
    }
}
