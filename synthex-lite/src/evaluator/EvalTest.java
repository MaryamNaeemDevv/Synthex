package com.synthex.evaluator;

import java.util.Scanner;

/**
 * Interactive test for the Docker Code Evaluator.
 * 
 * Lets you:
 *   1. Paste C++ code
 *   2. Optionally set an expected output
 *   3. Watch it compile and run inside Docker
 *   4. See the result (CORRECT, SYNTAX_ERROR, WRONG_OUTPUT, TIMEOUT, etc.)
 */
public class EvalTest {

    public static void main(String[] args) {
        System.out.println("=== Synthex Code Evaluator Test ===");
        System.out.println("This runs C++ code inside a Docker sandbox (gcc:latest).");
        System.out.println("Security: 128MB RAM, 0.5 CPU, no network, 10s timeout.\n");

        CodeSandbox sandbox;
        try {
            sandbox = new CodeSandbox();
        } catch (Exception e) {
            System.out.println("FATAL: Could not start Docker sandbox!");
            System.out.println("Make sure Docker Desktop is running.");
            System.out.println("Error: " + e.getMessage());
            return;
        }

        DockerCodeEvaluator evaluator = new DockerCodeEvaluator(sandbox);
        Scanner scanner = new Scanner(System.in);

        System.out.println("\nCommands:");
        System.out.println("  test    - Submit C++ code for evaluation");
        System.out.println("  quick   - Run a preset Hello World test");
        System.out.println("  syntax  - Run a preset syntax error test");
        System.out.println("  loop    - Run an infinite loop test (timeout check)");
        System.out.println("  wrong   - Run a wrong output test");
        System.out.println("  exit    - Stop the sandbox and quit\n");

        while (true) {
            System.out.print("EVAL > ");
            String input = scanner.nextLine().trim().toLowerCase();

            if (input.equals("exit")) break;

            if (input.equals("quick")) {
                runTest(evaluator, HELLO_WORLD, "Hello, World!");
            } else if (input.equals("syntax")) {
                runTest(evaluator, SYNTAX_ERROR_CODE, "");
            } else if (input.equals("loop")) {
                System.out.println("Submitting infinite loop... (should timeout in ~10s)");
                runTest(evaluator, INFINITE_LOOP, "");
            } else if (input.equals("wrong")) {
                runTest(evaluator, HELLO_WORLD, "Goodbye, World!");
            } else if (input.equals("test")) {
                System.out.println("Paste your C++ code below. Type 'END' on a new line when done:");
                StringBuilder code = new StringBuilder();
                while (true) {
                    String line = scanner.nextLine();
                    if (line.trim().equals("END")) break;
                    code.append(line).append("\n");
                }
                System.out.print("Expected output (press Enter to skip): ");
                String expected = scanner.nextLine().trim();
                runTest(evaluator, code.toString(), expected);
            }
        }

        evaluator.shutdown();
        System.out.println("Sandbox stopped. Goodbye!");
    }

    private static void runTest(DockerCodeEvaluator evaluator, String code, String expected) {
        System.out.println("\n--- Submitting to Docker Sandbox ---");
        long start = System.currentTimeMillis();
        EvaluationResult result = evaluator.sendToCompiler(code, expected);
        long elapsed = System.currentTimeMillis() - start;

        System.out.println("Result: " + result);
        System.out.println("Time:   " + elapsed + "ms");
        System.out.println("------------------------------------\n");
    }

    // ── Preset test cases ────────────────────────────────────────────────────

    private static final String HELLO_WORLD =
        "#include <iostream>\n" +
        "int main() {\n" +
        "    std::cout << \"Hello, World!\";\n" +
        "    return 0;\n" +
        "}\n";

    private static final String SYNTAX_ERROR_CODE =
        "#include <iostream>\n" +
        "int main() {\n" +
        "    std::cout << \"Missing semicolon\"\n" +
        "    return 0;\n" +
        "}\n";

    private static final String INFINITE_LOOP =
        "#include <iostream>\n" +
        "int main() {\n" +
        "    while(true) {}\n" +
        "    return 0;\n" +
        "}\n";
}
