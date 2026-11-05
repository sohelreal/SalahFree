# Salah Free — free Muslim prayer app (Android, Kotlin + Jetpack Compose)

No ads · No login · No tracking · Works offline (after first run of the Quran script)

## Features
- 5 daily prayer times + sunrise, calculated astronomically with the Adhan library
- Azan alarm at each prayer (foreground service, exact alarms, re-armed after reboot / time-zone change); per-prayer on/off
- Qibla compass (true-north corrected, Kaaba distance) — worldwide
- Quran (Arabic + Sahih International), Duas, Hadith, Niyyah + rak'ah guide for the 5 prayers
- Hijri date with manual ±day adjustment (Bangladesh often differs from Saudi by a day)
- City presets (Bangladesh, Canada, USA, Makkah, ...) + GPS; methods: Karachi, ISNA, Umm al-Qura, MWL, Egyptian...; Hanafi/Shafi'i Asr
- English + Bengali (বাংলা) names and niyyah

## Build
1. Install Android Studio (Koala or newer), open this folder, let Gradle sync.
2. **Full Quran:** `python3 tools/fetch_quran.py` (internet needed once). Without it, only Al-Fatihah and Al-Ikhlas are bundled.
3. **Azan audio:** put an MP3 you have the right to redistribute at `app/src/main/res/raw/azan.mp3`
   (optional: `azan_fajr.mp3` for Fajr). Without it the phone's default sound plays.
4. Run on a device (the compass needs a real phone, not an emulator).

## Important notes
- Verify times against your local mosque / Islamic Foundation once and adjust the method or Hijri offset if needed.
- Android 12+: grant "Alarms & reminders"; on Xiaomi/Huawei/Samsung disable battery optimisation for reliable azan.
- Hadith/dua texts were entered by hand: have a scholar proof-read before public release.

## Put it on GitHub
```bash
git remote add origin https://github.com/YOUR-USERNAME/SalahFree.git
git branch -M main
git push -u origin main
```
After the push, open the **Actions** tab: the "Build APK" workflow fetches the full Quran, builds the app
and uploads `SalahFree-debug-apk` — download it from the run page and install it on your phone.
(Add `azan.mp3` to `app/src/main/res/raw/` before pushing if you want your own azan sound in the APK.)

## License
MIT. Free for everyone. Quran text/translation come from the AlQuran Cloud API (Tanzil Uthmani text, Sahih International).
