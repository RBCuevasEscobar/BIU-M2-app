package com.ecommerce.repository;

import com.ecommerce.model.Direccion;
import com.ecommerce.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DireccionRepository extends JpaRepository<Direccion, Long> {
    List<Direccion> findByUsuario(Usuario usuario);

    long countByUsuario(Usuario usuario);
}
