import os
import json
import time
import subprocess
import requests
import schedule
import sys
import shutil
sys.stdout.reconfigure(encoding='utf-8')
from datetime import datetime
from dotenv import load_dotenv, find_dotenv

BASE_DIR = os.path.dirname(os.path.abspath(__file__))
PARENT_DIR = os.path.dirname(BASE_DIR)

load_dotenv(os.path.join(PARENT_DIR, ".env"))
load_dotenv(os.path.join(BASE_DIR, ".env"))
load_dotenv(find_dotenv())

CHANNELS = {
    "paresh_goswami": "UCoCCNfFug9Q5NLXxyZVFasQ",
    "ambalal_patel": "UCI7K8N8bGdRnY0ANgH6ccVw",
    "jagat_tat": "UCxPtL1U71ZY6ZkuMzLK2iTQ"
}

GROQ_API_KEY = os.getenv("GROQ_API_KEY")

if not GROQ_API_KEY:
    print("WARNING: GROQ_API_KEY is not set in environment variables! Audio transcription will fail.", flush=True)
else:
    print(f"GROQ_API_KEY is loaded successfully! ({len(GROQ_API_KEY)} chars)", flush=True)

GROQ_URL = "https://api.groq.com/openai/v1/chat/completions"
GROQ_MODEL = "qwen/qwen3.8-27b"

if os.path.exists(os.path.join(PARENT_DIR, "src")):
    OUTPUT_FILE = os.path.join(PARENT_DIR, "youtube_consensus.json")
    STATUS_FILE = os.path.join(PARENT_DIR, "youtube_status.json")
else:
    OUTPUT_FILE = os.path.join(BASE_DIR, "..", "youtube_consensus.json")
    STATUS_FILE = os.path.join(BASE_DIR, "..", "youtube_status.json")

def get_latest_video_info(channel_id):
    import urllib.request
    import xml.etree.ElementTree as ET
    from datetime import datetime, timezone
    
    url = f"https://www.youtube.com/feeds/videos.xml?channel_id={channel_id}"
    try:
        req = urllib.request.Request(url, headers={'User-Agent': 'Mozilla/5.0'})
        response = urllib.request.urlopen(req).read()
        root = ET.fromstring(response)
        
        ns = {'yt': 'http://www.youtube.com/xml/schemas/2015', 'atom': 'http://www.w3.org/2005/Atom'}
        entry = root.find('atom:entry', ns)
        if entry is None:
            return None, False
            
        video_id = entry.find('yt:videoId', ns).text
        published_str = entry.find('atom:published', ns).text
        
        published_date = datetime.fromisoformat(published_str)
        now = datetime.now(timezone.utc)
        
        is_live = (now - published_date).total_seconds() < 86400
        
        print(f"Found latest video {video_id} for {channel_id}. Live: {is_live}", flush=True)
        return video_id, is_live
    except Exception as e:
        print(f"Failed to fetch RSS for {channel_id}: {e}", flush=True)
        return None, False

def get_transcript(video_id):
    print(f"Fetching audio for {video_id} to transcribe with Groq Whisper AI...", flush=True)
    try:
        try:
            import imageio_ffmpeg
            ffmpeg_path = imageio_ffmpeg.get_ffmpeg_exe()
        except Exception:
            ffmpeg_path = shutil.which("ffmpeg") or "ffmpeg"
            
        audio_file = os.path.join(BASE_DIR, f"{video_id}.mp3")
        if os.path.exists(audio_file):
            try:
                os.remove(audio_file)
            except Exception:
                pass

        subprocess.run(
            [
                sys.executable, "-m", "yt_dlp", 
                "-x", "--audio-format", "mp3", 
                "--ffmpeg-location", ffmpeg_path, 
                "--playlist-end", "1", 
                "-o", audio_file, 
                f"https://www.youtube.com/watch?v={video_id}"
            ],
            check=True, capture_output=True
        )
        
        if os.path.exists(audio_file):
            file_size_mb = os.path.getsize(audio_file) / (1024 * 1024)
            if file_size_mb > 24:
                print(f"Skipping {video_id} because audio is too large ({file_size_mb:.2f} MB)", flush=True)
                os.remove(audio_file)
                return None
                
        with open(audio_file, "rb") as f:
            file_bytes = f.read()
            
        res = requests.post(
            "https://api.groq.com/openai/v1/audio/transcriptions",
            headers={"Authorization": f"Bearer {GROQ_API_KEY}"},
            files={"file": ("audio.mp3", file_bytes, "audio/mpeg")},
            data={"model": "whisper-large-v3-turbo", "response_format": "json"}
        )
            
        if os.path.exists(audio_file):
            try:
                os.remove(audio_file)
            except Exception:
                pass
            
        if res.status_code == 200:
            print(f"Groq Whisper successfully transcribed {video_id}", flush=True)
            return res.json().get("text", "")
        else:
            print(f"Groq Whisper error: {res.text}", flush=True)
            return None
    except subprocess.CalledProcessError as ex:
        error_msg = ex.stderr.decode('utf-8', errors='replace') if ex.stderr else 'No stderr'
        print(f"Groq Whisper transcription failed (yt-dlp error): {ex}\nSTDERR: {error_msg}", flush=True)
        return None
    except Exception as ex:
        print(f"Groq Whisper transcription failed: {ex}", flush=True)
        return None

def summarize_transcript(name, text):
    if not text:
        return "માહિતી ઉપલબ્ધ નથી."

    if len(text) > 2500:
        text = text[:2500] + "... (truncated)"

    prompt = f"નીચે {name} નો વિડીયો છે. આ વિડીયોમાંથી હવામાનની આગાહી અને ખેતીની ટિપ્સ (૨ થી ૩ વાક્યોમાં ટૂંકમાં) કાઢો. જો વિડીયોમાં હવામાન સિવાય અન્ય વાત હોય તો સ્પષ્ટ જણાવો:\n\n{text}"

    headers = {
        "Authorization": f"Bearer {GROQ_API_KEY}",
        "Content-Type": "application/json"
    }
    response = requests.post(GROQ_URL, headers=headers, json={
        "model": GROQ_MODEL,
        "messages": [{"role": "user", "content": prompt}],
        "temperature": 0.1,
        "max_tokens": 300
    })

    if response.status_code == 200:
        return response.json().get("choices", [{}])[0].get("message", {}).get("content", "")
    print(f"Error in summarize_transcript for {name}: {response.status_code} - {response.text}", flush=True)
    return "માહિતી ઉપલબ્ધ નથી."

def analyze_with_ollama(paresh_text, ambalal_text, jagat_text):
    print("Summarizing Paresh Goswami...", flush=True)
    paresh_summary = summarize_transcript("પરેશ ગોસ્વામી", paresh_text)

    print("Waiting 5 seconds for Groq rate limit...", flush=True)
    time.sleep(5)

    print("Summarizing Ambalal Patel...", flush=True)
    ambalal_summary = summarize_transcript("અંબાલાલ પટેલ", ambalal_text)

    print("Waiting 5 seconds for Groq rate limit...", flush=True)
    time.sleep(5)

    print("Summarizing Jagat Tat...", flush=True)
    jagat_summary = summarize_transcript("જગત તાત", jagat_text)

    print("Waiting 5 seconds for Groq rate limit...", flush=True)
    time.sleep(5)

    print("Generating final JSON...", flush=True)
    prompt = f"""
    You are an expert Gujarati agricultural advisor. Summarize the following expert inputs into valid JSON:
    પરેશ ગોસ્વામી: {paresh_summary}
    અંબાલાલ પટેલ: {ambalal_summary}
    જગત તાત: {jagat_summary}

    Output JSON with these exact keys. All text in Gujarati, 1-2 concise sentences per field:
    {{
        "pareshForecast": "પરેશ ગોસ્વામીની આગાહી",
        "ambalalForecast": "અંબાલાલ પટેલની આગાહી",
        "combinedWeatherAlert": "બધા નિષ્ણાતોના મતે મુખ્ય એલર્ટ",
        "agreementPercentage": "૭૫%",
        "farmingTips": ["ખેતી ટિપ ૧", "ખેતી ટિપ ૨"]
    }}
    """

    try:
        headers = {
            "Authorization": f"Bearer {GROQ_API_KEY}",
            "Content-Type": "application/json"
        }
        response = requests.post(GROQ_URL, headers=headers, json={
            "model": GROQ_MODEL,
            "messages": [{"role": "user", "content": prompt}],
            "temperature": 0.1,
            "max_tokens": 600
        })

        if response.status_code == 200:
            result = response.json()
            response_text = result.get("choices", [{}])[0].get("message", {}).get("content", "")

            import re
            json_match = re.search(r"\{.*\}", response_text, re.DOTALL)
            if json_match:
                response_text = json_match.group(0)

            try:
                return json.loads(response_text)
            except Exception as e:
                print(f"Failed to parse JSON: {e}", flush=True)
                print(f"Raw text: {response_text}", flush=True)
                return None
        else:
            print(f"Groq API Error: {response.status_code} - {response.text}", flush=True)
            return None
    except Exception as e:
        print(f"Error calling Groq or parsing JSON: {e}", flush=True)
        return None

def update_status(is_running, progress=0, message=""):
    try:
        data = {
            "isRunning": is_running,
            "progress": progress,
            "progressMessage": message
        }
        with open(STATUS_FILE, "w", encoding="utf-8") as f:
            json.dump(data, f, ensure_ascii=False)
        try:
            alt_status = os.path.join(BASE_DIR, "youtube_status.json")
            with open(alt_status, "w", encoding="utf-8") as f:
                json.dump(data, f, ensure_ascii=False)
        except Exception:
            pass
    except Exception as e:
        print(f"Failed to update status: {e}", flush=True)

def run_job():
    try:
        update_status(True, 10, "પ્રારંભ થઈ રહ્યું છે (Initializing)...")
        print(f"[{datetime.now()}] Starting YouTube analysis job...", flush=True)

        update_status(True, 20, "લેટેસ્ટ વિડીયો શોધી રહ્યા છીએ (Finding latest videos)...")
        paresh_vid, paresh_live = get_latest_video_info(CHANNELS["paresh_goswami"])
        ambalal_vid, ambalal_live = get_latest_video_info(CHANNELS["ambalal_patel"])
        jagat_vid, jagat_live = get_latest_video_info(CHANNELS["jagat_tat"])

        update_status(True, 40, "વિડીયોનું લખાણ ડાઉનલોડ થઈ રહ્યું છે (Downloading transcripts)...")
        paresh_text = get_transcript(paresh_vid) if paresh_vid else None
        ambalal_text = get_transcript(ambalal_vid) if ambalal_vid else None
        jagat_text = get_transcript(jagat_vid) if jagat_vid else None

        if paresh_text or ambalal_text or jagat_text:
            update_status(True, 60, "AI પૃથ્થકરણ ચાલુ છે (AI Analysis in progress)...")
            print("Transcripts fetched. Calling analysis...", flush=True)
            analysis = analyze_with_ollama(paresh_text, ambalal_text, jagat_text)

            if analysis:
                update_status(True, 95, "ફાઇનલ ડેટા સેવ થઈ રહ્યો છે (Saving final data)...")
                analysis["lastUpdated"] = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
                analysis["pareshIsLive"] = paresh_live
                analysis["ambalalIsLive"] = ambalal_live
                analysis["jagatIsLive"] = jagat_live

                if "agreementPercentage" not in analysis:
                    analysis["agreementPercentage"] = "85%"

                with open(OUTPUT_FILE, 'w', encoding='utf-8') as f:
                    json.dump(analysis, f, ensure_ascii=False, indent=4)
                print(f"Analysis saved to {OUTPUT_FILE}", flush=True)
            else:
                print("Failed to get valid analysis.", flush=True)
        else:
            print("No transcripts could be fetched today.", flush=True)
    finally:
        update_status(False, 100, "પૂર્ણ થયું (Completed)")

if __name__ == "__main__":
    print("YouTube Weather Analyzer Service Started...", flush=True)
    run_job()
    schedule.every(6).hours.do(run_job)
    while True:
        schedule.run_pending()
        time.sleep(60)
