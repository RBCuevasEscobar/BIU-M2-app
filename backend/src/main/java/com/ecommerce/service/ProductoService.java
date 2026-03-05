package com.ecommerce.service;

import com.ecommerce.dto.ProductoDTO;
import com.ecommerce.dto.ProductoRequestDTO;
import com.ecommerce.model.Producto;
import com.ecommerce.model.ProductoDigital;
import com.ecommerce.model.ProductoFisico;
import com.ecommerce.model.ProductoImagen;
import com.ecommerce.model.Usuario;
import com.ecommerce.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductoService {

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private UsuarioService usuarioService;

    @SuppressWarnings("null")
    @Transactional
    public ProductoDTO guardarProducto(ProductoRequestDTO request) {
        Producto producto;
        if ("Fisico".equalsIgnoreCase(request.getTipo())) {
            ProductoFisico pf = new ProductoFisico();
            pf.setStock(request.getStock());
            pf.setPeso(request.getPeso());
            producto = pf;
        } else {
            ProductoDigital pd = new ProductoDigital();
            pd.setUrlDescarga(request.getUrlDescarga());
            producto = pd;
        }

        updateCommonFields(producto, request);
        return ProductoDTO.fromEntity(productoRepository.save(producto));
    }

    public List<ProductoDTO> listarProductos() {
        return productoRepository.findAll().stream()
                .map(ProductoDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @SuppressWarnings("null")
    public Optional<ProductoDTO> obtenerProducto(Long id) {
        return productoRepository.findById(id).map(ProductoDTO::fromEntity);
    }

    @SuppressWarnings("null")
    @Transactional
    public void eliminarProducto(Long id) {
        productoRepository.deleteById(id);
    }

    @SuppressWarnings("null")
    @Transactional
    public ProductoDTO actualizarProducto(Long id, ProductoRequestDTO request, String username) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con id: " + id));

        Usuario accionUsuario = usuarioService.buscarPorEmail(username)
                .orElseThrow(() -> new RuntimeException("Usuario accion no encontrado"));

        boolean isAdmin = accionUsuario.getRole().name().equals("ADMIN");
        boolean isSupplier = accionUsuario.getRole().name().equals("SUPPLIER");

        if (isSupplier) {
            // Un SUPPLIER solo puede modificar sus productos, y solo pueden tocar imágenes
            // u otros campos habilitados.
            // Para simplificar: el proveedor del producto debería coincidir con la
            // empresa/identificador del supplier.
            // Puesto que "proveedor" es un String asigando por el ADMIN a cada producto,
            // vamos a empatar el String "proveedor" vs el email / nombre del usuario
            // actual.
            // Si el supplier no es el dueño, se rechaza la petición.
            if (producto.getProveedor() == null || (!producto.getProveedor().equals(accionUsuario.getEmail())
                    && !producto.getProveedor().equals(((com.ecommerce.model.Proveedor) accionUsuario).getEmpresa()))) {
                throw new RuntimeException("Solo puedes editar productos que te pertenecen");
            }
            // Supplier can't alter proveedor name
        } else if (isAdmin) {
            // Admin can override proveedor
            producto.setProveedor(request.getProveedor());
        } else {
            throw new RuntimeException("Not Authorized");
        }

        producto.setNombre(request.getNombre());
        producto.setPrecio(request.getPrecio());
        producto.setDescripcion(request.getDescripcion());

        if (producto instanceof ProductoFisico pf && "Fisico".equalsIgnoreCase(request.getTipo())) {
            pf.setStock(request.getStock());
            pf.setPeso(request.getPeso());
        } else if (producto instanceof ProductoDigital pd && "Digital".equalsIgnoreCase(request.getTipo())) {
            pd.setUrlDescarga(request.getUrlDescarga());
        }

        // Handle Images
        if (request.getImagenesUrls() != null && !request.getImagenesUrls().isEmpty()) {
            producto.getImagenes().clear(); // For simplicity, replace all
            int defaultIndex = request.getDefaultImageIndex() != null ? request.getDefaultImageIndex() : 0;

            for (int i = 0; i < request.getImagenesUrls().size(); i++) {
                boolean isDefault = (i == defaultIndex);
                ProductoImagen img = new ProductoImagen(request.getImagenesUrls().get(i), isDefault, producto);
                producto.getImagenes().add(img);
            }
        }

        return ProductoDTO.fromEntity(productoRepository.save(producto));
    }

    private void updateCommonFields(Producto producto, ProductoRequestDTO request) {
        producto.setNombre(request.getNombre());
        producto.setPrecio(request.getPrecio());
        producto.setDescripcion(request.getDescripcion());
        producto.setProveedor(request.getProveedor());

        if (request.getImagenesUrls() != null && !request.getImagenesUrls().isEmpty()) {
            int defaultIndex = request.getDefaultImageIndex() != null ? request.getDefaultImageIndex() : 0;

            for (int i = 0; i < request.getImagenesUrls().size(); i++) {
                boolean isDefault = (i == defaultIndex);
                ProductoImagen img = new ProductoImagen(request.getImagenesUrls().get(i), isDefault, producto);
                producto.getImagenes().add(img);
            }
        }
    }
}
