package com.ecommerce;

import com.ecommerce.model.SystemConfig;
import com.ecommerce.repository.SystemConfigRepository;
import com.ecommerce.security.JwtConfig;
import com.ecommerce.service.ConfiguracionSistemaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para ConfiguracionSistemaService.
 * Usa Mockito para aislar el repositorio y verificar comportamiento.
 */
class ConfiguracionSistemaServiceTest {

    private SystemConfigRepository mockRepo;
    private JwtConfig jwtConfig;
    private ConfiguracionSistemaService service;

    @BeforeEach
    void setUp() {
        mockRepo = mock(SystemConfigRepository.class);
        jwtConfig = new JwtConfig();
        service = new ConfiguracionSistemaService(mockRepo, jwtConfig);
    }

    @Test
    @DisplayName("cargarDesdeDB: aplica IVA del repositorio al Singleton")
    void testCargarDesdeDB_aplicaIva() {
        when(mockRepo.findById("iva")).thenReturn(Optional.of(new SystemConfig("iva", "0.19")));
        when(mockRepo.findById("monedaSistema")).thenReturn(Optional.empty());
        when(mockRepo.findById("stockMinimo")).thenReturn(Optional.empty());
        when(mockRepo.findById("modoDebug")).thenReturn(Optional.empty());
        when(mockRepo.findById("maxProductosOrden")).thenReturn(Optional.empty());
        when(mockRepo.findById("jwt.expirationTime")).thenReturn(Optional.empty());

        service.cargarDesdeDB();

        assertEquals(0.19, service.getConfiguracionSistema().getIva(), 0.001);
    }

    @Test
    @DisplayName("cargarDesdeDB: aplica jwt.expirationTime a JwtConfig")
    void testCargarDesdeDB_aplicaJwtExpiration() {
        when(mockRepo.findById("iva")).thenReturn(Optional.empty());
        when(mockRepo.findById("monedaSistema")).thenReturn(Optional.empty());
        when(mockRepo.findById("stockMinimo")).thenReturn(Optional.empty());
        when(mockRepo.findById("modoDebug")).thenReturn(Optional.empty());
        when(mockRepo.findById("maxProductosOrden")).thenReturn(Optional.empty());
        when(mockRepo.findById("jwt.expirationTime"))
                .thenReturn(Optional.of(new SystemConfig("jwt.expirationTime", "3600000")));

        service.cargarDesdeDB();

        assertEquals(3600000L, jwtConfig.getExpirationTime());
    }

    @Test
    @DisplayName("cargarDesdeDB: tabla vacía, el Singleton conserva defaults")
    void testCargarDesdeDB_tablaVacia_conservaDefaults() {
        when(mockRepo.findById(anyString())).thenReturn(Optional.empty());

        double ivaAntes = service.getConfiguracionSistema().getIva();
        service.cargarDesdeDB();

        // Si no hay valores en DB, el Singleton no cambia
        assertEquals(ivaAntes, service.getConfiguracionSistema().getIva(), 0.001);
    }

    @Test
    @DisplayName("persistirSistema: guarda todos los parámetros del Singleton")
    void testPersistirSistema_guardaTodosLosParametros() {
        Map<String, Object> body = Map.of(
                "iva", "0.16",
                "monedaSistema", "USD",
                "stockMinimo", "10",
                "modoDebug", "true",
                "maxProductosOrden", "100"
        );

        service.persistirSistema(body);

        verify(mockRepo, times(5)).save(any(SystemConfig.class));
    }

    @Test
    @DisplayName("persistirSistema: guarda solo los campos presentes en el body")
    void testPersistirSistema_soloFieldsPresentes() {
        Map<String, Object> body = Map.of("iva", "0.20");

        service.persistirSistema(body);

        ArgumentCaptor<SystemConfig> captor = ArgumentCaptor.forClass(SystemConfig.class);
        verify(mockRepo, times(1)).save(captor.capture());
        assertEquals("iva", captor.getValue().getKey());
        assertEquals("0.20", captor.getValue().getValue());
    }

    @Test
    @DisplayName("persistirJwt: guarda jwt.expirationTime correctamente")
    void testPersistirJwt_guardaExpirationTime() {
        service.persistirJwt(7200000L);

        ArgumentCaptor<SystemConfig> captor = ArgumentCaptor.forClass(SystemConfig.class);
        verify(mockRepo).save(captor.capture());
        assertEquals("jwt.expirationTime", captor.getValue().getKey());
        assertEquals("7200000", captor.getValue().getValue());
    }
}
