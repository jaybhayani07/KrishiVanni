from youtube_transcript_api import YouTubeTranscriptApi
try:
    video_id = "_Ynw-6XsvkE"
    api = YouTubeTranscriptApi()
    transcript_list = api.list(video_id)
    transcript = transcript_list.find_transcript(['en', 'gu', 'hi'])
    data = transcript.fetch()
    text = " ".join([getattr(t, 'text', '') if hasattr(t, 'text') else t['text'] for t in data])
    print("Success! Length:", len(text))
    print(text[:200])
except Exception as e:
    print("Error:", e)
