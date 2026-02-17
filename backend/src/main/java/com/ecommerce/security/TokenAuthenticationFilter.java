package com.ecommerce.security;

import com.ecommerce.model.Usuario;
import com.ecommerce.repository.UsuarioRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class TokenAuthenticationFilter extends OncePerRequestFilter {

    private final UsuarioRepository usuarioRepository;

    public TokenAuthenticationFilter(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    protected void doFilterInternal(
            @org.springframework.lang.NonNull HttpServletRequest request,
            @org.springframework.lang.NonNull HttpServletResponse response,
            @org.springframework.lang.NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            // Mock token validation: extract user ID from token
            // Format: "mock-jwt-token-{userId}"
            if (token.startsWith("mock-jwt-token-")) {
                try {
                    Long userId = Long.parseLong(token.substring(15));
                    Usuario usuario = usuarioRepository.findById(userId).orElse(null);

                    if (usuario != null) {
                        // Create authentication token with role
                        SimpleGrantedAuthority authority = new SimpleGrantedAuthority(
                                "ROLE_" + usuario.getRole().name());
                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                usuario.getEmail(),
                                null,
                                Collections.singletonList(authority));

                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                } catch (NumberFormatException e) {
                    // Invalid token format, continue without authentication
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
