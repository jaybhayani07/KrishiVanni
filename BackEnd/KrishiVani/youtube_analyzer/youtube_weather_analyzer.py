import os
import json
import time
import subprocess
import requests
import schedule
import sys
sys.stdout.reconfigure(encoding='utf-8')
from datetime import datetime
from youtube_transcript_api import YouTubeTranscriptApi
from dotenv import load_dotenv

load_dotenv()

CHANNELS = {
    "paresh_goswami": "https://www.youtube.com/@pareshgoswami",
    "ambalal_patel": "https://www.youtube.com/@AmbalalPatelOfficial",
    "jagat_tat": "https://www.youtube.com/@JagatTat"
}

GROQ_API_KEY = os.getenv("GROQ_API_KEY")
if not GROQ_API_KEY:
    print("Warning: GROQ_API_KEY not found in .env file")

GROQ_URL = "https://api.groq.com/openai/v1/chat/completions"
GROQ_MODEL = "llama-3.3-70b-versatile"

OUTPUT_FILE = r"../youtube_consensus.json"

def get_latest_video_info(channel_url):
    try:

        result = subprocess.run(
            [sys.executable, "-m", "yt_dlp", "--print", "id", "--print", "upload_date", "--playlist-end", "1", channel_url],
            capture_output=True, text=True, check=True
        )
        lines = result.stdout.strip().split('\n')
        video_id = lines[0]
        upload_date_str = lines[1] if len(lines) > 1 else ""


        from datetime import timedelta
        now = datetime.now()
        today_str = now.strftime("%Y%m%d")
        yesterday_str = (now - timedelta(days=1)).strftime("%Y%m%d")

        is_live = False
        if upload_date_str == today_str:
            is_live = True
        elif upload_date_str == yesterday_str and now.hour < 20:
            is_live = True

        return video_id, is_live
    except Exception as e:
        print(f"Error fetching latest video for {channel_url}: {e}")
        return None, False

def get_transcript(video_id):
    try:

        api = YouTubeTranscriptApi()
        transcript_list = api.list(video_id)
        transcript = transcript_list.find_transcript(['gu', 'hi', 'en'])
        data = transcript.fetch()
        text = " ".join([getattr(t, 'text', '') for t in data])
        return text
    except Exception as e:
        print(f"Error fetching transcript for {video_id}: {e}")
        return None

def summarize_transcript(name, text):
    if not text:
        return "માહિતી ઉપલબ્ધ નથી."

    if len(text) > 2500:
        text = text[:2500] + "... (truncated)"

    prompt = f"નીચે {name} નો વિડીયો છે. આ વિડીયોમાંથી હવામાનની આગાહી અને ખેતીની ટિપ્સ (લગભગ ૩ થી ૫ વાક્યોમાં) કાઢો. જો વિડીયોમાં માત્ર El Nino કે લાંબા ગાળાની વાત હોય, તો સ્પષ્ટ જણાવો અને જાતે કોઈ આગાહી ન બનાવો:\n\n{text}"

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
    print(f"Error in summarize_transcript for {name}: {response.status_code} - {response.text}")
    return "માહિતી ઉપલબ્ધ નથી."

def analyze_with_ollama(paresh_text, ambalal_text, jagat_text):
    print("Summarizing Paresh Goswami...")
    paresh_summary = summarize_transcript("પરેશ ગોસ્વામી", paresh_text)

    print("Waiting 60 seconds for Groq rate limit...")
    time.sleep(60)

    print("Summarizing Ambalal Patel...")
    ambalal_summary = summarize_transcript("અંબાલાલ પટેલ", ambalal_text)

    print("Waiting 60 seconds for Groq rate limit...")
    time.sleep(60)

    print("Summarizing Jagat Tat...")
    jagat_summary = summarize_transcript("જગત તાત", jagat_text)

    print("Waiting 60 seconds for Groq rate limit...")
    time.sleep(60)

    print("Generating final JSON...")
    prompt = f"""
    You are an expert Gujarati agricultural advisor. નીચે ગુજરાતના ત્રણ પ્રખ્યાત નિષ્ણાતોના વિડીયોનો ટૂંકો સાર (summary) આપેલો છે.

    પરેશ ગોસ્વામી: {paresh_summary}
    અંબાલાલ પટેલ: {ambalal_summary}
    જગત તાત: {jagat_summary}

    તમારે આ માહિતીનું વિશ્લેષણ કરીને નીચે મુજબ JSON ફોર્મેટમાં જ જવાબ આપવાનો છે (બીજું કોઈ લખાણ નહિ):
    {{
        "pareshForecast": "પરેશ ગોસ્વામીની હવામાન અંગેની મુખ્ય આગાહી (૫-૬ વાક્યોમાં ખૂબ જ વિગતવાર).",
        "ambalalForecast": "અંબાલાલ પટેલની હવામાન અંગેની મુખ્ય આગાહી (૫-૬ વાક્યોમાં ખૂબ જ વિગતવાર).",
        "combinedWeatherAlert": "બધા નિષ્ણાતોના મતે હવામાન કેવું રહેશે? (૫-૬ વાક્યોમાં વિસ્તૃત રીતે).",
        "agreementPercentage": "બધા નિષ્ણાતોના મંતવ્યો કેટલા ટકા સમાન છે?",
        "farmingTips": ["ટિપ 1", "ટિપ 2"]
    }}
    અત્યંત મહત્વની સૂચના 1: જો કોઈ નિષ્ણાતનો સારાંશ "માહિતી ઉપલબ્ધ નથી." હોય, તો તેની આગાહીમાં તમારી જાતે કોઈ માહિતી બનાવવી નહિ.
    અત્યંત મહત્વની સૂચના 2: જો વિડીયોમાં ટૂંકા ગાળાની (Short-term) આગાહી ન હોય અને માત્ર લાંબા ગાળાની (El Nino, IOD વગેરે) માહિતી હોય, તો સ્પષ્ટ જણાવો કે વિડીયોમાં માત્ર લાંબા ગાળાની ચર્ચા છે, તમારી જાતે કોઈ ખોટો વરસાદ કે આગાહી ન બનાવો.
    અત્યંત મહત્વની સૂચના 3: farmingTips એ માત્ર વાક્યો (સ્ટ્રિંગ) નું લિસ્ટ હોવું જોઈએ. જો વિડીયોમાં કોઈ ખેતીની ટિપ્સ ન હોય તો સામાન્ય ઉપયોગી ટિપ્સ આપો.
    દરેક ટિપ ૪-૫ વાક્યો લાંબી હોવી જોઈએ. જો તમારી પાસે માહિતી ઓછી હોય, તો ખેડૂતોને ઉપયોગી થાય તેવી સામાન્ય સમજૂતી ઉમેરીને પણ દરેક ટિપ લાંબી બનાવો.(લગભગ ૪૦-૫૦ શબ્દો). ફક્ત ૨ જ સૌથી મહત્વની ટિપ્સ આપો.
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
            "max_tokens": 2000,
            "response_format": {"type": "json_object"}
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
                print(f"Failed to parse JSON: {e}")
                print(f"Raw text: {response_text}")
                return None
        else:
            print(f"Groq API Error: {response.status_code} - {response.text}")
            return None
    except Exception as e:
        print(f"Error calling Groq or parsing JSON: {e}")
        return None

def update_status(is_running, progress=0, message=""):
    status_file = r"../youtube_status.json"
    import json
    try:
        with open(status_file, "w", encoding="utf-8") as f:
            json.dump({
                "isRunning": is_running,
                "progress": progress,
                "progressMessage": message
            }, f, ensure_ascii=False)
    except Exception as e:
        print(f"Failed to update status: {e}")

def run_job():
    try:
        update_status(True, 10, "પ્રારંભ થઈ રહ્યું છે (Initializing)...")
        print(f"[{datetime.now()}] Starting YouTube analysis job...")

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
            print("Transcripts fetched. Calling analysis...")
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
                print(f"Analysis saved to {OUTPUT_FILE}")
            else:
                print("Failed to get valid analysis.")
        else:
            print("No transcripts could be fetched today.")
    finally:
        update_status(False, 100, "પૂર્ણ થયું (Completed)")

if __name__ == "__main__":
    print("YouTube Weather Analyzer Service Started...")

    run_job()


    schedule.every(6).hours.do(run_job)

    while True:
        schedule.run_pending()
        time.sleep(60)
