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

/**
 * Filtro JWT: valida el token y establece un objeto UsuarioSecurity como
 * principal del SecurityContext para que cualquier controller pueda acceder
 * al userId, email y rol del usuario autenticado sin tocar la BD.
 */
@Component
public class TokenAuthenticationFilter extends OncePerRequestFilter {

    private final UsuarioRepository usuarioRepository;
    private final JwtProvider jwtProvider;

    public TokenAuthenticationFilter(UsuarioRepository usuarioRepository, JwtProvider jwtProvider) {
        this.usuarioRepository = usuarioRepository;
        this.jwtProvider = jwtProvider;
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

            if (jwtProvider.validateToken(token)) {
                String email  = jwtProvider.extractEmail(token);
                Long   userId = jwtProvider.extractUserId(token);
                String role   = jwtProvider.extractClaims(token).get("role", String.class);

                // Verificar que el usuario siga activo en BD
                Usuario usuario = usuarioRepository.findById(userId).orElse(null);

                if (usuario != null && email.equals(usuario.getEmail())) {

                    SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role);

                    // Construir UsuarioSecurity como principal, así los controllers
                    // pueden acceder a userId sin hacer un cast incorrecto.
                    UsuarioSecurity usuarioSecurity = new UsuarioSecurity(
                            userId,
                            email,
                            null,          // password no necesaria post-auth
                            Collections.singletonList(authority));

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    usuarioSecurity,
                                    null,
                                    Collections.singletonList(authority));

                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
