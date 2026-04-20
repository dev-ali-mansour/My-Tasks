import os
import sys
import google.generativeai as genai

def review_pr(diff_content):
    api_key = os.environ.get("GEMINI_API_KEY")
    if not api_key:
        print("Error: GEMINI_API_KEY not found.")
        sys.exit(1)

    genai.configure(api_key=api_key)

    # Use Gemini 1.5 Flash for fast text processing
    model = genai.GenerativeModel('gemini-1.5-flash')

    prompt = f"""
    You are a Senior Android Engineer reviewing a Pull Request. 
    Analyze the following git diff and provide a short, professional summary of the changes.
    Point out if the tests align with the logic. Keep it under 4 sentences.
    
    Git Diff:
    {diff_content}
    """

    response = model.generate_content(prompt)
    print(response.text)

if __name__ == "__main__":
    # Read the diff from standard input
    diff = sys.stdin.read()
    review_pr(diff)