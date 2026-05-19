using System;
using System.Collections;
using System.Text;
using UnityEngine;
using UnityEngine.Networking;

/// <summary>
/// Synthex Backend API Client for Unity.
/// Attach this MonoBehaviour to an empty GameObject named "BackendClient".
/// 
/// Usage:
///   - Set baseUrl in the Inspector if not using localhost.
///   - Call methods via StartCoroutine from your UI scripts.
///   - Example: StartCoroutine(FindObjectOfType<SynthexApiClient>().HealthCheck());
/// 
/// No external packages required — uses only UnityEngine.Networking.
/// </summary>
public class SynthexApiClient : MonoBehaviour
{
    [Header("Backend Configuration")]
    [Tooltip("Base URL of the Synthex Java backend")]
    public string baseUrl = "http://localhost:8080";

    // ────────────────────────────────────────────────────────────────────────
    //  Serializable Request Classes (for JSON serialization with JsonUtility)
    // ────────────────────────────────────────────────────────────────────────

    [Serializable]
    public class LoginRequest
    {
        public string username;
        public string password;
    }

    [Serializable]
    public class RegisterRequest
    {
        public string username;
        public string password;
    }

    [Serializable]
    public class SubmitRequest
    {
        public string challengeId;
        public string submittedCode;
        public float timeTaken;
    }

    // ────────────────────────────────────────────────────────────────────────
    //  Callback Delegates
    // ────────────────────────────────────────────────────────────────────────

    /// <summary>Generic callback with raw JSON response string.</summary>
    public delegate void ApiCallback(bool success, string jsonResponse);

    // ────────────────────────────────────────────────────────────────────────
    //  Public API Methods
    // ────────────────────────────────────────────────────────────────────────

    /// <summary>
    /// GET /health — Check if the backend is reachable.
    /// Call this on app launch to verify connectivity.
    /// </summary>
    public IEnumerator HealthCheck(ApiCallback callback = null)
    {
        string url = baseUrl + "/health";
        Debug.Log("[SynthexAPI] GET " + url);

        using (UnityWebRequest request = UnityWebRequest.Get(url))
        {
            yield return request.SendWebRequest();

            if (request.result != UnityWebRequest.Result.Success)
            {
                Debug.LogError("[SynthexAPI] HealthCheck FAILED: " + request.error);
                callback?.Invoke(false, null);
            }
            else
            {
                string json = request.downloadHandler.text;
                Debug.Log("[SynthexAPI] HealthCheck OK: " + json);
                callback?.Invoke(true, json);
            }
        }
    }

    /// <summary>
    /// POST /auth/login — Authenticate a player.
    /// Returns JSON with: success (bool), playerId (string), message (string).
    /// </summary>
    public IEnumerator Login(string username, string password, ApiCallback callback = null)
    {
        string url = baseUrl + "/auth/login";
        Debug.Log("[SynthexAPI] POST " + url + " user=" + username);

        LoginRequest body = new LoginRequest
        {
            username = username,
            password = password
        };
        string jsonBody = JsonUtility.ToJson(body);

        using (UnityWebRequest request = PostJson(url, jsonBody))
        {
            yield return request.SendWebRequest();

            if (request.result != UnityWebRequest.Result.Success)
            {
                Debug.LogError("[SynthexAPI] Login FAILED: " + request.error);
                callback?.Invoke(false, null);
            }
            else
            {
                string json = request.downloadHandler.text;
                Debug.Log("[SynthexAPI] Login OK: " + json);
                callback?.Invoke(true, json);
            }
        }
    }

    /// <summary>
    /// POST /auth/register — Register a new player.
    /// Returns JSON with: success (bool), playerId (string), message (string).
    /// Note: The backend currently ignores the email field.
    /// </summary>
    public IEnumerator Register(string username, string password, string email, ApiCallback callback = null)
    {
        string url = baseUrl + "/auth/register";
        Debug.Log("[SynthexAPI] POST " + url + " user=" + username);

        // Backend uses LoginRequest (username + password only).
        // email is accepted here for future compatibility but not sent yet.
        RegisterRequest body = new RegisterRequest
        {
            username = username,
            password = password
        };
        string jsonBody = JsonUtility.ToJson(body);

        using (UnityWebRequest request = PostJson(url, jsonBody))
        {
            yield return request.SendWebRequest();

            if (request.result != UnityWebRequest.Result.Success)
            {
                Debug.LogError("[SynthexAPI] Register FAILED: " + request.error);
                callback?.Invoke(false, null);
            }
            else
            {
                string json = request.downloadHandler.text;
                Debug.Log("[SynthexAPI] Register OK: " + json);
                callback?.Invoke(true, json);
            }
        }
    }

    /// <summary>
    /// POST /game/start — Start a new game session.
    /// Returns JSON with: sessionId (string), status (string).
    /// </summary>
    public IEnumerator StartGame(ApiCallback callback = null)
    {
        string url = baseUrl + "/game/start";
        Debug.Log("[SynthexAPI] POST " + url);

        using (UnityWebRequest request = PostJson(url, "{}"))
        {
            yield return request.SendWebRequest();

            if (request.result != UnityWebRequest.Result.Success)
            {
                Debug.LogError("[SynthexAPI] StartGame FAILED: " + request.error);
                callback?.Invoke(false, null);
            }
            else
            {
                string json = request.downloadHandler.text;
                Debug.Log("[SynthexAPI] StartGame OK: " + json);
                callback?.Invoke(true, json);
            }
        }
    }

    /// <summary>
    /// GET /challenge/current — Get the next coding challenge from the queue.
    /// Returns JSON with: id, type, difficulty, title, description, code, topic, hint.
    /// If type == "NONE", the queue is empty — retry after a short delay.
    /// </summary>
    public IEnumerator GetCurrentChallenge(ApiCallback callback = null)
    {
        string url = baseUrl + "/challenge/current";
        Debug.Log("[SynthexAPI] GET " + url);

        using (UnityWebRequest request = UnityWebRequest.Get(url))
        {
            yield return request.SendWebRequest();

            if (request.result != UnityWebRequest.Result.Success)
            {
                Debug.LogError("[SynthexAPI] GetCurrentChallenge FAILED: " + request.error);
                callback?.Invoke(false, null);
            }
            else
            {
                string json = request.downloadHandler.text;
                Debug.Log("[SynthexAPI] Challenge received: " + json);
                callback?.Invoke(true, json);
            }
        }
    }

    /// <summary>
    /// POST /challenge/submit — Submit code for evaluation.
    /// 
    /// Parameters:
    ///   challengeId — The "id" field from the challenge response.
    ///   submittedCode — The player's code answer.
    ///   timeTaken — Time spent.
    /// 
    /// Returns JSON with: success (bool), isCorrect (bool), resultType (string), message (string), scoreDelta (int), speedDelta (float).
    /// </summary>
    public IEnumerator SubmitCode(string challengeId, string submittedCode, float timeTaken, ApiCallback callback = null)
    {
        string url = baseUrl + "/challenge/submit";
        Debug.Log("[SynthexAPI] POST " + url + " challengeId=" + challengeId);

        // SubmitRequest maps directly to backend DTO
        SubmitRequest body = new SubmitRequest
        {
            challengeId = challengeId,
            submittedCode = submittedCode,
            timeTaken = timeTaken
        };
        string jsonBody = JsonUtility.ToJson(body);

        using (UnityWebRequest request = PostJson(url, jsonBody))
        {
            yield return request.SendWebRequest();

            if (request.result != UnityWebRequest.Result.Success)
            {
                Debug.LogError("[SynthexAPI] SubmitCode FAILED: " + request.error);
                callback?.Invoke(false, null);
            }
            else
            {
                string json = request.downloadHandler.text;
                Debug.Log("[SynthexAPI] Submit result: " + json);
                callback?.Invoke(true, json);
            }
        }
    }

    /// <summary>
    /// GET /leaderboard — Fetch the global leaderboard.
    /// Returns JSON with: entries[] (username, score, rank).
    /// </summary>
    public IEnumerator GetLeaderboard(ApiCallback callback = null)
    {
        string url = baseUrl + "/leaderboard";
        Debug.Log("[SynthexAPI] GET " + url);

        using (UnityWebRequest request = UnityWebRequest.Get(url))
        {
            yield return request.SendWebRequest();

            if (request.result != UnityWebRequest.Result.Success)
            {
                Debug.LogError("[SynthexAPI] GetLeaderboard FAILED: " + request.error);
                callback?.Invoke(false, null);
            }
            else
            {
                string json = request.downloadHandler.text;
                Debug.Log("[SynthexAPI] Leaderboard: " + json);
                callback?.Invoke(true, json);
            }
        }
    }

    /// <summary>
    /// GET /friends — Fetch the player's friend list.
    /// Returns JSON with: friends[] (username, online), pendingRequests[].
    /// </summary>
    public IEnumerator GetFriends(ApiCallback callback = null)
    {
        string url = baseUrl + "/friends";
        Debug.Log("[SynthexAPI] GET " + url);

        using (UnityWebRequest request = UnityWebRequest.Get(url))
        {
            yield return request.SendWebRequest();

            if (request.result != UnityWebRequest.Result.Success)
            {
                Debug.LogError("[SynthexAPI] GetFriends FAILED: " + request.error);
                callback?.Invoke(false, null);
            }
            else
            {
                string json = request.downloadHandler.text;
                Debug.Log("[SynthexAPI] Friends: " + json);
                callback?.Invoke(true, json);
            }
        }
    }

    /// <summary>
    /// GET /profile/stats — Fetch the current player's stats.
    /// Returns JSON with: playerId, currentScore, queueSize, status.
    /// </summary>
    public IEnumerator GetProfileStats(ApiCallback callback = null)
    {
        string url = baseUrl + "/profile/stats";
        Debug.Log("[SynthexAPI] GET " + url);

        using (UnityWebRequest request = UnityWebRequest.Get(url))
        {
            yield return request.SendWebRequest();

            if (request.result != UnityWebRequest.Result.Success)
            {
                Debug.LogError("[SynthexAPI] GetProfileStats FAILED: " + request.error);
                callback?.Invoke(false, null);
            }
            else
            {
                string json = request.downloadHandler.text;
                Debug.Log("[SynthexAPI] ProfileStats: " + json);
                callback?.Invoke(true, json);
            }
        }
    }

    // ────────────────────────────────────────────────────────────────────────
    //  Helper: Create a POST request with JSON body
    // ────────────────────────────────────────────────────────────────────────

    private UnityWebRequest PostJson(string url, string jsonBody)
    {
        byte[] bodyRaw = Encoding.UTF8.GetBytes(jsonBody);

        UnityWebRequest request = new UnityWebRequest(url, "POST");
        request.uploadHandler = new UploadHandlerRaw(bodyRaw);
        request.downloadHandler = new DownloadHandlerBuffer();
        request.SetRequestHeader("Content-Type", "application/json");

        return request;
    }
}
