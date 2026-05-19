package com.synthex;

public interface AiModel {
    class GeneratedData {
        public String title;
        public String description;
        public String type;
        public String rawOutput;
    }
    GeneratedData sendPrompt(String prompt);
}
