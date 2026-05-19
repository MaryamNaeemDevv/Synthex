package com.synthex;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import java.io.FileInputStream;

public final class FirebaseContext {
    private static Firestore db;
    private static boolean initialized = false;

    public static synchronized boolean init() {
        if (initialized) return true;
        try {
            FileInputStream serviceAccount = new FileInputStream(AppConfig.FIREBASE_SERVICE_ACCOUNT_PATH);
            FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build();
            if (FirebaseApp.getApps().isEmpty()) FirebaseApp.initializeApp(options);
            db = FirestoreClient.getFirestore();
            initialized = true;
            return true;
        } catch (Exception e) {
            System.err.println("Firebase Init failed: " + e.getMessage());
            return false;
        }
    }
    public static Firestore getFirestore() { return db; }
}
