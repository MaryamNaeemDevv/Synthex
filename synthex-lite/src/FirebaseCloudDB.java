package com.synthex;

import com.google.cloud.firestore.*;
import java.util.*;

public class FirebaseCloudDB implements CloudDB {
    private Firestore db;
    private boolean initialized;

    public FirebaseCloudDB() {
        initialized = FirebaseContext.init();
        db = FirebaseContext.getFirestore();
    }

    @Override
    public void pushData(Map<String, Object> data) {
        if (!initialized) return;
        try { db.collection("syncData").add(data).get(); } catch (Exception e) {}
    }

    @Override
    public boolean verifyIntegrity() { return initialized; }

    @Override
    public List<Map<String, Object>> fetchLeaderboard() {
        List<Map<String, Object>> result = new ArrayList<>();
        if (!initialized) return result;
        try {
            QuerySnapshot snapshot = db.collection("users").orderBy("highscore", Query.Direction.DESCENDING).limit(10).get().get();
            for (DocumentSnapshot doc : snapshot.getDocuments()) {
                Map<String, Object> entry = new HashMap<>();
                entry.put("username", doc.getString("username"));
                entry.put("score", doc.getLong("highscore").intValue());
                result.add(entry);
            }
        } catch (Exception e) {}
        return result;
    }

    @Override
    public List<Map<String, Object>> fetchFriendList(String playerId) {
        List<Map<String, Object>> result = new ArrayList<>();
        if (!initialized) return result;
        try {
            String uid = resolveUid(playerId);
            if (uid == null) return result;
            QuerySnapshot snapshot = db.collection("friends").whereEqualTo("userUid", uid).get().get();
            for (DocumentSnapshot doc : snapshot.getDocuments()) {
                Map<String, Object> entry = new HashMap<>();
                entry.put("username", doc.getString("friendUsername"));
                result.add(entry);
            }
        } catch (Exception e) {}
        return result;
    }

    @Override
    public void upsertLeaderboardEntry(String userID, String username, int score) {
        if (!initialized) return;
        try {
            String uid = resolveUid(userID);
            if (uid == null) return;
            DocumentReference docRef = db.collection("users").document(uid);
            Map<String, Object> data = new HashMap<>();
            data.put("highscore", score);
            data.put("username", username);
            docRef.set(data, SetOptions.merge()).get();
        } catch (Exception e) {}
    }

    private String resolveUid(String username) throws Exception {
        DocumentSnapshot snap = db.collection("usernames").document(username.toLowerCase()).get().get();
        return snap.exists() ? snap.getString("uid") : null;
    }
}
