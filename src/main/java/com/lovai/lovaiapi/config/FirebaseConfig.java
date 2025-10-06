package com.lovai.lovaiapi.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessaging;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@Configuration
public class FirebaseConfig {

    private static final Logger logger = LoggerFactory.getLogger(FirebaseConfig.class);

    @Value("${firebase.config.path:}")
    private String firebaseConfigPath;

    @Bean
    public FirebaseMessaging firebaseMessaging() {
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseOptions options;
                
                if (firebaseConfigPath != null && !firebaseConfigPath.isEmpty()) {
                    logger.info("Initializing Firebase with config file: {}", firebaseConfigPath);
                    
                    InputStream serviceAccount;
                    try {
                        serviceAccount = getClass().getClassLoader().getResourceAsStream(firebaseConfigPath);
                        if (serviceAccount == null) {
                            serviceAccount = new FileInputStream(firebaseConfigPath);
                        }
                    } catch (Exception e) {
                        logger.warn("Could not read Firebase config from path: {}, trying classpath", firebaseConfigPath);
                        serviceAccount = getClass().getClassLoader().getResourceAsStream("lovai-d5bd9-firebase-adminsdk-fbsvc-1ce77c3b86.json");
                    }
                    
                    if (serviceAccount == null) {
                        throw new RuntimeException("Firebase config file not found: " + firebaseConfigPath);
                    }
                    
                    GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccount);
                    
                    options = FirebaseOptions.builder()
                            .setCredentials(credentials)
                            .build();
                } else {
                    logger.info("Initializing Firebase with default credentials");
                    GoogleCredentials credentials = GoogleCredentials.getApplicationDefault();
                    
                    options = FirebaseOptions.builder()
                            .setCredentials(credentials)
                            .build();
                }
                
                FirebaseApp.initializeApp(options);
                logger.info("Firebase initialized successfully");
            } else {
                logger.info("Firebase already initialized");
            }
            
            return FirebaseMessaging.getInstance();
            
        } catch (IOException e) {
            logger.error("Error initializing Firebase: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to initialize Firebase", e);
        }
    }

    @Bean
    public FirebaseAuth firebaseAuth() {
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                firebaseMessaging();
            }
            return FirebaseAuth.getInstance();
        } catch (Exception e) {
            logger.error("Error initializing FirebaseAuth: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to initialize FirebaseAuth", e);
        }
    }
}

