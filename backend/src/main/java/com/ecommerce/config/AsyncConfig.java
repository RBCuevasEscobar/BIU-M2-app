package com.ecommerce.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Habilita el procesamiento en Hilos Paralelos para los Observadores asíncronos.
 */
@Configuration
@EnableAsync
public class AsyncConfig {
}
