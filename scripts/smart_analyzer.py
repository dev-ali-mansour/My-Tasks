import sys
import os
from google import genai

def analyze_code(diff_content):
    api_key = os.environ.get("GEMINI_API_KEY")
    if not api_key:
        print("Error: GEMINI_API_KEY not found.")
        sys.exit(1)

    client = genai.Client(api_key=api_key)

    # The magic is in this highly specific prompt
    prompt = f"""
    You are an expert Senior Android Engineer conducting a strict code review on the following Git diff.
    Your goal is to perform a 'Smart Static Analysis'. Ignore formatting and styling.

    Analyze the code for:
    1. Potential Memory Leaks: Look for leaked Contexts, uncancelled Coroutines, improper Flow collection (e.g., missing repeatOnLifecycle), or forgotten state cleanups in Jetpack Compose.
    2. Logic Flaws: Look for race conditions, improper error handling, or broken Unidirectional Data Flow / MVI state management.
    3. Missing Edge Cases: Look for unhandled nullability, empty lists, or configuration change issues.

    If you find issues, list them briefly with a suggestion for fixing them.
    If the code looks solid and safe, respond ONLY with: "✅ Smart Analysis Passed: No major memory or logic flaws detected."

    Git Diff:
    {diff_content}
    """

    try:
        # Using Gemini 2.5 Flash for fast, deep context reasoning
        response = client.models.generate_content(
            model='gemini-2.5-flash',
            contents=prompt,
        )
        print(response.text)
    except Exception as e:
        print(f"Error during analysis: {e}")
        sys.exit(1)

if __name__ == "__main__":
    diff = sys.stdin.read()
    analyze_code(diff)