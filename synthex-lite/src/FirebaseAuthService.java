package com.synthex;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class FirebaseAuthService {
    public static class AuthResult {
        public final boolean success;
        public final String uid;
        public final String username;
        public final String message;

        public AuthResult(boolean success, String uid, String username, String message) {
            this.success = success;
            this.uid = uid;
            this.username = username;
            this.message = message;
        }
    }

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final Gson gson = new Gson();

    public AuthResult register(String email, String password, String username) {
        if (!FirebaseContext.init()) return new AuthResult(false, null, null, "Firebase init failed.");
        
        try {
            Firestore db = FirebaseContext.getFirestore();
            if (db.collection("usernames").document(username.toLowerCase()).get().get().exists()) {
                return new AuthResult(false, null, null, "Username taken.");
            }

            JsonObject payload = new JsonObject();
            payload.addProperty("email", email);
            payload.addProperty("password", password);
            payload.addProperty("returnSecureToken", true);

            String url = "https://identitytoolkit.googleapis.com/v1/accounts:signUp?key=" + AppConfig.FIREBASE_API_KEY;
            JsonObject resp = postJson(url, payload);
            if (resp.has("error")) return new AuthResult(false, null, null, resp.get("error").toString());

            String uid = resp.get("localId").getAsString();
            
            // Link username
            Map<String, Object> uData = new HashMap<>();
            uData.put("uid", uid); uData.put("username", username);
            db.collection("usernames").document(username.toLowerCase()).set(uData);
            
            Map<String, Object> pData = new HashMap<>();
            pData.put("email", email); pData.put("username", username); pData.put("highscore", 0);
            db.collection("users").document(uid).set(pData);

            return new AuthResult(true, uid, username, "Registered!");
        } catch (Exception e) {
            return new AuthResult(false, null, null, e.getMessage());
        }
    }

    public AuthResult login(String email, String password) {
        if (!FirebaseContext.init()) return new AuthResult(false, null, null, "Firebase init failed.");
        try {
            JsonObject payload = new JsonObject();
            payload.addProperty("email", email);
            payload.addProperty("password", password);
            payload.addProperty("returnSecureToken", true);

            String url = "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=" + AppConfig.FIREBASE_API_KEY;
            JsonObject resp = postJson(url, payload);
            if (resp.has("error")) return new AuthResult(false, null, null, "Login failed.");

            String uid = resp.get("localId").getAsString();
            DocumentSnapshot snap = FirebaseContext.getFirestore().collection("users").document(uid).get().get();
            return new AuthResult(true, uid, snap.getString("username"), "Welcome!");
        } catch (Exception e) {
            return new AuthResult(false, null, null, e.getMessage());
        }
    }

    private JsonObject postJson(String url, JsonObject payload) throws Exception {
        HttpRequest req = HttpRequest.newBuilder().uri(URI.create(url))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(payload)))
            .build();
        return gson.fromJson(httpClient.send(req, HttpResponse.BodyHandlers.ofString()).body(), JsonObject.class);
    }
}
