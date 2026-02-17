package com.ecommerce.config;

import com.ecommerce.model.*;
import com.ecommerce.repository.UsuarioRepository;
import com.ecommerce.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final ProductoRepository productoRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public DataSeeder(UsuarioRepository usuarioRepository,
            ProductoRepository productoRepository,
            PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.productoRepository = productoRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        if (usuarioRepository.count() == 0) {
            crearUsuarios();
        }
        if (productoRepository.count() == 0) {
            crearProductos();
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
        customer.setRole(Role.CUSTOMER);
        usuarioRepository.save(customer);

        System.out.println("Usuarios de prueba creados.");
    }

    private void crearProductos() {
        ProductoFisico p1 = new ProductoFisico();
        p1.setNombre("Laptop Gamer");
        p1.setPrecio(1500.0);
        p1.setPeso(2.5);
        p1.setStock(10);
        productoRepository.save(p1);

        ProductoDigital p2 = new ProductoDigital();
        p2.setNombre("E-Book Java");
        p2.setPrecio(29.99);
        p2.setUrlDescarga("http://example.com/ebook.pdf");
        productoRepository.save(p2);

        System.out.println("Productos de prueba creados.");
    }
}
