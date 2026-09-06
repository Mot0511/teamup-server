package ru.matveysuvorov.teamup.security;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;


@Configuration
public class JwtKeyConfig {

    @Value("${jwt.private-key-path}")
    private String privateKeyPath;

    @Value("${jwt.public-key-path}")
    private String publicKeyPath;

    @Bean
    public RSAKey rsaKey() throws Exception {

        RSAPrivateKey privateKey = loadPrivateKey(privateKeyPath);
        RSAPublicKey publicKey = loadPublicKey(publicKeyPath);

        return new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyID("teamup-key")
                .build();
    }

    private RSAPrivateKey loadPrivateKey(
            String path
    ) throws Exception {

        String key = Files.readString(
                Path.of(path)
        );

        key = key
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");

        byte[] decoded = Base64.getDecoder().decode(key);

        PKCS8EncodedKeySpec spec =
                new PKCS8EncodedKeySpec(decoded);

        KeyFactory factory =
                KeyFactory.getInstance("RSA");

        return (RSAPrivateKey) factory.generatePrivate(spec);
    }

    private RSAPublicKey loadPublicKey(
            String path
    ) throws Exception {

        String key = Files.readString(
                Path.of(path)
        );

        key = key
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");

        byte[] decoded = Base64.getDecoder().decode(key);

        X509EncodedKeySpec spec =
                new X509EncodedKeySpec(decoded);

        KeyFactory factory =
                KeyFactory.getInstance("RSA");

        return (RSAPublicKey) factory.generatePublic(spec);
    }

    @Bean
    public JwtEncoder jwtEncoder(RSAKey rsaKey) {

        JWKSource<SecurityContext> jwkSource =
                (selector, context) ->
                        selector.select(
                                new JWKSet(rsaKey)
                        );

        return new NimbusJwtEncoder(jwkSource);
    }

    @Bean
    public JwtDecoder jwtDecoder(RSAKey rsaKey) throws Exception {
        return NimbusJwtDecoder
                .withPublicKey(rsaKey.toRSAPublicKey())
                .build();
    }

}
