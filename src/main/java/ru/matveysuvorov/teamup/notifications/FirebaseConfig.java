package ru.matveysuvorov.teamup.notifications;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import java.io.FileInputStream;

@Configuration
public class FirebaseConfig {

    private static final Logger log = LoggerFactory.getLogger(FirebaseConfig.class);

    @Value("${firebase.service-account-path:}")
    private String serviceAccountPath;

    @PostConstruct
    public void init() {
        if (serviceAccountPath == null || serviceAccountPath.isBlank()) {
            log.warn("firebase.service-account-path not set. FCM notifications are disabled.");
            return;
        }

        boolean alreadyInitialized = FirebaseApp.getApps().stream()
                .anyMatch(app -> FirebaseApp.DEFAULT_APP_NAME.equals(app.getName()));
        if (alreadyInitialized) {
            return;
        }

        try (FileInputStream serviceAccount = new FileInputStream(serviceAccountPath)) {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();
            FirebaseApp.initializeApp(options);
            log.info("FirebaseApp initialized for FCM notifications.");
        } catch (Exception e) {
            log.warn("Failed to initialize FirebaseApp: {}", e.getMessage());
        }
    }
}
