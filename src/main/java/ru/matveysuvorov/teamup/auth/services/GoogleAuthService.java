package ru.matveysuvorov.teamup.auth.services;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.matveysuvorov.teamup.auth.dto.GoogleUser;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

@Service
public class GoogleAuthService {

    private final GoogleIdTokenVerifier verifier;

    public GoogleAuthService(
            @Value("${google.client-id}") String clientId
    ) {
        this.verifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(),
                GsonFactory.getDefaultInstance()
        )
                .setAudience(Collections.singletonList(clientId))
                .build();
    }

    public GoogleUser verify(String idToken) throws GeneralSecurityException, IOException {
        GoogleIdToken data = verifier.verify(idToken);
        if (data == null) {
            throw new IllegalArgumentException("Invalid Google ID token");
        }
        GoogleIdToken.Payload payload = data.getPayload();

        return new GoogleUser(payload.getSubject(), payload.getEmail());
    }

}