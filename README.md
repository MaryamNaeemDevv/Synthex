# Synthex Core — Java Backend Skeleton

Central Java backend for the Synthex 3D endless runner coding game.

## What This Is

This is the **central skeleton** that connects:
- **AI Queue** (real — calls Ollama via Python bridge)
- **Code Evaluator** (mock — plug in Docker sandbox later)
- **Local DB** (mock — plug in SQLite/SQL Server later)
- **Cloud DB** (mock — plug in Firebase later)
- **Unity API** (HTTP endpoints — Unity calls these)

## Prerequisites

- **Java 17+**
- **Maven 3.6+**
- **Python 3** (for AI bridge)
- **Ollama** (optional — falls back if unavailable)

## Quick Start

```bash
# 1. Build (from repo root)
mvn clean compile

# 2. Run
mvn exec:java

# 3. Test endpoints (in another terminal)
curl http://localhost:8080/health
curl http://localhost:8080/leaderboard
curl http://localhost:8080/challenge/current
curl -X POST http://localhost:8080/auth/login -H "Content-Type: application/json" -d '{"username":"test","password":"test"}'
curl -X POST http://localhost:8080/game/start -H "Content-Type: application/json" -d '{}'
curl -X POST http://localhost:8080/challenge/submit -H "Content-Type: application/json" -d '{"challengeId":"abc","submittedCode":"int main(){}","timeTaken":5.0}'
```

### PowerShell Test Commands

```powershell
# Health check
Invoke-RestMethod -Uri http://localhost:8080/health

# Login
Invoke-RestMethod -Method Post -Uri http://localhost:8080/auth/login -Body '{"username":"test","password":"test"}' -ContentType "application/json"

# Start game
Invoke-RestMethod -Method Post -Uri http://localhost:8080/game/start -Body '{}' -ContentType "application/json"

# Get challenge
Invoke-RestMethod -Uri http://localhost:8080/challenge/current

# Submit code
Invoke-RestMethod -Method Post -Uri http://localhost:8080/challenge/submit -Body '{"challengeId":"abc","submittedCode":"int main(){}","timeTaken":5.0}' -ContentType "application/json"

# Leaderboard
Invoke-RestMethod -Uri http://localhost:8080/leaderboard

# Run the automated API smoke test
.\scripts\smoke-test-unity-api.ps1

# Run the Docker Evaluator diagnostics
.\scripts\test-docker-evaluator.ps1
```

## HTTP Endpoints

| Method | Path | Description |
|--------|------|-------------|
| GET | `/health` | Server status + queue size |
| POST | `/auth/login` | Mock login |
| POST | `/auth/register` | Mock register |
| POST | `/game/start` | Start a game session |
| POST | `/game/pause` | Pause current session |
| POST | `/game/resume` | Resume session |
| POST | `/game/end` | End session, get score |
| GET | `/challenge/current` | Pop next challenge from queue |
| POST | `/challenge/submit` | Submit code for evaluation |
| GET | `/leaderboard` | Get leaderboard (mock) |
| GET | `/friends` | Get friend list (mock) |
| GET | `/profile/stats` | Player stats |
| POST | `/sync` | Trigger data sync |

## Package Structure

```
com.synthex
├── Main.java                    # Entry point
├── api/
│   ├── LocalHttpServer.java     # JDK HttpServer wrapper
│   └── RouteHandler.java        # Endpoint routing
├── controller/
│   ├── Synthex.java             # Top-level orchestrator
│   ├── AuthController.java      # Login/register
│   ├── GameSessionController.java # Game logic
│   ├── ProgressController.java  # Session saving
│   └── SocialController.java    # Leaderboard/friends
├── ai/
│   ├── AiModel.java             # Interface
│   ├── OllamaPythonAiModel.java # Ollama implementation
│   ├── ObstacleGenerator.java   # Prompt builder
│   └── ChallengeQueueService.java # Queue lifecycle
├── challenge/
│   ├── Challenge.java           # Abstract base
│   ├── WritingChallenge.java    # Write-code challenges
│   ├── ErrorChallenge.java      # Find-error challenges
│   └── CodeObstacle.java        # FCFS queue (capacity 3)
├── evaluation/
│   ├── CodeEvaluator.java       # Interface
│   ├── MockCodeEvaluator.java   # Always-pass mock
│   ├── CodeSandbox.java         # Interface
│   └── EvaluationResult.java    # Result DTO
├── model/
│   ├── Player.java
│   ├── Account.java
│   ├── GameSession.java
│   ├── DifficultyLevel.java
│   ├── Submission.java
│   ├── WeaknessProfile.java
│   ├── PerformanceReport.java
│   ├── Leaderboard.java
│   └── FriendList.java
├── repository/
│   ├── LocalDBRepository.java   # Interface
│   ├── CloudDB.java             # Interface
│   ├── MockLocalDBRepository.java
│   └── MockCloudDB.java
├── dto/
│   ├── LoginRequest.java
│   ├── LoginResponse.java
│   ├── ChallengeResponse.java
│   ├── SubmitCodeRequest.java
│   ├── SubmitCodeResponse.java
│   ├── LeaderboardResponse.java
│   └── FriendListResponse.java
└── util/
    ├── JsonUtil.java
    └── AppConfig.java
```

## Unity Integration

For the Unity teammate — everything you need to connect the Unity frontend:

| Document | Description |
|----------|-------------|
| [docs/UNITY_API_CONTRACT.md](docs/UNITY_API_CONTRACT.md) | Full API contract — every endpoint, JSON shapes, mock flags |
| [docs/UNITY_INTEGRATION_STEPS.md](docs/UNITY_INTEGRATION_STEPS.md) | Step-by-step setup guide for Unity |
| [docs/unity-samples/SynthexApiClient.cs](docs/unity-samples/SynthexApiClient.cs) | Drop-in Unity C# API client — copy to `Assets/Scripts/` |

## How To Plug In Real Implementations

- **Real DB**: Implement `LocalDBRepository` interface → swap in `Synthex.java`
- **Real Cloud DB**: Implement `CloudDB` interface → swap in `Synthex.java`
- **Real Evaluator**: See [Evaluator Setup Guide](docs/EVALUATOR_SETUP.md). You can test your docker setup by running `.\scripts\test-docker-evaluator.ps1`.

## Recent updates (2026-05-06)

This project was updated to clarify the AI bridge behavior, expected Python output format, and how the Java backend handles fallbacks. The notes below summarize the important changes and where to look in the codebase.

### AI bridge / Ollama
- Implementation: `OllamaPythonAiModel` launches an external Python script (see [src/main/python/generate_problem.py](src/main/python/generate_problem.py)) using the command defined in `AppConfig.PYTHON_COMMAND` and `AppConfig.PYTHON_SCRIPT_PATH`.
- The Python bridge calls the model (Ollama by default) and prints structured metadata to stdout which the Java code parses.

### Expected Python script output (exact format)
- The Python script must print plain-text lines using `KEY=VALUE` pairs. The Java parser looks for these keys (exact prefixes):
    - `TYPE=` — `WRITING` or `ERROR`
    - `TITLE=` — short title for the challenge
    - `DESCRIPTION=` — single-line sanitized description
- Example successful output:

    TYPE=WRITING
    TITLE=Writing Challenge
    DESCRIPTION=Write a C++ function that reverses a string and returns it.

- Any order of lines is accepted. The full stdout is kept as `GeneratedData.rawOutput`.
- The script must exit with code `0` for the Java backend to treat it as success. A non-zero exit or empty stdout triggers the fallback path.

### AppConfig highlights
- `AppConfig.PYTHON_COMMAND` — default `{"py","-3"}` on Windows. These tokens are prepended when launching the Python bridge.
- `AppConfig.PYTHON_SCRIPT_PATH` — default `src/main/python/generate_problem.py`.
- `AppConfig.OLLAMA_MODEL` — default model name used by the Python bridge.

### Fallback behavior
- If the Python bridge fails or returns blank output, `OllamaPythonAiModel.fallback()` is used.
- The fallback attempts to select a static challenge from `StaticChallengeBank` (loaded from `/codebase.json` in resources). If none are available, a generic fallback text challenge is returned.

### How to run the Python bridge manually
Run the script directly (example):

```bash
# Windows (default AppConfig.PYTHON_COMMAND)
py -3 src/main/python/generate_problem.py "Generate one short WRITING C++ challenge. Difficulty=1."

# Or with an explicit python executable
python src/main/python/generate_problem.py "Generate one short ERROR C++ challenge. Difficulty=2."
```

### Where to look in the repo
- AI interface and implementation: [src/main/java/com/synthex/ai/AiModel.java](src/main/java/com/synthex/ai/AiModel.java) and [src/main/java/com/synthex/ai/OllamaPythonAiModel.java](src/main/java/com/synthex/ai/OllamaPythonAiModel.java)
- Prompt builder and queue: [src/main/java/com/synthex/ai/ObstacleGenerator.java](src/main/java/com/synthex/ai/ObstacleGenerator.java) and [src/main/java/com/synthex/ai/ChallengeQueueService.java](src/main/java/com/synthex/ai/ChallengeQueueService.java)
- Static fallback bank: [src/main/java/com/synthex/ai/StaticChallengeBank.java](src/main/java/com/synthex/ai/StaticChallengeBank.java)
- Python bridge script: [src/main/python/generate_problem.py](src/main/python/generate_problem.py)

---
- **Unity**:  HTTP endpoints above — see [Unity Integration docs](docs/UNITY_API_CONTRACT.md)
