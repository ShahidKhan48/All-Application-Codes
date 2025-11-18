package com.ninjacart.nfcservice.notifications;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

@Service
@Slf4j
public class FCMinitializer {
    @Value("${cloud.firebase.configuration-file}")
    private String firebaseConfigPath;

    @PostConstruct
    public void initialize() {
        try {
            if (Objects.isNull(firebaseConfigPath)) {
                log.debug("Firebase config file path is not mentioned in application.yml");
                return;
            }

            InputStream inputStream = new ClassPathResource(firebaseConfigPath).getInputStream();
            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(inputStream)).build();
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                log.info("Firebase application has been initialized");
            }
        } catch (IOException e) {
            log.debug("Error while initializing Firebase app");
            log.error(e.getMessage());
        }
    }
}
