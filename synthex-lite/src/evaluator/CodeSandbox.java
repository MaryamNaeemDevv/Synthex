package com.synthex.evaluator;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.*;

/**
 * Docker-based sandbox for compiling and running C++ code safely.
 * 
 * Security restrictions:
 *   - Memory cap: 128MB
 *   - CPU cap: 0.5 cores
 *   - No network access
 *   - Compile timeout: 15s
 *   - Run timeout: 10s
 *   - Container auto-removed on stop
 */
public class CodeSandbox {

    private static final String DOCKER = "C:\\Program Files\\Docker\\Docker\\resources\\bin\\docker.exe";
    private static final String IMAGE = "gcc:latest";
    private static final String CONTAINER_NAME = "synthex_cpp_sandbox";
    private static final int COMPILE_TIMEOUT_SECONDS = 15;
    private static final int RUN_TIMEOUT_SECONDS = 10;

    private String containerId;

    public CodeSandbox() throws IOException, InterruptedException {
        cleanup(); // Kill any leftover container from a previous crash
        startContainer();
    }

    private void cleanup() {
        try {
            new ProcessBuilder(DOCKER, "rm", "-f", CONTAINER_NAME)
                .redirectErrorStream(true).start().waitFor(5, TimeUnit.SECONDS);
        } catch (Exception ignored) {}
    }

    private void startContainer() throws IOException, InterruptedException {
        System.out.println("[Sandbox] Starting Docker container (" + IMAGE + ")...");
        ProcessBuilder pb = new ProcessBuilder(
            DOCKER, "run", "-d", "--rm",
            "--name", CONTAINER_NAME,
            "--memory", "128m",
            "--cpus", "0.5",
            "--network", "none",
            IMAGE,
            "sleep", "infinity"
        );
        pb.redirectErrorStream(true);
        Process p = pb.start();
        containerId = new String(p.getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim();
        p.waitFor();
        if (p.exitValue() != 0) {
            throw new IOException("Docker failed to start: " + containerId);
        }
        System.out.println("[Sandbox] Container ready: " + containerId.substring(0, 12));
    }

    /**
     * Write code into container, compile with g++, and run.
     * Returns the program's stdout output.
     */
    public String run(String code) throws SubmissionTimeoutException, SandboxException {
        try {
            writeCode(code);
            compileCode();
            return executeCode();
        } catch (SubmissionTimeoutException | SandboxException e) {
            throw e;
        } catch (IOException | InterruptedException e) {
            throw new SandboxException("Sandbox process error: " + e.getMessage(), e);
        }
    }

    // ── write code into the container ────────────────────────────────────────

    private void writeCode(String code) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(
            DOCKER, "exec", "-i", containerId,
            "bash", "-c", "cat > /tmp/main.cpp"
        );
        pb.redirectErrorStream(true);
        Process p = pb.start();

        try (OutputStream stdin = p.getOutputStream()) {
            stdin.write(code.getBytes(StandardCharsets.UTF_8));
        }

        p.waitFor();
    }

    // ── compile with g++ ─────────────────────────────────────────────────────

    private void compileCode() throws IOException, InterruptedException, SubmissionTimeoutException, SandboxException {
        ProcessBuilder pb = new ProcessBuilder(
            DOCKER, "exec", containerId,
            "g++", "-o", "/tmp/main", "/tmp/main.cpp"
        );
        pb.redirectErrorStream(false);
        Process p = pb.start();

        boolean finished = p.waitFor(COMPILE_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        if (!finished) {
            p.destroyForcibly();
            killContainerProcess("g++");
            throw new SubmissionTimeoutException(
                "Compilation exceeded " + COMPILE_TIMEOUT_SECONDS + "s limit."
            );
        }

        if (p.exitValue() != 0) {
            String stderr = new String(p.getErrorStream().readAllBytes(), StandardCharsets.UTF_8).trim();
            throw new SandboxException("__SYNTAX_ERROR__:" + stderr);
        }
    }

    // ── execute the compiled binary ──────────────────────────────────────────

    private String executeCode() throws IOException, InterruptedException, SubmissionTimeoutException, SandboxException {
        ProcessBuilder pb = new ProcessBuilder(
            DOCKER, "exec", containerId,
            "/tmp/main"
        );
        pb.redirectErrorStream(false);
        Process p = pb.start();

        boolean finished = p.waitFor(RUN_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        if (!finished) {
            p.destroyForcibly();
            killContainerProcess("main");
            throw new SubmissionTimeoutException(
                "Execution exceeded " + RUN_TIMEOUT_SECONDS + "s limit."
            );
        }

        if (p.exitValue() != 0) {
            String stderr = new String(p.getErrorStream().readAllBytes(), StandardCharsets.UTF_8).trim();
            throw new SandboxException("Runtime error: " + stderr);
        }

        return new String(p.getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim();
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private void killContainerProcess(String processName) {
        try {
            new ProcessBuilder(
                DOCKER, "exec", containerId,
                "pkill", "-f", processName
            ).start().waitFor(3, TimeUnit.SECONDS);
        } catch (Exception ignored) {}
    }

    public void close() {
        System.out.println("[Sandbox] Stopping container...");
        try {
            new ProcessBuilder(DOCKER, "stop", containerId)
                .start()
                .waitFor(10, TimeUnit.SECONDS);
        } catch (Exception ignored) {}
    }

    // ── custom exceptions ────────────────────────────────────────────────────

    public static class SubmissionTimeoutException extends Exception {
        public SubmissionTimeoutException(String msg) { super(msg); }
    }

    public static class SandboxException extends Exception {
        public SandboxException(String msg) { super(msg); }
        public SandboxException(String msg, Throwable cause) { super(msg, cause); }
    }
}
