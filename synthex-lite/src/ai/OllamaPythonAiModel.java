package com.synthex.ai;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class OllamaPythonAiModel implements AiModel {
    private final String scriptPath;

    public OllamaPythonAiModel(String scriptPath) {
        this.scriptPath = scriptPath;
    }

    @Override
    public GeneratedData sendPrompt(String prompt) {
        try {
            List<String> command = new ArrayList<>();
            command.add("py");
            command.add("-3");
            command.add(scriptPath);
            command.add(prompt);

            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) output.append(line).append("\n");
            }
            
            process.waitFor();
            return parse(output.toString().trim());
        } catch (Exception e) {
            GeneratedData err = new GeneratedData();
            err.title = "Error"; err.description = "Failed: " + e.getMessage(); err.type = "ERROR";
            err.expectedOutput = "";
            return err;
        }
    }

    private GeneratedData parse(String stdout) {
        GeneratedData data = new GeneratedData();
        data.type = "Writing"; 
        data.title = "C++ Challenge";
        data.expectedOutput = "";
        
        // Parse line by line to find our markers
        int typeIdx = stdout.indexOf("TYPE=");
        int titleIdx = stdout.indexOf("TITLE=");
        int expectedIdx = stdout.indexOf("EXPECTED_OUTPUT=");
        int descIdx = stdout.indexOf("DESCRIPTION=");

        if (typeIdx != -1 && titleIdx != -1) {
            data.type = stdout.substring(typeIdx + 5, titleIdx).trim();
        }
        if (titleIdx != -1 && expectedIdx != -1) {
            data.title = stdout.substring(titleIdx + 6, expectedIdx).trim();
        }
        if (expectedIdx != -1 && descIdx != -1) {
            data.expectedOutput = stdout.substring(expectedIdx + 16, descIdx).trim();
        }
        if (descIdx != -1) {
            data.description = stdout.substring(descIdx + 12).trim();
            data.rawOutput = data.description;
        }

        return data;
    }
}
