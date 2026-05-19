package com.synthex.evaluator;

/**
 * Real code evaluator that uses the Docker sandbox.
 * 
 * Flow:
 *   1. Send user code to CodeSandbox (compile + run)
 *   2. Normalize and compare actual output with expected output
 *   3. Return structured EvaluationResult
 * 
 * Output normalization rules (to prevent false negatives):
 *   - \r\n → \n (Windows vs Unix line endings)
 *   - Trailing whitespace stripped per line
 *   - Leading/trailing blank lines removed
 *   - Multiple consecutive blank lines collapsed to one
 *   - Final comparison is case-sensitive but whitespace-tolerant
 */
public class DockerCodeEvaluator implements CodeEvaluator {
    private final CodeSandbox sandbox;

    public DockerCodeEvaluator(CodeSandbox sandbox) {
        this.sandbox = sandbox;
    }

    @Override
    public EvaluationResult sendToCompiler(String userCode, String expectedOutput) {
        try {
            String actualOutput = sandbox.run(userCode);

            if (expectedOutput == null || expectedOutput.trim().isEmpty()) {
                return new EvaluationResult("CORRECT", true,
                    "Code compiled and ran successfully. Output: " + actualOutput);
            }

            String normalizedActual = normalize(actualOutput);
            String normalizedExpected = normalize(expectedOutput);

            if (normalizedActual.equals(normalizedExpected)) {
                return new EvaluationResult("CORRECT", true, "Output matched expected.");
            } else {
                return new EvaluationResult("WRONG_OUTPUT", false,
                    "Expected:\n[" + normalizedExpected + "]\nGot:\n[" + normalizedActual + "]");
            }

        } catch (CodeSandbox.SubmissionTimeoutException e) {
            return new EvaluationResult("TIMEOUT", false, e.getMessage());

        } catch (CodeSandbox.SandboxException e) {
            if (e.getMessage().startsWith("__SYNTAX_ERROR__")) {
                String err = e.getMessage().substring("__SYNTAX_ERROR__:".length());
                return new EvaluationResult("SYNTAX_ERROR", false, err);
            }
            return new EvaluationResult("RUNTIME_ERROR", false, e.getMessage());

        } catch (Exception e) {
            return new EvaluationResult("EVALUATOR_ERROR", false, "Evaluator failure: " + e.getMessage());
        }
    }

    /**
     * Normalize output to prevent false negatives from formatting differences.
     * 
     * Steps:
     *   1. Replace \r\n and \r with \n
     *   2. Strip trailing whitespace from each line
     *   3. Remove leading/trailing blank lines
     *   4. Collapse multiple consecutive blank lines into one
     */
    private String normalize(String output) {
        if (output == null) return "";

        // Step 1: Unify line endings
        String result = output.replace("\r\n", "\n").replace("\r", "\n");

        // Step 2: Strip trailing whitespace per line
        String[] lines = result.split("\n", -1);
        StringBuilder sb = new StringBuilder();
        for (String line : lines) {
            sb.append(line.stripTrailing()).append("\n");
        }
        result = sb.toString();

        // Step 3: Collapse multiple blank lines into one
        result = result.replaceAll("\n{3,}", "\n\n");

        // Step 4: Trim leading/trailing blank lines
        result = result.strip();

        return result;
    }

    public void shutdown() {
        if (sandbox != null) sandbox.close();
    }
}
