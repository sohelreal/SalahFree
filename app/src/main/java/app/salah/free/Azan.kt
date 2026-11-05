package app.salah.free

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat

object Scheduler {
    private const val REQ = 1001

    /** Schedules a single exact alarm for the next enabled prayer. Each firing schedules the following one. */
    fun scheduleNext(ctx: Context) {
        val am = ctx.getSystemService(AlarmManager::class.java)
        val (key, at) = Engine.next(ctx, System.currentTimeMillis(), onlyEnabled = true)
        val pi = PendingIntent.getBroadcast(
            ctx, REQ, Intent(ctx, AzanReceiver::class.java).putExtra("key", key),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val canExact = Build.VERSION.SDK_INT < 31 || am.canScheduleExactAlarms()
        if (canExact) am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi)
        else am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi) // may be a few minutes late
    }
}

class AzanReceiver : BroadcastReceiver() {
    override fun onReceive(ctx: Context, intent: Intent) {
        val key = intent.getStringExtra("key") ?: return
        if (Engine.azanEnabled(ctx, key)) {
            ctx.startForegroundService(Intent(ctx, AzanService::class.java).putExtra("key", key))
        }
        Scheduler.scheduleNext(ctx)
    }
}

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(ctx: Context, intent: Intent) = Scheduler.scheduleNext(ctx)
}

class AzanService : Service() {
    private var player: MediaPlayer? = null

    override fun onBind(i: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == "STOP") { stopSelf(); return START_NOT_STICKY }
        val key = intent?.getStringExtra("key") ?: "dhuhr"
        val info = prayerInfo(key)

        val nm = getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(
            NotificationChannel("azan", "Azan", NotificationManager.IMPORTANCE_HIGH).apply { setSound(null, null) }
        )
        val stop = PendingIntent.getService(
            this, 0, Intent(this, AzanService::class.java).setAction("STOP"),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val open = PendingIntent.getActivity(
            this, 1, Intent(this, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE
        )
        val n: Notification = NotificationCompat.Builder(this, "azan")
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("${info.en} — ${info.ar}")
            .setContentText("It is time for ${info.en} prayer · ${info.bn}")
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(open)
            .addAction(0, "Stop azan", stop)
            .setOngoing(true)
            .build()
        if (Build.VERSION.SDK_INT >= 29)
            startForeground(7, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
        else startForeground(7, n)

        play(key)
        return START_NOT_STICKY
    }

    private fun play(key: String) {
        player?.release()
        // Looks for res/raw/azan_fajr.mp3 (Fajr only), then res/raw/azan.mp3, then falls back to the phone's default sound.
        var id = if (key == "fajr") resources.getIdentifier("azan_fajr", "raw", packageName) else 0
        if (id == 0) id = resources.getIdentifier("azan", "raw", packageName)
        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build()
        val mp = MediaPlayer()
        try {
            mp.setAudioAttributes(attrs)
            if (id != 0) {
                resources.openRawResourceFd(id).use { mp.setDataSource(it.fileDescriptor, it.startOffset, it.length) }
            } else {
                mp.setDataSource(this, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            }
            mp.setOnCompletionListener { stopSelf() }
            mp.setOnErrorListener { _, _, _ -> stopSelf(); true }
            mp.prepare(); mp.start()
            player = mp
        } catch (e: Exception) { mp.release(); stopSelf() }
    }

    override fun onDestroy() {
        player?.release(); player = null
        super.onDestroy()
    }
}
