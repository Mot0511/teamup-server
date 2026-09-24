package ru.matveysuvorov.teamup.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * CORS for the HTTP API. Enabled in {@link SecurityConfig} via
 * {@code http.cors(...)}, so preflight OPTIONS requests are answered by Spring
 * Security's CorsFilter before BearerTokenAuthenticationFilter and
 * AuthorizationFilter run.
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource(
            @Value("${app.cors.allowed-origins:http://localhost:3000}") List<String> allowedOrigins
    ) {
        CorsConfiguration config = new CorsConfiguration();

        // Patterns, not plain origins: they are the only form that is allowed to
        // be combined with allowCredentials, should cookies be introduced later.
        config.setAllowedOriginPatterns(allowedOrigins);
        config.setAllowedMethods(List.of(
                "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"
        ));
        config.setAllowedHeaders(List.of(
                "Authorization", "Content-Type", "Accept"
        ));
        config.setExposedHeaders(List.of("Authorization"));

        // Tokens travel in the Authorization header, not in cookies.
        config.setAllowCredentials(false);

        // Cache preflight results for an hour so browsers do not send OPTIONS
        // before every API call.
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }

}
