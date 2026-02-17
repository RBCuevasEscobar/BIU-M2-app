package com.ecommerce.security;

import com.ecommerce.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component("usuarioSecurity")
public class UsuarioSecurity {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @SuppressWarnings("null")
    public boolean isOwner(Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentEmail = authentication.getName();

        return usuarioRepository.findById(id)
                .map(user -> user.getEmail().equals(currentEmail))
                .orElse(false);
    }
}
