import json
import os
import sys
import urllib.request

OLLAMA_URL = "----"
MODEL_NAME = os.environ.get("OLLAMA_MODEL", "qwen2.5-coder:3b")

def to_str(val):
    """Force any value into a string."""
    if isinstance(val, str):
        return val
    if isinstance(val, dict):
        return json.dumps(val, indent=2)
    if isinstance(val, list):
        return "\n".join(str(item) for item in val)
    return str(val)

def main():
    prompt_text = sys.argv[1] if len(sys.argv) > 1 else ""
    is_error_type = "bug" in prompt_text.lower() or "error" in prompt_text.lower()

    payload = {
        "model": MODEL_NAME,
        "prompt": prompt_text,
        "stream": False,
        "format": "json"
    }

    try:
        req = urllib.request.Request(
            OLLAMA_URL,
            data=json.dumps(payload).encode("utf-8"),
            headers={"Content-Type": "application/json"},
            method="POST"
        )
        with urllib.request.urlopen(req, timeout=120) as resp:
            res_data = json.loads(resp.read().decode("utf-8"))

        raw = res_data.get("response", "").strip()
        data = json.loads(raw)

        # Handle different JSON structures
        snippet = data
        if "snippets" in data and isinstance(data["snippets"], list):
            snippet = data["snippets"][0]

        title = to_str(snippet.get("title", "C++ Challenge"))[:50]
        code = to_str(snippet.get("code", ""))
        expected_output = to_str(snippet.get("expected_output", ""))

        if not code.strip():
            for key in ["snippet", "source", "program", "content"]:
                if key in snippet:
                    code = to_str(snippet[key])
                    break

        # Extract title from first comment if generic
        if title in ("C++ Challenge", "") and code:
            for line in code.split("\n"):
                line = line.strip()
                if line.startswith("//"):
                    extracted = line.lstrip("/").strip()
                    if len(extracted) > 5:
                        title = extracted[:50]
                        break

        problem_type = "Error Identification" if is_error_type else "Writing"
        print(f"TYPE={problem_type}")
        print(f"TITLE={title}")
        print(f"EXPECTED_OUTPUT={expected_output}")
        print(f"DESCRIPTION={code}")

    except Exception as e:
        raw_preview = ""
        try:
            raw_preview = raw[:300]
        except:
            pass
        print(f"TYPE=ERROR")
        print(f"TITLE=Generation Error")
        print(f"EXPECTED_OUTPUT=")
        print(f"DESCRIPTION=Error: {str(e)} | Raw: {raw_preview}")
        sys.exit(1)

if __name__ == "__main__":
    main()
