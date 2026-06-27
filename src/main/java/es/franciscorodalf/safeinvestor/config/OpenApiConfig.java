package es.franciscorodalf.safeinvestor.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuración de OpenAPI / Swagger UI.
 *
 * Expone:
 *   - /v3/api-docs    → especificación JSON
 *   - /swagger-ui.html → UI interactivo (con botón \"Authorize\" para JWT)
 *
 * El esquema de seguridad \"bearerAuth\" se aplica globalmente: en la UI
 * basta con pulsar Authorize, pegar el JWT obtenido en POST /api/auth/login
 * y todas las llamadas a /api/** lo incluirán en el header Authorization.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI safeInvestorOpenAPI(@Value("${app.base-url:http://localhost:8080}") String baseUrl) {
        final String SECURITY_SCHEME = "bearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("SafeInvestor API")
                        .description("""
                                API REST de **SafeInvestor**, una app de finanzas personales.

                                ### Cómo usar esta API
                                1. Llama a `POST /api/auth/register` o `POST /api/auth/login` y guarda el campo `token`.
                                2. Pulsa el botón **Authorize** (arriba a la derecha) y pega el token.
                                3. Llama a cualquier endpoint protegido — el token viaja en el header `Authorization: Bearer <token>`.

                                Los endpoints `/api/auth/*` son los únicos públicos. El resto requiere JWT válido.
                                """)
                        .version("v1")
                        .contact(new Contact()
                                .name("Francisco Rodríguez")
                                .url("https://github.com/franciscorodalf/SafeInvestor"))
                        .license(new License()
                                .name("MIT")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server().url(baseUrl).description("Servidor actual"),
                        new Server().url("https://safeinvestor.onrender.com").description("Producción (Render)"),
                        new Server().url("http://localhost:8080").description("Local")))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME, new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT obtenido en `POST /api/auth/login`")))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME));
    }
}
