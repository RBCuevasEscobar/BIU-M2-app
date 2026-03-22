package com.ecommerce;

import com.ecommerce.model.SystemConfig;
import com.ecommerce.repository.SystemConfigRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests para SystemConfigRepository usando MySQL real.
 * Limpia los registros creados en cada test para no contaminar la DB.
 */
@SpringBootTest
class SystemConfigRepositoryTest {

    @Autowired
    private SystemConfigRepository repo;

    private static final String TEST_KEY_PREFIX = "test.";

    @AfterEach
    void cleanUp() {
        // Borrar solo los registros de prueba para no afectar la DB de producción
        repo.deleteById(TEST_KEY_PREFIX + "iva");
        repo.deleteById(TEST_KEY_PREFIX + "moneda");
        repo.deleteById(TEST_KEY_PREFIX + "jwt");
        repo.deleteById(TEST_KEY_PREFIX + "nuevo");
    }

    @Test
    @DisplayName("save y findById: persiste y recupera un registro correctamente")
    void testSaveAndFindById() {
        repo.save(new SystemConfig(TEST_KEY_PREFIX + "iva", "0.16"));

        Optional<SystemConfig> result = repo.findById(TEST_KEY_PREFIX + "iva");

        assertTrue(result.isPresent());
        assertEquals("0.16", result.get().getValue());
    }

    @Test
    @DisplayName("save: actualiza el valor si la clave ya existe (upsert)")
    void testSave_actualizaRegistroExistente() {
        repo.save(new SystemConfig(TEST_KEY_PREFIX + "iva", "0.16"));
        repo.save(new SystemConfig(TEST_KEY_PREFIX + "iva", "0.21")); // misma key, nuevo valor

        Optional<SystemConfig> result = repo.findById(TEST_KEY_PREFIX + "iva");

        assertTrue(result.isPresent());
        assertEquals("0.21", result.get().getValue());
    }

    @Test
    @DisplayName("findById: retorna vacío si la clave no existe")
    void testFindById_claveFaltante_retornaEmpty() {
        Optional<SystemConfig> result = repo.findById("clave_inexistente_xyz_99");
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("findAll: incluye los registros guardados en el test")
    void testFindAll_incluyeRegistros() {
        repo.save(new SystemConfig(TEST_KEY_PREFIX + "moneda", "MXN"));
        repo.save(new SystemConfig(TEST_KEY_PREFIX + "jwt", "86400000"));

        List<SystemConfig> all = repo.findAll();

        assertTrue(all.size() >= 2);
        assertTrue(all.stream().anyMatch(sc -> sc.getKey().equals(TEST_KEY_PREFIX + "moneda")));
    }
}
