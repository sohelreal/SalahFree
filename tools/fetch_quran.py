#!/usr/bin/env python3
"""
Generates app/src/main/assets/quran.json with ALL 114 surahs:
Arabic (Uthmani) + English (Sahih International), from the free AlQuran Cloud API.
Run once on a computer with internet:   python3 tools/fetch_quran.py
"""
import json, urllib.request, os

URL = "https://api.alquran.cloud/v1/quran/editions/quran-uthmani,en.sahih"
BISMILLAH_WORDS = 4  # the app shows Bismillah itself, so strip it from the first ayah

with urllib.request.urlopen(URL, timeout=60) as r:
    data = json.load(r)["data"]

ar, en = data[0]["surahs"], data[1]["surahs"]
out = []
for a, e in zip(ar, en):
    n = a["number"]
    ayahs = []
    for i, (x, y) in enumerate(zip(a["ayahs"], e["ayahs"])):
        text = x["text"]
        if i == 0 and n not in (1, 9):
            parts = text.split(" ")
            if len(parts) > BISMILLAH_WORDS:
                text = " ".join(parts[BISMILLAH_WORDS:])
        ayahs.append({"a": text, "e": y["text"]})
    out.append({"n": n, "name": a["englishName"], "ar": a["name"].replace("سورة", "").strip(),
                "en": a["englishNameTranslation"], "ayahs": ayahs})

assert len(out) == 114, len(out)
path = os.path.join(os.path.dirname(__file__), "..", "app", "src", "main", "assets", "quran.json")
with open(path, "w", encoding="utf-8") as f:
    json.dump(out, f, ensure_ascii=False)
print("Wrote", os.path.normpath(path), "-", sum(len(s["ayahs"]) for s in out), "ayahs")
