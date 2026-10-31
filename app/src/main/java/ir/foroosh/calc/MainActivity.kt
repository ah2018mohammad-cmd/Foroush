package ir.foroosh.calc

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import ir.foroosh.calc.data.SettingsStore
import ir.foroosh.calc.ui.lc.LcScreen
import ir.foroosh.calc.ui.lock.LockScreen
import ir.foroosh.calc.ui.nikvam.NikVamScreen
import ir.foroosh.calc.ui.practical.PracticalScreen
import ir.foroosh.calc.ui.settings.SettingsScreen
import ir.foroosh.calc.ui.shayan.ShayanScreen
import ir.foroosh.calc.ui.theme.ForooshTheme
import ir.foroosh.calc.ui.theme.Red600
import java.util.Locale

/** اکتیویتی اصلی — FragmentActivity برای پشتیبانی BiometricPrompt */
class MainActivity : FragmentActivity() {

    override fun attachBaseContext(newBase: android.content.Context) {
        // زبان فارسی و راست‌به‌چپ برای کل برنامه
        val locale = Locale("fa")
        val config = Configuration(newBase.resources.configuration)
        config.setLocale(locale)
        super.attachBaseContext(newBase.createConfigurationContext(config))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val store = SettingsStore(this)
        enableEdgeToEdge()
        setContent {
            var themeMode by remember { mutableStateOf(store.themeMode) }
            var locked by remember { mutableStateOf(store.pinEnabled) }

            ForooshTheme(themeMode = themeMode) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Surface(
                        Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        if (locked) {
                            LockScreen(store, onUnlock = { locked = false })
                        } else {
                            MainScaffold(
                                store = store,
                                themeMode = themeMode,
                                onThemeChange = {
                                    store.themeMode = it
                                    themeMode = it
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

private data class TabItem(val title: String, val short: String, val icon: ImageVector)

private val tabs = listOf(
    TabItem("شایان", "شایان", Icons.Filled.Percent),
    TabItem("نیک وام", "نیک وام", Icons.Filled.VolunteerActivism),
    TabItem("محاسبات کاربردی", "کاربردی", Icons.Filled.Calculate),
    TabItem("محاسبات گشایش اعتبار اسنادی", "اعتبار اسنادی", Icons.Filled.ReceiptLong),
    TabItem("تنظیمات", "تنظیمات", Icons.Filled.Settings),
)

@Composable
private fun MainScaffold(store: SettingsStore, themeMode: ir.foroosh.calc.data.ThemeMode, onThemeChange: (ir.foroosh.calc.data.ThemeMode) -> Unit) {
    var tab by rememberSaveable { mutableIntStateOf(0) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                tonalElevation = 3.dp
            ) {
                tabs.forEachIndexed { i, t ->
                    val selected = tab == i
                    NavigationBarItem(
                        selected = selected,
                        onClick = { tab = i },
                        icon = {
                            Icon(
                                t.icon,
                                contentDescription = t.title,
                                modifier = Modifier.size(23.dp)
                            )
                        },
                        label = {
                            Text(
                                t.short,
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 1
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                            selectedTextColor = MaterialTheme.colorScheme.onBackground,
                            indicatorColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    )
                }
            }
        }
    ) { pad ->
        Column(
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(pad)
                .imePadding()
        ) {
            // هدر برنامه
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceContainerLow)
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Surface(
                        color = Red600,
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp),
                        modifier = Modifier.size(width = 46.dp, height = 30.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = androidx.compose.ui.Alignment.Center
                        ) {
                            Text(
                                "فروش",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }
                    }
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(
                            tabs[tab].title,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            "محاسبه‌گر تسهیلات و تعهدات بانکی",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // محتوای تب فعال
            Column(
                Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                when (tab) {
                    0 -> ShayanScreen()
                    1 -> NikVamScreen()
                    2 -> PracticalScreen()
                    3 -> LcScreen()
                    4 -> SettingsScreen(store, themeMode, onThemeChange)
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

