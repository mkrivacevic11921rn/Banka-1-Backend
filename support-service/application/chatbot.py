from google import genai
from google.genai import types
import pathlib
import httpx
from dotenv import load_dotenv
import os

load_dotenv()

client = genai.Client(api_key=os.getenv("GOOGLE_API"))

# Move this test code into a __name__ == "__main__" block to prevent execution when imported
if __name__ == "__main__":
    response = client.models.generate_content(
        model="gemini-2.0-flash",
        contents="Explain how AI works",
    )
    print(response.text)

# Define system prompt for consistent personality

context_txt_path = "ctx.txt" 
context = ""
with open(context_txt_path, "r") as file:
    context = file.read()

SYSTEM_PROMPT = f"""
You are a helpful and professional banking assistant named BankBot. 
Your role is to provide clear, accurate information about banking services and assist customers with their queries.

Please follow these guidelines:
- Be polite, concise, and helpful in your responses
- Use professional but friendly language
- When you don't know an answer, acknowledge it and offer to connect the customer with a human representative
- Never share sensitive customer information or ask for PINs, passwords, or full card numbers
- For account-specific questions, explain the steps to find the information rather than pretending to have access to accounts
- Always prioritize security and privacy in your responses

You work for Example Bank, which offers checking accounts, savings accounts, loans, credit cards, and investment services.

The context you need for this is {context}
"""

def customer_service_response(prompt):
    # For testing without the PDF context
    response = client.models.generate_content(
        model="gemini-2.0-flash",
        contents= SYSTEM_PROMPT + " The user asked this: " + prompt,
    )
    return response.text

# Commented function for future implementation with PDF context
# def customer_service_response_with_context(prompt):
#     #  TODO Remove comment once context is added.
#     # pdf_path = "app/documents/context.pdf"
#     # file_path = pathlib.Path(pdf_path)
#     # file_path.write_bytes(httpx.get(pdf_path).content)
#     # sample_file = client.files.upload(file=file_path)
#     # response = client.models.generate_content(
#     #     model="gemini-2.0-flash",
#     #     contents=[sample_file, {"role": "system", "parts": [SYSTEM_PROMPT]}, {"role": "user", "parts": [prompt]}]
#     # )
#     # return response.text