package com.ecommerce;

import com.ecommerce.config.ConfiguracionSistema;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Pruebas unitarias para validar las propiedades del patrón Singleton.
 */
public class ConfiguracionSistemaTest {

    @Test
    public void testInstanciaInica() {
        ConfiguracionSistema config1 = ConfiguracionSistema.getInstance();
        ConfiguracionSistema config2 = ConfiguracionSistema.getInstance();

        // Debe ser exactamente la misma referencia en memoria
        assertSame(config1, config2, "Las instancias del Singleton deben ser idénticas");
    }

    @Test
    public void testThreadSafetySingleton() throws InterruptedException {
        int threads = 100;
        ExecutorService service = Executors.newFixedThreadPool(threads);
        CountDownLatch latch = new CountDownLatch(threads);
        
        final ConfiguracionSistema[] instancias = new ConfiguracionSistema[threads];

        for (int i = 0; i < threads; i++) {
            final int index = i;
            service.submit(() -> {
                instancias[index] = ConfiguracionSistema.getInstance();
                latch.countDown();
            });
        }
        
        latch.await();

        // Validar que en un ambiente de alta concurrencia, todos posean la misma instancia.
        ConfiguracionSistema primeraInstancia = instancias[0];
        for (ConfiguracionSistema instancia : instancias) {
            assertSame(primeraInstancia, instancia, "Violación de Thread-Safety en Singleton");
        }
    }

    @Test
    public void testModificacionValoresAtomicos() {
        ConfiguracionSistema config = ConfiguracionSistema.getInstance();
        
        // Double Original = 0.16
        config.setIva(0.20);
        assertEquals(0.20, config.getIva(), "El valor referenciado debe alterarse correctamente de forma atómica");
        
        // Return default for testing sanity
        config.setIva(0.16);
    }
}
