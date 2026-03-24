package com.ecommerce.repository;

import com.ecommerce.model.NotificacionPendiente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificacionPendienteRepository extends JpaRepository<NotificacionPendiente, Long> {

    /** Retorna todas las notificaciones aún no leídas */
    List<NotificacionPendiente> findByLeidaFalseOrderByFechaAsc();
}
