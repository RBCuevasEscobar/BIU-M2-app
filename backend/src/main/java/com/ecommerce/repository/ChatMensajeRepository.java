package com.ecommerce.repository;

import com.ecommerce.model.ChatMensaje;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMensajeRepository extends JpaRepository<ChatMensaje, Long> {

    List<ChatMensaje> findByUsuarioIdOrderByFechaEnvioAsc(Long usuarioId);

    void deleteByUsuarioId(Long usuarioId);
}
