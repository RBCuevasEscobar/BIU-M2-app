package com.ecommerce.repository;

import com.ecommerce.model.SystemConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio JPA para la entidad SystemConfig.
 * Proporciona CRUD estándar sobre la tabla system_config.
 */
@Repository
public interface SystemConfigRepository extends JpaRepository<SystemConfig, String> {
}
