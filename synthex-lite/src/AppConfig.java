package com.synthex;

public class AppConfig {
    public static final String SQLITE_DB_PATH = env(---);
    public static final String FIREBASE_SERVICE_ACCOUNT_PATH = env("---", "---");
    public static final String FIREBASE_API_KEY = env("----", "----");
    public static final String OLLAMA_MODEL = env("OLLAMA_MODEL", "qwen2.5-coder:3b");

    private static String env(String key, String defaultVal) {
        String v = System.getenv(key);
        return (v != null && !v.isBlank()) ? v : defaultVal;
    }
}
