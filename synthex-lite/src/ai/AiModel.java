package com.synthex.ai;

public interface AiModel {
    class GeneratedData {
        public String title;
        public String description;
        public String type;
        public String rawOutput;
        public String expectedOutput;
    }
    GeneratedData sendPrompt(String prompt);
}
