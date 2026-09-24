package ru.matveysuvorov.teamup.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import ru.matveysuvorov.teamup.auth.filters.EmailVerificationFilter;

@Configuration
public class SecurityConfig {

    private final EmailVerificationFilter emailVerificationFilter;

    public SecurityConfig(
            EmailVerificationFilter emailVerificationFilter
    ) {
        this.emailVerificationFilter = emailVerificationFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                // Must be handled before authorization: BearerTokenAuthenticationFilter
                // runs before AuthorizationFilter, so preflight OPTIONS requests carry no
                // token. Without cors() Spring Security never answers them with
                // Access-Control-Allow-Origin, and the browser blocks the real request.
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/auth/**"
                        ).permitAll()
                                .anyRequest().authenticated()
                )
                .oauth2ResourceServer(
                        oauth2 -> oauth2.jwt(jwt -> {})
                )
                .addFilterAfter(
                        emailVerificationFilter,
                        BearerTokenAuthenticationFilter.class
                );

        return http.build();

    }

}
