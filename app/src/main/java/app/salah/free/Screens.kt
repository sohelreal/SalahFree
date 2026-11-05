package app.salah.free

import android.content.Context
import android.content.Intent
import android.hardware.GeomagneticField
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import org.json.JSONArray
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

private fun lang(ctx: Context) = Prefs.sp(ctx).getString("lang", "en") ?: "en"

/* ───────────────────────── PRAYER TIMES ───────────────────────── */

@Composable
fun PrayerScreen(modifier: Modifier, ver: Int) {
    val ctx = LocalContext.current
    val loc = remember(ver) { Engine.loc(ctx) }
    val today = remember(ver) { Engine.timesFor(ctx, loc, 0) }
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var enabledVer by remember { mutableIntStateOf(0) }
    var detail by remember { mutableStateOf<PrayerInfo?>(null) }
    LaunchedEffect(Unit) { while (true) { delay(1000); now = System.currentTimeMillis() } }

    val (nextKey, nextAt) = remember(now / 1000, ver) { Engine.next(ctx, now) }
    val secs = ((nextAt - now) / 1000).coerceAtLeast(0)
    val bn = lang(ctx) == "bn"

    LazyColumn(modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                Column(Modifier.padding(20.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(Engine.hijri(ctx), fontWeight = FontWeight.Medium)
                    Text(loc.label, fontSize = 13.sp)
                    Spacer(Modifier.height(12.dp))
                    val info = prayerInfo(nextKey)
                    Text("Next: ${info.en}  ${info.ar}" + if (bn) "  ${info.bn}" else "", fontSize = 18.sp)
                    Text("%02d:%02d:%02d".format(secs / 3600, secs % 3600 / 60, secs % 60),
                        fontSize = 40.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
        item {
            PrayerRow(SUNRISE, Engine.fmt(ctx, today.sunrise.time, loc.tz), bn, null, false) {}
        }
        items(PRAYERS) { p ->
            val t = when (p.key) {
                "fajr" -> today.fajr; "dhuhr" -> today.dhuhr; "asr" -> today.asr
                "maghrib" -> today.maghrib; else -> today.isha
            }
            val on = remember(enabledVer) { Engine.azanEnabled(ctx, p.key) }
            PrayerRow(p, Engine.fmt(ctx, t.time, loc.tz), bn, on, p.key == nextKey) { toggle ->
                if (toggle) {
                    Prefs.sp(ctx).edit().putBoolean("azan_${p.key}", !on).apply()
                    enabledVer++; Scheduler.scheduleNext(ctx)
                } else detail = p
            }
        }
        item { Text("Tap a prayer for niyyah, rak'ahs and Sunnah. Switch = play azan at that time.", fontSize = 12.sp) }
    }
    detail?.let { PrayerDetail(it, bn) { detail = null } }
}

@Composable
private fun PrayerRow(p: PrayerInfo, time: String, bn: Boolean, on: Boolean?, highlight: Boolean, click: (Boolean) -> Unit) {
    Card(
        Modifier.fillMaxWidth().clickable { click(false) },
        colors = if (highlight) CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer) else CardDefaults.cardColors()
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(p.ar, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Text(if (bn) "${p.bn} · ${p.en}" else p.en, fontSize = 14.sp)
            }
            Text(time, fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
            if (on != null) {
                Spacer(Modifier.width(8.dp))
                Switch(checked = on, onCheckedChange = { click(true) })
            }
        }
    }
}

@Composable
private fun PrayerDetail(p: PrayerInfo, bn: Boolean, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } },
        title = { Text("${p.en} · ${p.ar}") },
        text = {
            LazyColumn {
                item {
                    Text("Fard: ${p.rakats} rak'ahs", fontWeight = FontWeight.Bold)
                    Text(if (bn) p.sunnahBn else p.sunnahEn)
                    Spacer(Modifier.height(12.dp))
                    Text("Niyyah", fontWeight = FontWeight.Bold)
                    Text(niyyatAr(p), fontSize = 22.sp, textAlign = TextAlign.End, modifier = Modifier.fillMaxWidth(), lineHeight = 36.sp)
                    Spacer(Modifier.height(6.dp))
                    Text(niyyatEn(p))
                    if (bn) { Spacer(Modifier.height(6.dp)); Text(niyyatBn(p)) }
                    Spacer(Modifier.height(10.dp))
                    Text(if (bn) NIYYAT_NOTE_BN else NIYYAT_NOTE_EN, fontSize = 12.sp)
                }
            }
        }
    )
}

/* ───────────────────────── QIBLA ───────────────────────── */

@Composable
fun QiblaScreen(modifier: Modifier, ver: Int) {
    val ctx = LocalContext.current
    val loc = remember(ver) { Engine.loc(ctx) }
    val bearing = remember(ver) { Qibla.bearing(loc.lat, loc.lon).toFloat() }
    val dist = remember(ver) { Qibla.distanceKm(loc.lat, loc.lon) }
    val declination = remember(ver) {
        GeomagneticField(loc.lat.toFloat(), loc.lon.toFloat(), 0f, System.currentTimeMillis()).declination
    }
    var heading by remember { mutableFloatStateOf(0f) } // degrees from TRUE north
    var hasSensor by remember { mutableStateOf(true) }

    DisposableEffect(Unit) {
        val sm = ctx.getSystemService(SensorManager::class.java)
        val sensor = sm.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        val listener = object : SensorEventListener {
            val r = FloatArray(9); val o = FloatArray(3)
            override fun onSensorChanged(e: SensorEvent) {
                SensorManager.getRotationMatrixFromVector(r, e.values)
                SensorManager.getOrientation(r, o)
                val az = ((Math.toDegrees(o[0].toDouble()) + declination + 360) % 360).toFloat()
                var d = az - heading
                if (d > 180) d -= 360
                if (d < -180) d += 360
                heading = (heading + d * 0.2f + 360f) % 360f
            }
            override fun onAccuracyChanged(s: Sensor?, a: Int) {}
        }
        if (sensor == null) hasSensor = false else sm.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_UI)
        onDispose { sm.unregisterListener(listener) }
    }

    val diff = ((bearing - heading + 540) % 360) - 180
    val aligned = hasSensor && abs(diff) < 4f
    val primary = MaterialTheme.colorScheme.primary
    val onSurface = MaterialTheme.colorScheme.onSurface

    Column(modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(loc.label)
        Text("Qibla: %.1f° from North  ·  Kaaba %,d km away".format(bearing, dist.toInt()), fontSize = 14.sp)
        Spacer(Modifier.height(8.dp))
        Text(if (aligned) "✔ You are facing the Qibla" else if (hasSensor) "Turn until the green arrow points up" else "No compass sensor — use the bearing above",
            color = if (aligned) primary else onSurface, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        Canvas(Modifier.fillMaxWidth().aspectRatio(1f)) {
            val c = center
            val rad = size.minDimension / 2 * 0.9f
            // Dial rotates opposite to the phone heading so N always points to true north
            rotate(-heading, c) {
                drawCircle(onSurface.copy(alpha = 0.25f), rad, c, style = Stroke(3f))
                for (deg in 0 until 360 step 30) {
                    val a = Math.toRadians(deg - 90.0)
                    val inner = rad * (if (deg % 90 == 0) 0.86f else 0.93f)
                    drawLine(onSurface.copy(0.6f),
                        Offset(c.x + (cos(a) * inner).toFloat(), c.y + (sin(a) * inner).toFloat()),
                        Offset(c.x + (cos(a) * rad).toFloat(), c.y + (sin(a) * rad).toFloat()), 3f)
                }
                // North marker (red)
                drawCircle(Color.Red, 10f, Offset(c.x, c.y - rad * 0.78f))
                // Qibla needle fixed to its bearing on the dial
                rotate(bearing, c) {
                    drawLine(if (aligned) primary else Color(0xFF2E7D32), c, Offset(c.x, c.y - rad * 0.8f), 12f, StrokeCap.Round)
                    drawRect(Color(0xFF111111), Offset(c.x - 16f, c.y - rad * 0.8f - 32f), Size(32f, 32f)) // Kaaba
                    drawRect(Color(0xFFD4AF37), Offset(c.x - 16f, c.y - rad * 0.8f - 24f), Size(32f, 5f))
                }
            }
            drawCircle(onSurface, 8f, c)
        }
        Spacer(Modifier.height(8.dp))
        Text("Red dot = North · Black cube = Kaaba. Heading is corrected for magnetic declination (${"%.1f".format(declination)}°). Keep the phone flat and away from metal/magnets; wave in a figure-8 to calibrate.",
            fontSize = 12.sp, textAlign = TextAlign.Center)
    }
}

/* ───────────────────────── QURAN ───────────────────────── */

data class Ayah(val ar: String, val en: String)
data class Surah(val n: Int, val name: String, val ar: String, val meaning: String, val ayahs: List<Ayah>)

private fun loadQuran(ctx: Context): List<Surah> = try {
    val txt = ctx.assets.open("quran.json").bufferedReader(Charsets.UTF_8).use { it.readText() }
    val arr = JSONArray(txt)
    List(arr.length()) { i ->
        val s = arr.getJSONObject(i)
        val a = s.getJSONArray("ayahs")
        Surah(s.getInt("n"), s.getString("name"), s.getString("ar"), s.getString("en"),
            List(a.length()) { j -> a.getJSONObject(j).let { Ayah(it.getString("a"), it.getString("e")) } })
    }
} catch (e: Exception) { emptyList() }

@Composable
fun QuranScreen(modifier: Modifier) {
    val ctx = LocalContext.current
    val surahs = remember { loadQuran(ctx) }
    var open by rememberSaveable { mutableStateOf<Int?>(null) }
    val s = surahs.firstOrNull { it.n == open }

    if (s == null) {
        LazyColumn(modifier.fillMaxSize(), contentPadding = PaddingValues(12.dp)) {
            if (surahs.size < 114) item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                    Text("Only ${surahs.size} of 114 surahs are bundled in this build. Run tools/fetch_quran.py once to generate the full Quran (see README).",
                        Modifier.padding(12.dp), fontSize = 13.sp)
                }
            }
            items(surahs) { su ->
                Row(Modifier.fillMaxWidth().clickable { open = su.n }.padding(vertical = 12.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Text("${su.n}", Modifier.width(40.dp), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Column(Modifier.weight(1f)) {
                        Text(su.name, fontWeight = FontWeight.Medium)
                        Text("${su.meaning} · ${su.ayahs.size} ayahs", fontSize = 12.sp)
                    }
                    Text(su.ar, fontSize = 22.sp)
                }
                HorizontalDivider()
            }
        }
    } else {
        LazyColumn(modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            item {
                TextButton(onClick = { open = null }) { Text("← All surahs") }
                Text(s.ar, fontSize = 32.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                Text("${s.n}. ${s.name} — ${s.meaning}", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                if (s.n != 1 && s.n != 9)
                    Text("بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ", fontSize = 26.sp, modifier = Modifier.fillMaxWidth().padding(top = 12.dp), textAlign = TextAlign.Center)
            }
            itemsIndexed(s.ayahs) { i, a ->
                Column {
                    Text("${a.ar} ﴿${i + 1}﴾", fontSize = 26.sp, lineHeight = 44.sp, textAlign = TextAlign.End, modifier = Modifier.fillMaxWidth())
                    Text(a.en, fontSize = 14.sp, modifier = Modifier.padding(top = 4.dp))
                }
            }
            item { Text("Translation: Sahih International", fontSize = 11.sp) }
        }
    }
}

/* ───────────────────────── DUAS & HADITH ───────────────────────── */

@Composable
fun DuasScreen(modifier: Modifier) {
    LazyColumn(modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(DUAS) { d ->
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text(d.title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text(d.ar, fontSize = 24.sp, lineHeight = 40.sp, textAlign = TextAlign.End, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp))
                    Text(d.translit, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic, fontSize = 14.sp)
                    Text(d.meaning, fontSize = 14.sp, modifier = Modifier.padding(top = 4.dp))
                    Text("Source: ${d.source}", fontSize = 11.sp, modifier = Modifier.padding(top = 6.dp))
                }
            }
        }
    }
}

@Composable
fun HadithScreen(modifier: Modifier) {
    LazyColumn(modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(HADITHS) { h ->
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    if (h.ar.isNotEmpty())
                        Text(h.ar, fontSize = 22.sp, lineHeight = 38.sp, textAlign = TextAlign.End, modifier = Modifier.fillMaxWidth())
                    Text(h.en, modifier = Modifier.padding(vertical = 6.dp))
                    Text(h.source, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

/* ───────────────────────── SETTINGS ───────────────────────── */

@Composable
fun SettingsScreen(modifier: Modifier, onChanged: () -> Unit) {
    val ctx = LocalContext.current
    val sp = remember { Prefs.sp(ctx) }
    var v by remember { mutableIntStateOf(0) }
    fun change(block: android.content.SharedPreferences.Editor.() -> Unit) {
        sp.edit().apply(block).apply(); v++; Scheduler.scheduleNext(ctx); onChanged()
    }
    var gpsMsg by remember { mutableStateOf("") }
    val city = sp.getInt("city", 0); val method = sp.getString("method", "KARACHI")
    @Suppress("UNUSED_EXPRESSION") v

    LazyColumn(modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp)) {
        item { SectionTitle("Location") }
        item {
            Button(onClick = {
                gpsMsg = if (Engine.useGps(ctx)) "Using your GPS location." else "No location yet — grant permission, turn on location, then try again."
                v++; Scheduler.scheduleNext(ctx); onChanged()
            }) { Text("📍 Use my GPS location") }
            if (gpsMsg.isNotEmpty()) Text(gpsMsg, fontSize = 12.sp)
        }
        itemsIndexed(CITIES) { i, c ->
            Row(Modifier.fillMaxWidth().clickable {
                change { putInt("city", i); putString("method", c.method); putBoolean("hanafi", c.hanafi) }
            }, verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = city == i, onClick = null)
                Text("  ${c.name}, ${c.country}", Modifier.padding(vertical = 8.dp))
            }
        }
        item { SectionTitle("Calculation method") }
        items(METHODS) { (id, label) ->
            Row(Modifier.fillMaxWidth().clickable { change { putString("method", id) } }, verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = method == id, onClick = null)
                Text("  $label", Modifier.padding(vertical = 6.dp), fontSize = 14.sp)
            }
        }
        item {
            SectionTitle("Asr (juristic school)")
            val h = sp.getBoolean("hanafi", true)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(checked = h, onCheckedChange = { change { putBoolean("hanafi", it) } })
                Text("  Hanafi Asr (later). Off = Shafi'i/Maliki/Hanbali", fontSize = 14.sp)
            }
        }
        item {
            SectionTitle("Display")
            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(checked = sp.getString("lang", "en") == "bn", onCheckedChange = { change { putString("lang", if (it) "bn" else "en") } })
                Text("  বাংলা (Bengali names & niyyah)")
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(checked = sp.getBoolean("h24", false), onCheckedChange = { change { putBoolean("h24", it) } })
                Text("  24-hour clock")
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Hijri date adjust: ${sp.getInt("hijri_off", 0)} day(s)")
                TextButton(onClick = { change { putInt("hijri_off", sp.getInt("hijri_off", 0) - 1) } }) { Text("−") }
                TextButton(onClick = { change { putInt("hijri_off", sp.getInt("hijri_off", 0) + 1) } }) { Text("+") }
            }
        }
        item {
            SectionTitle("Azan")
            Text("For reliable azan on every phone: allow notifications, allow exact alarms, and turn off battery optimisation for this app.", fontSize = 13.sp)
            Row {
                TextButton(onClick = {
                    ctx.startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:${ctx.packageName}")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                }) { Text("Exact alarms") }
                TextButton(onClick = {
                    ctx.startActivity(Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, Uri.parse("package:${ctx.packageName}")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                }) { Text("Battery") }
                TextButton(onClick = {
                    ctx.startForegroundService(Intent(ctx, AzanService::class.java).putExtra("key", "dhuhr"))
                }) { Text("▶ Test azan") }
            }
        }
    }
}

@Composable private fun SectionTitle(t: String) =
    Text(t, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 20.dp, bottom = 6.dp))
