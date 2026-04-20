import os
import sys
from google import genai

def review_pr(diff_content):
    api_key = os.environ.get("GEMINI_API_KEY")
    if not api_key:
        print("Error: GEMINI_API_KEY not found.")
        sys.exit(1)

    # Initialize the new client
    client = genai.Client(api_key=api_key)

    prompt = f"""
    You are a Senior Android Engineer reviewing a Pull Request.
    Analyze the following git diff and provide a short, professional summary of the changes.
    Point out if the tests align with the logic. Keep it under 4 sentences.

    Git Diff:
    {diff_content}
    """

    try:
        # Use the updated SDK method
        response = client.models.generate_content(
            model='gemini-2.5-flash',
            contents=prompt,
        )
        print(response.text)
    except Exception as e:
        print(f"Error generating content: {e}")
        sys.exit(1)

if __name__ == "__main__":
    # Read the diff from standard input
    diff = sys.stdin.read()
    review_pr(diff)