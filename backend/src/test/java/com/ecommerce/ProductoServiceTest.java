package com.ecommerce;

import com.ecommerce.dto.ProductoRequestDTO;
import com.ecommerce.dto.ProductoDTO;
import com.ecommerce.exception.AccesoDenegadoException;
import com.ecommerce.exception.ProductoNoEncontradoException;
import com.ecommerce.exception.UsuarioNoEncontradoException;
import com.ecommerce.factory.FabricaEntidades;
import com.ecommerce.model.*;
import com.ecommerce.repository.ProductoRepository;
import com.ecommerce.repository.UsuarioRepository;
import com.ecommerce.service.ProductoService;
import com.ecommerce.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias de ProductoService con Mockito.
 * Se mockean ProductoRepository, UsuarioRepository y FabricaEntidades.
 * No se levanta el contexto de Spring (@ExtendWith(MockitoExtension) es ligero).
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("ProductoService — Tests Unitarios")
class ProductoServiceTest {

    @Mock private ProductoRepository productoRepository;
    @Mock private UsuarioRepository  usuarioRepository;
    @Mock private FabricaEntidades   fabricaEntidades;
    @Mock private UsuarioService     usuarioService;

    @InjectMocks
    private ProductoService productoService;

    private ProductoFisico laptop;
    private Administrador adminUser;
    private Proveedor supplierUser;
    private ProductoRequestDTO requestFisico;

    @BeforeEach
    void setUp() {
        laptop = new ProductoFisico("Laptop Pro", 25000.0, 10, 2.5);
        laptop.setProveedor("TechCorp");

        adminUser = new Administrador();
        adminUser.setEmail("admin@test.com");
        adminUser.setRole(Role.ADMIN);

        supplierUser = new Proveedor();
        supplierUser.setEmail("proveedor@techcorp.com");
        supplierUser.setEmpresa("TechCorp");
        supplierUser.setRole(Role.SUPPLIER);

        requestFisico = new ProductoRequestDTO();
        requestFisico.setNombre("Laptop Pro V2");
        requestFisico.setPrecio(28000.0);
        requestFisico.setTipo("Fisico");
        requestFisico.setStock(8);
        requestFisico.setPeso(2.3);

        ReflectionTestUtils.setField(productoService, "usuarioService", usuarioService);
        ReflectionTestUtils.setField(productoService, "fabricaEntidades", fabricaEntidades);
    }

    // ── listarProductosPublicos ───────────────────────────────────────────────
    @Test
    @DisplayName("listarProductosPublicos: retorna todos los productos")
    void listarProductosPublicos_ok() {
        when(productoRepository.findAll()).thenReturn(List.of(laptop));
        List<ProductoDTO> resultado = productoService.listarProductosPublicos();
        assertEquals(1, resultado.size());
        verify(productoRepository).findAll();
    }

    // ── listarProductos ───────────────────────────────────────────────────────
    @Test
    @DisplayName("listarProductos: ADMIN ve todos")
    void listarProductos_admin() {
        when(usuarioRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(adminUser));
        when(productoRepository.findAll()).thenReturn(List.of(laptop));
        List<ProductoDTO> resultado = productoService.listarProductos("admin@test.com");
        assertEquals(1, resultado.size());
    }

    @Test
    @DisplayName("listarProductos: usuario no encontrado → UsuarioNoEncontradoException")
    void listarProductos_usuarioNoEncontrado() {
        when(usuarioRepository.findByEmail("nadie@x.com")).thenReturn(Optional.empty());
        assertThrows(UsuarioNoEncontradoException.class,
                () -> productoService.listarProductos("nadie@x.com"));
    }

    // ── obtenerProducto ───────────────────────────────────────────────────────
    @Test
    @DisplayName("obtenerProducto: retorna Optional con DTO cuando existe")
    void obtenerProducto_existe() {
        laptop = spy(laptop);
        when(productoRepository.findById(1L)).thenReturn(Optional.of(laptop));
        assertTrue(productoService.obtenerProducto(1L).isPresent());
    }

    @Test
    @DisplayName("obtenerProducto: retorna Optional.empty cuando no existe")
    void obtenerProducto_noExiste() {
        when(productoRepository.findById(99L)).thenReturn(Optional.empty());
        assertTrue(productoService.obtenerProducto(99L).isEmpty());
    }

    // ── actualizarProducto ────────────────────────────────────────────────────
    @Test
    @DisplayName("actualizarProducto: producto no encontrado → ProductoNoEncontradoException")
    void actualizarProducto_productoNoExiste() {
        when(productoRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ProductoNoEncontradoException.class,
                () -> productoService.actualizarProducto(99L, requestFisico, "admin@test.com"));
    }

    @Test
    @DisplayName("actualizarProducto: SUPPLIER intenta editar producto ajeno → AccesoDenegadoException")
    void actualizarProducto_supplierSinPermiso() {
        // Proveedor diferente al del producto
        Proveedor otroProveedor = new Proveedor();
        otroProveedor.setEmail("otro@empresa.com");
        otroProveedor.setEmpresa("OtraEmpresa");
        otroProveedor.setRole(Role.SUPPLIER);

        when(productoRepository.findById(1L)).thenReturn(Optional.of(laptop));
        when(usuarioService.buscarPorEmail("otro@empresa.com")).thenReturn(Optional.of(otroProveedor));

        assertThrows(AccesoDenegadoException.class,
                () -> productoService.actualizarProducto(1L, requestFisico, "otro@empresa.com"));
    }

    @Test
    @DisplayName("actualizarProducto: ADMIN actualiza cualquier producto")
    void actualizarProducto_adminExito() {
        ProductoFisico laptopActualizado = new ProductoFisico("Laptop Pro V2", 28000.0, 8, 2.3);
        when(productoRepository.findById(1L)).thenReturn(Optional.of(laptop));
        when(usuarioService.buscarPorEmail("admin@test.com")).thenReturn(Optional.of(adminUser));
        when(productoRepository.save(any())).thenReturn(laptopActualizado);

        ProductoDTO result = productoService.actualizarProducto(1L, requestFisico, "admin@test.com");
        assertNotNull(result);
        assertEquals("Laptop Pro V2", result.getNombre());
    }

    // ── eliminarProducto ──────────────────────────────────────────────────────
    @Test
    @DisplayName("eliminarProducto: llama a deleteById con el ID correcto")
    void eliminarProducto_ok() {
        doNothing().when(productoRepository).deleteById(1L);
        productoService.eliminarProducto(1L);
        verify(productoRepository).deleteById(1L);
    }
}
