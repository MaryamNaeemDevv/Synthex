# Synthex Docker Evaluator Setup

The Synthex backend uses a real Code Evaluator (`DockerCodeEvaluator`) powered by Docker to safely compile and run C++ code submitted by players.

This document explains how to set up, test, and troubleshoot the Docker evaluator.

## Requirements
- **Docker**: Must be installed and running. On Windows, this means **Docker Desktop** must be running in the background.

## The Docker Image
The evaluator requires the standard C++ compilation image:
- **Image Name**: `gcc:latest`

### How to pull the image
Open your terminal and run:
```powershell
docker pull gcc:latest
```

## How to Test
We have provided an automated script to verify your Docker installation and run a mock C++ compilation.
Run the following in PowerShell from the repository root:
```powershell
.\scripts\test-docker-evaluator.ps1
```

## How to Enable in the Backend
1. Open `src/main/java/com/synthex/util/AppConfig.java`.
2. Change the flag:
   ```java
   public static final boolean USE_REAL_EVALUATOR = true;
   ```
3. Restart the backend:
   ```powershell
   .\mvnw.cmd exec:java
   ```

## The Fallback Mechanism
If `USE_REAL_EVALUATOR` is set to `true`, but Docker is NOT running (or not installed), the Synthex backend **will not crash**. 

Instead, it will catch the initialization exception, log a warning, and automatically fall back to the `MockCodeEvaluator`. You will see this in the backend terminal logs:
`[Synthex] Failed to initialize DockerCodeEvaluator (Cannot run program "docker"...). Falling back to MockCodeEvaluator.`

## Troubleshooting Common Errors

### `Cannot run program "docker"`
- **Cause**: Docker is not installed, or its path is not in your system's environment variables.
- **Solution**: Install Docker Desktop and ensure it is added to your PATH.

### `error during connect: ...` (Docker daemon not running)
- **Cause**: Docker is installed, but the background service (Docker daemon) is stopped.
- **Solution**: Launch Docker Desktop manually and wait for the engine to start.

### `Unable to find image 'gcc:latest' locally`
- **Cause**: The backend attempted to start the sandbox, but the required image hasn't been pulled.
- **Solution**: Run `docker pull gcc:latest`.

### `Compilation exceeded 15s limit` / `Execution exceeded 10s limit`
- **Cause**: The submitted C++ code has an infinite loop or takes too long to compile.
- **Solution**: The Sandbox automatically kills the process and returns a `TIMEOUT` or `WRONG_OUTPUT` evaluation result to the Unity client.

### `Syntax Error`
- **Cause**: The player's code failed to compile using `g++`.
- **Solution**: The Sandbox captures the compiler's `stderr` and maps it directly to a `SYNTAX_ERROR` evaluation result.
