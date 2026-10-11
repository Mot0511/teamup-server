package ru.matveysuvorov.teamup.auth.services;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.matveysuvorov.teamup.auth.dto.DiscordUser;
import ru.matveysuvorov.teamup.auth.exceptions.UnauthorizedException;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Service
public class DiscordAuthService {

    private static final String TOKEN_URL = "https://discord.com/api/oauth2/token";
    private static final String USER_URL = "https://discord.com/api/users/@me";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String clientId;
    private final String clientSecret;
    private final String redirectUri;

    public DiscordAuthService(
            ObjectMapper objectMapper,
            @Value("${discord.client-id}") String clientId,
            @Value("${discord.client-secret}") String clientSecret,
            @Value("${discord.redirect-uri}") String redirectUri
    ) {
        this.objectMapper = objectMapper;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUri = redirectUri;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public DiscordUser verify(String code) throws IOException, InterruptedException {
        String accessToken = exchangeCode(code);
        return fetchUser(accessToken);
    }

    private String exchangeCode(String code) throws IOException, InterruptedException {

        String form = "grant_type=authorization_code"
                + "&code=" + encode(code)
                + "&client_id=" + encode(clientId)
                + "&client_secret=" + encode(clientSecret)
                + "&redirect_uri=" + encode(redirectUri);
        HttpRequest request = HttpRequest.newBuilder(URI.create(TOKEN_URL))
                .timeout(Duration.ofSeconds(10))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(form))
                .build();

        JsonNode json = readJson(request, "Failed to exchange discord code");
        System.out.println(json);
        JsonNode accessToken = json.get("access_token");

        if (accessToken == null || accessToken.asText().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Discord did not return access token");
        }

        return accessToken.asText();
    }

    private DiscordUser fetchUser(String accessToken) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(USER_URL))
                .timeout(Duration.ofSeconds(10))
                .header("Authorization", "Bearer " + accessToken)
                .GET()
                .build();

        JsonNode json = readJson(request, "Failed to fetch discord user");
        JsonNode id = json.get("id");

        if (id == null || id.asText().isBlank()) {
            throw new UnauthorizedException("Discord did not return user id");
        }

        JsonNode email = json.get("email");
        String userEmail = email == null || email.isNull() ? null : email.asText();

        if (userEmail == null) {
            throw new UnauthorizedException("Discord account has no email");
        }

        return new DiscordUser(id.asText(), userEmail);
    }

    private JsonNode readJson(HttpRequest request, String message) throws IOException, InterruptedException {
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            HttpStatus status = response.statusCode() >= 500
                    ? HttpStatus.BAD_GATEWAY
                    : HttpStatus.BAD_REQUEST;
            throw new ResponseStatusException(
                    status,
                    message + ": " + response.body()
            );
        }

        return objectMapper.readTree(response.body());
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

}
