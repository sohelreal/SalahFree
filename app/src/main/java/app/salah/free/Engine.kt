package app.salah.free

import android.content.Context
import android.content.SharedPreferences
import android.location.LocationManager
import com.batoulapps.adhan.CalculationMethod
import com.batoulapps.adhan.CalculationParameters
import com.batoulapps.adhan.Coordinates
import com.batoulapps.adhan.Madhab
import com.batoulapps.adhan.PrayerTimes
import com.batoulapps.adhan.data.DateComponents
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.chrono.HijrahChronology
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.*

data class Loc(val lat: Double, val lon: Double, val tz: String, val label: String)

object Prefs {
    fun sp(ctx: Context): SharedPreferences = ctx.getSharedPreferences("salah", Context.MODE_PRIVATE)
}

object Engine {
    const val KAABA_LAT = 21.422487
    const val KAABA_LON = 39.826206

    fun loc(ctx: Context): Loc {
        val p = Prefs.sp(ctx)
        val idx = p.getInt("city", 0)
        if (idx >= 0) {
            val c = CITIES[idx.coerceIn(CITIES.indices)]
            return Loc(c.lat, c.lon, c.tz, "${c.name}, ${c.country}")
        }
        val lat = p.getString("lat", null)?.toDoubleOrNull() ?: CITIES[0].lat
        val lon = p.getString("lon", null)?.toDoubleOrNull() ?: CITIES[0].lon
        return Loc(lat, lon, TimeZone.getDefault().id, "Current location (GPS)")
    }

    private fun params(ctx: Context): CalculationParameters {
        val p = Prefs.sp(ctx)
        val m = try { CalculationMethod.valueOf(p.getString("method", "KARACHI")!!) }
                catch (e: Exception) { CalculationMethod.MUSLIM_WORLD_LEAGUE }
        val prm = m.parameters
        prm.madhab = if (p.getBoolean("hanafi", true)) Madhab.HANAFI else Madhab.SHAFI
        return prm
    }

    fun timesFor(ctx: Context, l: Loc, dayOffset: Int): PrayerTimes {
        val cal = Calendar.getInstance(TimeZone.getTimeZone(l.tz))
        cal.add(Calendar.DAY_OF_MONTH, dayOffset)
        val dc = DateComponents(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH))
        return PrayerTimes(Coordinates(l.lat, l.lon), dc, params(ctx))
    }

    fun five(pt: PrayerTimes): List<Pair<String, Date>> = listOf(
        "fajr" to pt.fajr, "dhuhr" to pt.dhuhr, "asr" to pt.asr, "maghrib" to pt.maghrib, "isha" to pt.isha
    )

    fun azanEnabled(ctx: Context, key: String) = Prefs.sp(ctx).getBoolean("azan_$key", true)

    /** Next prayer (any of the five) strictly after [now]. */
    fun next(ctx: Context, now: Long, onlyEnabled: Boolean = false): Pair<String, Long> {
        val l = loc(ctx)
        for (d in 0..2) {
            for ((k, t) in five(timesFor(ctx, l, d))) {
                if (t.time > now && (!onlyEnabled || azanEnabled(ctx, k))) return k to t.time
            }
        }
        return "fajr" to now + 3600_000
    }

    fun fmt(ctx: Context, ms: Long, tz: String): String {
        val pattern = if (Prefs.sp(ctx).getBoolean("h24", false)) "HH:mm" else "hh:mm a"
        return SimpleDateFormat(pattern, Locale.getDefault()).apply { timeZone = TimeZone.getTimeZone(tz) }.format(Date(ms))
    }

    /** Hijri date (Umm al-Qura calendar) with a manual day adjustment, since local moon sighting can differ (e.g. Bangladesh). */
    fun hijri(ctx: Context): String {
        val off = Prefs.sp(ctx).getInt("hijri_off", 0)
        val d = HijrahChronology.INSTANCE.date(LocalDate.now().plusDays(off.toLong()))
        val month = d.get(java.time.temporal.ChronoField.MONTH_OF_YEAR)
        return "${d.get(java.time.temporal.ChronoField.DAY_OF_MONTH)} ${HIJRI_MONTHS[month - 1]} ${d.get(java.time.temporal.ChronoField.YEAR)} AH"
    }

    fun guessMethod(lat: Double, lon: Double): Pair<String, Boolean> = when {
        lat in 20.5..26.8 && lon in 88.0..92.8 -> "KARACHI" to true            // Bangladesh
        lat in 6.0..37.0 && lon in 60.0..98.0 -> "KARACHI" to true             // Pakistan / India
        lat in 15.0..72.0 && lon in -170.0..-50.0 -> "NORTH_AMERICA" to false  // USA / Canada
        lat in 16.0..32.5 && lon in 34.0..56.0 -> "UMM_AL_QURA" to false       // Arabia
        lat in 35.0..42.5 && lon in 26.0..45.0 -> "TURKEY" to true
        lat in -11.0..7.0 && lon in 95.0..141.0 -> "SINGAPORE" to false
        else -> "MUSLIM_WORLD_LEAGUE" to false
    }

    /** Uses the last known device location. Returns false if none is available (permission/GPS off). */
    fun useGps(ctx: Context): Boolean {
        val lm = ctx.getSystemService(LocationManager::class.java)
        val best = try {
            lm.getProviders(true).mapNotNull { lm.getLastKnownLocation(it) }.maxByOrNull { it.time }
        } catch (e: SecurityException) { null } ?: return false
        val (m, h) = guessMethod(best.latitude, best.longitude)
        Prefs.sp(ctx).edit().putInt("city", -1)
            .putString("lat", best.latitude.toString()).putString("lon", best.longitude.toString())
            .putString("method", m).putBoolean("hanafi", h).apply()
        return true
    }
}

object Qibla {
    /** Initial great-circle bearing (degrees from true north) to the Kaaba. */
    fun bearing(lat: Double, lon: Double): Double {
        val p1 = Math.toRadians(lat); val p2 = Math.toRadians(Engine.KAABA_LAT)
        val dl = Math.toRadians(Engine.KAABA_LON - lon)
        val y = sin(dl) * cos(p2)
        val x = cos(p1) * sin(p2) - sin(p1) * cos(p2) * cos(dl)
        return (Math.toDegrees(atan2(y, x)) + 360) % 360
    }

    fun distanceKm(lat: Double, lon: Double): Double {
        val r = 6371.0
        val p1 = Math.toRadians(lat); val p2 = Math.toRadians(Engine.KAABA_LAT)
        val dp = p2 - p1; val dl = Math.toRadians(Engine.KAABA_LON - lon)
        val a = sin(dp / 2).pow(2) + cos(p1) * cos(p2) * sin(dl / 2).pow(2)
        return 2 * r * asin(sqrt(a))
    }
}
