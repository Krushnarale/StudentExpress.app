package com.core2web.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.cloud.FirestoreClient;

import java.io.InputStream;

public class FirebaseConfig {

    private static Firestore firestore;
    private static FirebaseAuth auth;
    private static boolean initialized = false;

    private FirebaseConfig() {
    }

    public static synchronized void initializeFirebase() {

        if (initialized)
            return;

        try {

            InputStream serviceAccount = FirebaseConfig.class.getResourceAsStream(
                    "/firebase/serviceAccount.json");

            if (serviceAccount == null) {
                throw new RuntimeException("serviceAccount.json not found");
            }

            GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccount);

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(credentials)
                    .setProjectId("studentexpress-390ac")
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
            }

            firestore = FirestoreClient.getFirestore();
            auth = FirebaseAuth.getInstance();

            initialized = true;

            System.out.println("Firebase Connected Successfully");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Firestore getFirestore() {

        if (!initialized) {
            initializeFirebase();
        }

        return firestore;
    }

    public static FirebaseAuth getFirebaseAuth() {

        if (!initialized) {
            initializeFirebase();
        }

        return auth;
    }
}