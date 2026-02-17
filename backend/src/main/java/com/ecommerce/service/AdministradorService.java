package com.ecommerce.service;

import com.ecommerce.model.Administrador;
import com.ecommerce.model.Usuario;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class AdministradorService {

    public static void validarAdminVigente(Usuario usuario) {
        if (!(usuario instanceof Administrador admin)) {
            throw new RuntimeException("No es administrador");
        }

        if (admin.getValidUntil().isBefore(LocalDate.now())) {
            throw new RuntimeException("Rol ADMIN expirado");
        }
    }
}