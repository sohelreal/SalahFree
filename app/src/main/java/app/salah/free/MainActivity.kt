package app.salah.free

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val perms = mutableListOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
        if (Build.VERSION.SDK_INT >= 33) perms += Manifest.permission.POST_NOTIFICATIONS
        val launcher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            Scheduler.scheduleNext(this)
        }
        launcher.launch(perms.toTypedArray())
        Scheduler.scheduleNext(this)
        setContent { SalahTheme { AppRoot() } }
    }
}

@Composable
fun SalahTheme(content: @Composable () -> Unit) {
    val scheme = if (isSystemInDarkTheme())
        darkColorScheme(primary = Color(0xFF4CC9A0), secondary = Color(0xFFE0B84C))
    else
        lightColorScheme(primary = Color(0xFF0B6E4F), secondary = Color(0xFFB8860B))
    MaterialTheme(colorScheme = scheme, content = content)
}

private val TABS = listOf("Prayer" to "🕌", "Qibla" to "🧭", "Quran" to "📖", "Duas" to "🤲", "Hadith" to "📜")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppRoot() {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var settings by rememberSaveable { mutableStateOf(false) }
    var ver by remember { mutableIntStateOf(0) } // bump to refresh screens after settings change
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (settings) "Settings" else TABS[tab].first) },
                actions = {
                    TextButton(onClick = { settings = !settings }) { Text(if (settings) "✕" else "⚙ Settings") }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                TABS.forEachIndexed { i, (label, icon) ->
                    NavigationBarItem(
                        selected = !settings && tab == i,
                        onClick = { tab = i; settings = false },
                        icon = { Text(icon) },
                        label = { Text(label) }
                    )
                }
            }
        }
    ) { pad ->
        val m = Modifier.padding(pad)
        if (settings) SettingsScreen(m) { ver++ }
        else when (tab) {
            0 -> PrayerScreen(m, ver)
            1 -> QiblaScreen(m, ver)
            2 -> QuranScreen(m)
            3 -> DuasScreen(m)
            else -> HadithScreen(m)
        }
    }
}
