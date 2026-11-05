import json, urllib.request, os

URL = "https://api.alquran.cloud/v1/quran/editions/quran-uthmani,en.sahih"
with urllib.request.urlopen(URL, timeout=60) as r:
    data = json.load(r)["data"]

out = []
for a, e in zip(data[0]["surahs"], data[1]["surahs"]):
    n = a["number"]
    ayahs = []
    for i, (x, y) in enumerate(zip(a["ayahs"], e["ayahs"])):
        text = x["text"]
        if i == 0 and n not in (1, 9):
            parts = text.split(" ")
            if len(parts) > 4:
                text = " ".join(parts[4:])
        ayahs.append({"a": text, "e": y["text"]})
    out.append({"n": n, "name": a["englishName"], "ar": a["name"].replace("سورة", "").strip(),
                "en": a["englishNameTranslation"], "ayahs": ayahs})

assert len(out) == 114
os.makedirs("app/src/main/assets", exist_ok=True)
with open("app/src/main/assets/quran.json", "w", encoding="utf-8") as f:
    json.dump(out, f, ensure_ascii=False)
print("done")
