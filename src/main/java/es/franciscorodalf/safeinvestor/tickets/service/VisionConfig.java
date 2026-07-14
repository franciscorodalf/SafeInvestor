package es.franciscorodalf.safeinvestor.tickets.service;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/** Registra los records de propiedades de los proveedores de visión. */
@Configuration
@EnableConfigurationProperties({GeminiProperties.class, AnthropicProperties.class})
public class VisionConfig {
}
