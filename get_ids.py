import urllib.request, re

urls = [
    ("Paresh", "https://www.youtube.com/@pareshgoswami"),
    ("Ambalal", "https://www.youtube.com/@AmbalalPatelOfficial"),
    ("Jagat", "https://www.youtube.com/@JagatTat")
]

for name, url in urls:
    html = urllib.request.urlopen(url).read().decode('utf-8')
    match = re.search(r'\"channelId\":\"(UC[a-zA-Z0-9_-]{22})\"', html)
    if match:
        print(f"{name}: {match.group(1)}")
    else:
        print(f"{name}: Not found")
