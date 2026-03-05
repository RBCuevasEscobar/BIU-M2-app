package com.ecommerce.repository;

import com.ecommerce.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    // NUEVO MÉTODO
    List<Producto> findByProveedor(String proveedor);

}
