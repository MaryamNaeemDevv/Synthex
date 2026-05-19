package com.synthex;

public class AppConfig {
    public static final String SQLITE_DB_PATH = env("SYNTHEX_SQLITE_PATH", "data/lite.db");
    public static final String FIREBASE_SERVICE_ACCOUNT_PATH = env("SYNTHEX_FIREBASE_SERVICE_ACCOUNT_PATH", "C:\\SDA Tasks\\Synthex\\synthex-e76bb-firebase-adminsdk-fbsvc-4529fbf21c.json");
    public static final String FIREBASE_API_KEY = env("SYNTHEX_FIREBASE_API_KEY", "AIzaSyAkUTgKOzkXcpbW0UD4LUpOveJSDuzxvpQ");
    public static final String OLLAMA_MODEL = env("OLLAMA_MODEL", "qwen2.5-coder:3b");

    private static String env(String key, String defaultVal) {
        String v = System.getenv(key);
        return (v != null && !v.isBlank()) ? v : defaultVal;
    }
}
