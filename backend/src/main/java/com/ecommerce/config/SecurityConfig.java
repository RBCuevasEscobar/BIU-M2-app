package com.ecommerce.config;

import com.ecommerce.security.TokenAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // Enable @PreAuthorize
public class SecurityConfig {

    private final CorsConfigurationSource corsConfigurationSource;
    private final TokenAuthenticationFilter tokenAuthenticationFilter;

    public SecurityConfig(CorsConfigurationSource corsConfigurationSource,
            TokenAuthenticationFilter tokenAuthenticationFilter) {
        this.corsConfigurationSource = corsConfigurationSource;
        this.tokenAuthenticationFilter = tokenAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .authorizeHttpRequests(auth -> auth
                        // 🔑 PERMITIR PREFLIGHT
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // 🔓 ENDPOINTS PÚBLICOS
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/public/**").permitAll()
                        .requestMatchers("/api/usuarios/cliente").permitAll()
                        .requestMatchers("/api/usuarios/proveedor").permitAll()
                        .requestMatchers("/api/usuarios/admin").permitAll()
                        .requestMatchers("/api/productos").permitAll()

                        // 🔐 CUALQUIER OTRA SOLICITUD ESTA PROTEGIDA
                        .requestMatchers("/api/usuarios").authenticated()
                        .anyRequest().authenticated())
                .addFilterBefore(tokenAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
