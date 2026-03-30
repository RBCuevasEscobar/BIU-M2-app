package com.ecommerce.config;

import com.ecommerce.model.*;
import com.ecommerce.repository.UsuarioRepository;
import com.ecommerce.repository.ProductoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

import org.springframework.context.annotation.Profile;

@Component
@Profile("!prod")
public class DataSeeder implements CommandLineRunner {

        private final UsuarioRepository usuarioRepository;
        private final ProductoRepository productoRepository;
        private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;

        public DataSeeder(UsuarioRepository usuarioRepository,
                        ProductoRepository productoRepository,
                        PasswordEncoder passwordEncoder,
            JdbcTemplate jdbcTemplate) {
                this.usuarioRepository = usuarioRepository;
                this.productoRepository = productoRepository;
                this.passwordEncoder = passwordEncoder;
        this.jdbcTemplate = jdbcTemplate;
        }

        @Override
    public void run(String... args) throws Exception {
        limpiarUsuariosHuerfanos();
        if (usuarioRepository.count() == 0) {
            crearUsuarios();
        }
        if (productoRepository.count() == 0) {
            crearProductos();
        }
    }

    private void limpiarUsuariosHuerfanos() {
        try {
            var orphans = jdbcTemplate.queryForList("SELECT id FROM usuarios WHERE id NOT IN (SELECT id FROM administradores) AND id NOT IN (SELECT id FROM proveedores) AND id NOT IN (SELECT id FROM clientes)");
            for (var o : orphans) {
                Object orphanId = o.get("id");
                
                // Limpiar hijos foraneos posibles de "usuarios" huerfanos para no romper constraint referencial
                jdbcTemplate.update("DELETE FROM ordenes WHERE usuario_id = ?", orphanId);
                jdbcTemplate.update("DELETE FROM carrito_productos WHERE carrito_id IN (SELECT id FROM carritos WHERE usuario_id = ?)", orphanId);
                jdbcTemplate.update("DELETE FROM carritos WHERE usuario_id = ?", orphanId);
                jdbcTemplate.update("DELETE FROM direcciones WHERE usuario_id = ?", orphanId);
                
                // Borrar finalmente el usuario
                jdbcTemplate.update("DELETE FROM usuarios WHERE id = ?", orphanId);
                System.out.println("Limpiado usuario huérfano ID: " + orphanId);
            }
        } catch (Exception e) {
            System.err.println("No se pudo limpiar usuarios huérfanos: " + e.getMessage());
        }
    }

    private void crearUsuarios() {
                // Admin
                Administrador admin = new Administrador();
                admin.setNombre("Admin User");
                admin.setEmail("admin@example.com");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setFechaNacimiento(LocalDate.of(1980, 1, 1));
                admin.setRole(Role.ADMIN);
                admin.setValidUntil(LocalDate.now().plusYears(1));
                usuarioRepository.save(admin);

                // Supplier
                Proveedor supplier = new Proveedor();
                supplier.setNombre("Supplier User");
                supplier.setEmail("supplier@example.com");
                supplier.setPassword(passwordEncoder.encode("supplier123"));
                supplier.setFechaNacimiento(LocalDate.of(1985, 5, 5));
                supplier.setRole(Role.SUPPLIER);
                supplier.setEmpresa("Supplier Company");
                usuarioRepository.save(supplier);

                // Customer
                Cliente customer = new Cliente();
                customer.setNombre("Customer User");
                customer.setEmail("customer@example.com");
                customer.setPassword(passwordEncoder.encode("customer123"));
                customer.setFechaNacimiento(LocalDate.of(1990, 10, 10));
                customer.setRfcCurp("CUVE900101ABC");
                customer.setRole(Role.CUSTOMER);
                usuarioRepository.save(customer);

                System.out.println("Usuarios de prueba creados.");
        }

        private void crearProductos() {
                // ── Producto Físico 1: Laptop Gamer ─────────────────────────────
                ProductoFisico p1 = new ProductoFisico();
                p1.setNombre("Laptop Gamer Pro");
                p1.setPrecio(1500.0);
                p1.setPeso(2.5);
                p1.setStock(10);
                p1.setDescripcion(
                                "Laptop gamer de alto rendimiento con procesador Intel Core i9, 32GB RAM, SSD 1TB y GPU RTX 4070.");
                p1.setProveedor("Supplier Company");
                // Imágenes asociadas
                p1.getImagenes().add(new ProductoImagen(
                                "https://images.unsplash.com/photo-1593642632559-0c6d3fc62b89?w=600", true, p1));
                p1.getImagenes().add(new ProductoImagen(
                                "https://images.unsplash.com/photo-1496181133206-80ce9b88a853?w=600", false, p1));
                p1.getImagenes().add(new ProductoImagen(
                                "https://images.unsplash.com/photo-1525547719571-a2d4ac8945e2?w=600", false, p1));
                productoRepository.save(p1);

                // ── Producto Físico 2: Mouse RGB ─────────────────────────────────
                ProductoFisico p2 = new ProductoFisico();
                p2.setNombre("Mouse RGB Ergonómico");
                p2.setPrecio(89.99);
                p2.setPeso(0.15);
                p2.setStock(50);
                p2.setDescripcion(
                                "Mouse inalámbrico ergonómico con iluminación RGB, 6 botones programables y batería de larga duración.");
                p2.setProveedor("Supplier Company");
                p2.getImagenes().add(new ProductoImagen(
                                "https://images.unsplash.com/photo-1527814050087-3793815479db?w=600", true, p2));
                p2.getImagenes().add(new ProductoImagen(
                                "https://images.unsplash.com/photo-1615663245857-ac93bb7c39e7?w=600", false, p2));
                productoRepository.save(p2);

                // ── Producto Físico 3: Teclado Mecánico ──────────────────────────
                ProductoFisico p3 = new ProductoFisico();
                p3.setNombre("Teclado Mecánico TKL");
                p3.setPrecio(149.99);
                p3.setPeso(0.9);
                p3.setStock(25);
                p3.setDescripcion(
                                "Teclado mecánico tenkeyless con switches Cherry MX Red, retroiluminación RGB por tecla y marco de aluminio.");
                p3.setProveedor("Supplier Company");
                p3.getImagenes().add(new ProductoImagen(
                                "https://images.unsplash.com/photo-1511467687858-23d96c32e4ae?w=600", true, p3));
                productoRepository.save(p3);

                // ── Producto Digital 1: E-Book Java ──────────────────────────────
                ProductoDigital p4 = new ProductoDigital();
                p4.setNombre("E-Book: Java Avanzado");
                p4.setPrecio(29.99);
                p4.setUrlDescarga("https://example.com/downloads/java-avanzado.pdf");
                p4.setDescripcion(
                                "Guía completa de Java avanzado: patrones de diseño, programación funcional, concurrencia y Spring Boot.");
                p4.setProveedor("Supplier Company");
                p4.getImagenes().add(new ProductoImagen(
                                "https://images.unsplash.com/photo-1532012197267-da84d127e765?w=600", true, p4));
                p4.getImagenes().add(new ProductoImagen(
                                "https://images.unsplash.com/photo-1481627834876-b7833e8f5570?w=600", false, p4));
                productoRepository.save(p4);

                // ── Producto Digital 2: Curso Spring Boot ────────────────────────
                ProductoDigital p5 = new ProductoDigital();
                p5.setNombre("Curso: Spring Boot REST API");
                p5.setPrecio(59.99);
                p5.setUrlDescarga("https://example.com/downloads/spring-boot-rest.zip");
                p5.setDescripcion(
                                "Curso completo en video sobre desarrollo de APIs RESTful con Spring Boot, seguridad JWT y despliegue en AWS.");
                p5.setProveedor("Supplier Company");
                p5.getImagenes().add(new ProductoImagen(
                                "https://images.unsplash.com/photo-1461749280684-dccba630e2f6?w=600", true, p5));
                productoRepository.save(p5);

                System.out.println("Productos de prueba con imágenes y descripciones creados.");
        }
}
