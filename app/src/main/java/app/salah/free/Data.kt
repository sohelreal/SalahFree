package app.salah.free

data class City(
    val name: String, val country: String,
    val lat: Double, val lon: Double, val tz: String,
    val method: String, val hanafi: Boolean
)

val CITIES = listOf(
    City("Dhaka", "Bangladesh", 23.8103, 90.4125, "Asia/Dhaka", "KARACHI", true),
    City("Chattogram", "Bangladesh", 22.3569, 91.7832, "Asia/Dhaka", "KARACHI", true),
    City("Sylhet", "Bangladesh", 24.8949, 91.8687, "Asia/Dhaka", "KARACHI", true),
    City("Rajshahi", "Bangladesh", 24.3745, 88.6042, "Asia/Dhaka", "KARACHI", true),
    City("Khulna", "Bangladesh", 22.8456, 89.5403, "Asia/Dhaka", "KARACHI", true),
    City("Toronto", "Canada", 43.6532, -79.3832, "America/Toronto", "NORTH_AMERICA", false),
    City("Montreal", "Canada", 45.5017, -73.5673, "America/Toronto", "NORTH_AMERICA", false),
    City("Calgary", "Canada", 51.0447, -114.0719, "America/Edmonton", "NORTH_AMERICA", false),
    City("Regina", "Canada", 50.4452, -104.6189, "America/Regina", "NORTH_AMERICA", false),
    City("Vancouver", "Canada", 49.2827, -123.1207, "America/Vancouver", "NORTH_AMERICA", false),
    City("New York", "USA", 40.7128, -74.0060, "America/New_York", "NORTH_AMERICA", false),
    City("Chicago", "USA", 41.8781, -87.6298, "America/Chicago", "NORTH_AMERICA", false),
    City("Houston", "USA", 29.7604, -95.3698, "America/Chicago", "NORTH_AMERICA", false),
    City("Los Angeles", "USA", 34.0522, -118.2437, "America/Los_Angeles", "NORTH_AMERICA", false),
    City("Makkah", "Saudi Arabia", 21.4225, 39.8262, "Asia/Riyadh", "UMM_AL_QURA", false),
    City("Madinah", "Saudi Arabia", 24.4672, 39.6024, "Asia/Riyadh", "UMM_AL_QURA", false),
    City("Dubai", "UAE", 25.2048, 55.2708, "Asia/Dubai", "DUBAI", false),
    City("Karachi", "Pakistan", 24.8607, 67.0011, "Asia/Karachi", "KARACHI", true),
    City("Delhi", "India", 28.6139, 77.2090, "Asia/Kolkata", "KARACHI", true),
    City("Istanbul", "Turkey", 41.0082, 28.9784, "Europe/Istanbul", "TURKEY", true),
    City("Cairo", "Egypt", 30.0444, 31.2357, "Africa/Cairo", "EGYPTIAN", false),
    City("London", "UK", 51.5072, -0.1276, "Europe/London", "MUSLIM_WORLD_LEAGUE", false),
    City("Jakarta", "Indonesia", -6.2088, 106.8456, "Asia/Jakarta", "SINGAPORE", false),
    City("Kuala Lumpur", "Malaysia", 3.1390, 101.6869, "Asia/Kuala_Lumpur", "SINGAPORE", false),
    City("Sydney", "Australia", -33.8688, 151.2093, "Australia/Sydney", "MUSLIM_WORLD_LEAGUE", false),
)

val METHODS = listOf(
    "MUSLIM_WORLD_LEAGUE" to "Muslim World League",
    "KARACHI" to "University of Islamic Sciences, Karachi (Bangladesh / Pakistan / India)",
    "NORTH_AMERICA" to "ISNA (USA / Canada)",
    "UMM_AL_QURA" to "Umm al-Qura (Makkah)",
    "EGYPTIAN" to "Egyptian General Authority",
    "DUBAI" to "Dubai",
    "KUWAIT" to "Kuwait",
    "QATAR" to "Qatar",
    "SINGAPORE" to "Singapore / Malaysia / Indonesia",
    "TURKEY" to "Turkey (Diyanet)",
    "TEHRAN" to "Tehran",
    "MOON_SIGHTING_COMMITTEE" to "Moonsighting Committee",
)

data class PrayerInfo(
    val key: String, val ar: String, val en: String, val bn: String,
    val arArticle: String, val rakats: Int, val sunnahEn: String, val sunnahBn: String
)

val PRAYERS = listOf(
    PrayerInfo("fajr", "الفجر", "Fajr", "ফজর", "الْفَجْرِ", 2,
        "2 Sunnah before Fard (very strongly emphasised), then 2 Fard",
        "ফরজের আগে ২ রাকাত সুন্নত (অত্যন্ত তাকিদপূর্ণ), তারপর ২ রাকাত ফরজ"),
    PrayerInfo("dhuhr", "الظهر", "Dhuhr", "যোহর", "الظُّهْرِ", 4,
        "4 Sunnah, 4 Fard, 2 Sunnah, 2 Nafl",
        "৪ সুন্নত, ৪ ফরজ, ২ সুন্নত, ২ নফল"),
    PrayerInfo("asr", "العصر", "Asr", "আসর", "الْعَصْرِ", 4,
        "4 Sunnah (optional), 4 Fard",
        "৪ সুন্নত (গাইরে মুয়াক্কাদা), ৪ ফরজ"),
    PrayerInfo("maghrib", "المغرب", "Maghrib", "মাগরিব", "الْمَغْرِبِ", 3,
        "3 Fard, 2 Sunnah, 2 Nafl",
        "৩ ফরজ, ২ সুন্নত, ২ নফল"),
    PrayerInfo("isha", "العشاء", "Isha", "এশা", "الْعِشَاءِ", 4,
        "4 Sunnah (optional), 4 Fard, 2 Sunnah, 2 Nafl, 3 Witr (Hanafi), 2 Nafl",
        "৪ সুন্নত (ঐচ্ছিক), ৪ ফরজ, ২ সুন্নত, ২ নফল, ৩ বিতির, ২ নফল"),
)
val SUNRISE = PrayerInfo("sunrise", "الشروق", "Sunrise", "সূর্যোদয়", "", 0, "", "")

fun prayerInfo(key: String) = PRAYERS.firstOrNull { it.key == key } ?: SUNRISE

private val BN_DIGITS = mapOf(2 to "২", 3 to "৩", 4 to "৪")
private val AR_RAKAT = mapOf(2 to "رَكْعَتَيْنِ", 3 to "ثَلَاثَ رَكَعَاتٍ", 4 to "أَرْبَعَ رَكَعَاتٍ")

fun niyyatAr(p: PrayerInfo) =
    "نَوَيْتُ أَنْ أُصَلِّيَ لِلَّهِ تَعَالَىٰ ${AR_RAKAT[p.rakats]} صَلَاةَ ${p.arArticle} فَرْضُ اللَّهِ تَعَالَىٰ مُتَوَجِّهًا إِلَىٰ جِهَةِ الْكَعْبَةِ الشَّرِيفَةِ اللَّهُ أَكْبَرُ"

private val BN_GEN = mapOf("fajr" to "ফজরের", "dhuhr" to "যোহরের", "asr" to "আসরের", "maghrib" to "মাগরিবের", "isha" to "এশার")

fun niyyatBn(p: PrayerInfo) =
    "আমি কিবলামুখী হয়ে আল্লাহর জন্য ${BN_DIGITS[p.rakats]} রাকাত ${BN_GEN[p.key]} ফরজ নামাজ আদায় করার নিয়ত করছি। আল্লাহু আকবার।"

fun niyyatEn(p: PrayerInfo) =
    "I intend to pray ${p.rakats} rak'ahs of the obligatory (Fard) ${p.en} prayer for the sake of Allah, facing the Qiblah. Allahu Akbar."

const val NIYYAT_NOTE_EN = "Niyyah (intention) is in the heart. Saying it aloud is optional — the words above are a common form, not a requirement."
const val NIYYAT_NOTE_BN = "নিয়ত মূলত অন্তরের বিষয়। মুখে বলা আবশ্যক নয়; উপরের শব্দগুলো প্রচলিত একটি রূপ মাত্র।"

val BN_MONTHS_NOTE = ""
val HIJRI_MONTHS = listOf(
    "Muharram", "Safar", "Rabi' al-Awwal", "Rabi' al-Thani", "Jumada al-Ula", "Jumada al-Akhirah",
    "Rajab", "Sha'ban", "Ramadan", "Shawwal", "Dhul Qa'dah", "Dhul Hijjah"
)

data class Dua(val title: String, val ar: String, val translit: String, val meaning: String, val source: String)

val DUAS = listOf(
    Dua("Starting the prayer (Thana)",
        "سُبْحَانَكَ اللَّهُمَّ وَبِحَمْدِكَ وَتَبَارَكَ اسْمُكَ وَتَعَالَىٰ جَدُّكَ وَلَا إِلَٰهَ غَيْرُكَ",
        "Subhanakal-lahumma wa bihamdika, wa tabarakasmuka, wa ta'ala jadduka, wa la ilaha ghayruk.",
        "Glory and praise be to You, O Allah. Blessed is Your name, exalted is Your majesty, and there is no god but You.",
        "Abu Dawud, Tirmidhi"),
    Dua("In Ruku' (bowing)", "سُبْحَانَ رَبِّيَ الْعَظِيمِ",
        "Subhana Rabbiyal 'Azim.", "Glory be to my Lord, the Most Great.", "Muslim"),
    Dua("Rising from Ruku'", "سَمِعَ اللَّهُ لِمَنْ حَمِدَهُ، رَبَّنَا وَلَكَ الْحَمْدُ",
        "Sami'allahu liman hamidah. Rabbana wa lakal-hamd.",
        "Allah hears whoever praises Him. Our Lord, to You belongs all praise.", "Bukhari"),
    Dua("In Sujud (prostration)", "سُبْحَانَ رَبِّيَ الْأَعْلَىٰ",
        "Subhana Rabbiyal A'la.", "Glory be to my Lord, the Most High.", "Muslim"),
    Dua("Durood Ibrahim (final sitting)",
        "اللَّهُمَّ صَلِّ عَلَىٰ مُحَمَّدٍ وَعَلَىٰ آلِ مُحَمَّدٍ كَمَا صَلَّيْتَ عَلَىٰ إِبْرَاهِيمَ وَعَلَىٰ آلِ إِبْرَاهِيمَ إِنَّكَ حَمِيدٌ مَجِيدٌ، اللَّهُمَّ بَارِكْ عَلَىٰ مُحَمَّدٍ وَعَلَىٰ آلِ مُحَمَّدٍ كَمَا بَارَكْتَ عَلَىٰ إِبْرَاهِيمَ وَعَلَىٰ آلِ إِبْرَاهِيمَ إِنَّكَ حَمِيدٌ مَجِيدٌ",
        "Allahumma salli 'ala Muhammad wa 'ala ali Muhammad, kama sallayta 'ala Ibrahim wa 'ala ali Ibrahim, innaka Hamidum Majid. Allahumma barik 'ala Muhammad wa 'ala ali Muhammad, kama barakta 'ala Ibrahim wa 'ala ali Ibrahim, innaka Hamidum Majid.",
        "O Allah, send prayers upon Muhammad and the family of Muhammad as You sent prayers upon Ibrahim and the family of Ibrahim; You are Praiseworthy and Glorious. O Allah, bless Muhammad and the family of Muhammad as You blessed Ibrahim and the family of Ibrahim; You are Praiseworthy and Glorious.",
        "Bukhari, Muslim"),
    Dua("After prayer / Rabbana atina",
        "رَبَّنَا آتِنَا فِي الدُّنْيَا حَسَنَةً وَفِي الْآخِرَةِ حَسَنَةً وَقِنَا عَذَابَ النَّارِ",
        "Rabbana atina fid-dunya hasanatan wa fil-akhirati hasanatan wa qina 'adhaban-nar.",
        "Our Lord, give us good in this world and good in the Hereafter, and protect us from the punishment of the Fire.",
        "Qur'an 2:201"),
    Dua("After Wudu",
        "أَشْهَدُ أَنْ لَا إِلَٰهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ وَأَشْهَدُ أَنَّ مُحَمَّدًا عَبْدُهُ وَرَسُولُهُ",
        "Ash-hadu an la ilaha illallahu wahdahu la sharika lah, wa ash-hadu anna Muhammadan 'abduhu wa rasuluh.",
        "I bear witness that there is no god but Allah alone, with no partner, and I bear witness that Muhammad is His servant and Messenger.",
        "Muslim"),
    Dua("After hearing the Adhan",
        "اللَّهُمَّ رَبَّ هَذِهِ الدَّعْوَةِ التَّامَّةِ وَالصَّلَاةِ الْقَائِمَةِ آتِ مُحَمَّدًا الْوَسِيلَةَ وَالْفَضِيلَةَ وَابْعَثْهُ مَقَامًا مَحْمُودًا الَّذِي وَعَدْتَهُ",
        "Allahumma Rabba hadhihid-da'watit-tammah, was-salatil-qa'imah, ati Muhammadanil-wasilata wal-fadilah, wab'athhu maqamam-mahmudanil-ladhi wa'adtah.",
        "O Allah, Lord of this perfect call and established prayer, grant Muhammad the Wasilah and the Fadilah, and raise him to the praised station You promised him.",
        "Bukhari"),
    Dua("Entering the mosque", "اللَّهُمَّ افْتَحْ لِي أَبْوَابَ رَحْمَتِكَ",
        "Allahummaf-tah li abwaba rahmatik.", "O Allah, open for me the doors of Your mercy.", "Muslim"),
    Dua("Leaving the mosque", "اللَّهُمَّ إِنِّي أَسْأَلُكَ مِنْ فَضْلِكَ",
        "Allahumma inni as'aluka min fadlik.", "O Allah, I ask You of Your bounty.", "Muslim"),
    Dua("Before eating", "بِسْمِ اللَّهِ",
        "Bismillah.", "In the name of Allah.", "Abu Dawud, Tirmidhi"),
    Dua("After eating",
        "الْحَمْدُ لِلَّهِ الَّذِي أَطْعَمَنَا وَسَقَانَا وَجَعَلَنَا مُسْلِمِينَ",
        "Alhamdu lillahil-ladhi at'amana wa saqana wa ja'alana muslimin.",
        "Praise be to Allah who fed us, gave us drink, and made us Muslims.", "Abu Dawud, Tirmidhi"),
    Dua("Before sleeping", "بِاسْمِكَ اللَّهُمَّ أَمُوتُ وَأَحْيَا",
        "Bismika Allahumma amutu wa ahya.", "In Your name, O Allah, I die and I live.", "Bukhari"),
    Dua("Waking up",
        "الْحَمْدُ لِلَّهِ الَّذِي أَحْيَانَا بَعْدَ مَا أَمَاتَنَا وَإِلَيْهِ النُّشُورُ",
        "Alhamdu lillahil-ladhi ahyana ba'da ma amatana wa ilayhin-nushur.",
        "Praise be to Allah who gave us life after causing us to die, and to Him is the resurrection.", "Bukhari"),
    Dua("Leaving the home",
        "بِسْمِ اللَّهِ تَوَكَّلْتُ عَلَى اللَّهِ وَلَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِاللَّهِ",
        "Bismillahi tawakkaltu 'alallah, wa la hawla wa la quwwata illa billah.",
        "In the name of Allah, I place my trust in Allah; there is no might or power except with Allah.",
        "Abu Dawud, Tirmidhi"),
)

data class Hadith(val ar: String, val en: String, val source: String)

val HADITHS = listOf(
    Hadith("إِنَّمَا الْأَعْمَالُ بِالنِّيَّاتِ، وَإِنَّمَا لِكُلِّ امْرِئٍ مَا نَوَىٰ",
        "Actions are judged by intentions, and every person will get what they intended.",
        "Bukhari 1, Muslim 1907 — Umar ibn al-Khattab (RA)"),
    Hadith("خَيْرُكُمْ مَنْ تَعَلَّمَ الْقُرْآنَ وَعَلَّمَهُ",
        "The best among you are those who learn the Qur'an and teach it.",
        "Bukhari 5027 — Uthman ibn Affan (RA)"),
    Hadith("", "None of you truly believes until he loves for his brother what he loves for himself.",
        "Bukhari 13, Muslim 45 — Anas ibn Malik (RA)"),
    Hadith("", "Whoever believes in Allah and the Last Day, let him speak good or keep silent.",
        "Bukhari 6018, Muslim 47 — Abu Hurairah (RA)"),
    Hadith("", "The five daily prayers are like a flowing river at your door in which you bathe five times a day — would any dirt remain on him? Likewise, the five prayers erase sins.",
        "Bukhari 528, Muslim 667 — Abu Hurairah (RA) (meaning)"),
    Hadith("الطُّهُورُ شَطْرُ الْإِيمَانِ",
        "Purity is half of faith.", "Muslim 223 — Abu Malik al-Ash'ari (RA)"),
    Hadith("", "The most beloved deeds to Allah are those done consistently, even if small.",
        "Bukhari 6464, Muslim 783 — Aisha (RA)"),
    Hadith("الدِّينُ النَّصِيحَةُ", "The religion is sincere advice.",
        "Muslim 55 — Tamim ad-Dari (RA)"),
    Hadith("يَسِّرُوا وَلَا تُعَسِّرُوا", "Make things easy and do not make them difficult.",
        "Bukhari 69, Muslim 1734 — Anas ibn Malik (RA)"),
    Hadith("", "Whoever travels a path seeking knowledge, Allah makes easy for him the path to Paradise.",
        "Muslim 2699 — Abu Hurairah (RA)"),
    Hadith("", "The first matter the servant will be questioned about on the Day of Resurrection is the prayer.",
        "Tirmidhi 413, Nasa'i 465 — Abu Hurairah (RA) (Sahih)"),
)
