import json
import sys

with open(r"C:\Users\Jay\.gemini\antigravity\brain\34160676-5bc4-40b8-85da-221bca2c29cd\.system_generated\logs\transcript.jsonl", "r", encoding="utf-8") as f:
    lines = f.readlines()

for line in lines:
    try:
        data = json.loads(line)
        if data.get("type") == "USER_INPUT":
            content = data.get("content", "")
            if "market" in content.lower() or "yard" in content.lower() or "price" in content.lower() or "mandi" in content.lower() or "agmarknet" in content.lower() or "data.gov.in" in content.lower():

                print(f"[{data.get('created_at')}] {content[:200].strip()}...")
    except:
        pass
