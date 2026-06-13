package es.franciscorodalf.safeinvestor.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

import java.time.Duration;
import java.util.Locale;

/**
 * Internacionalización ES/EN.
 *
 * - LocaleResolver basado en cookie (\"si-lang\"), default español.
 * - LocaleChangeInterceptor: ?lang=es | ?lang=en cambia el idioma en cualquier ruta.
 * - El bundle messages.properties (ES) + messages_en.properties (EN) se carga
 *   automáticamente por Spring Boot al estar en classpath:/messages*.properties
 *   (auto-configuración de MessageSourceAutoConfiguration).
 */
@Configuration
public class I18nConfig implements WebMvcConfigurer {

    @Bean
    LocaleResolver localeResolver() {
        CookieLocaleResolver resolver = new CookieLocaleResolver("si-lang");
        resolver.setDefaultLocale(new Locale("es"));
        resolver.setCookieMaxAge(Duration.ofDays(365));
        resolver.setCookiePath("/");
        return resolver;
    }

    @Bean
    LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor interceptor = new LocaleChangeInterceptor();
        interceptor.setParamName("lang");
        return interceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor());
    }
}
