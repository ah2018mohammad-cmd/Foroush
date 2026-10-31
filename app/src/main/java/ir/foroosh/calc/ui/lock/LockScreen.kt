package ir.foroosh.calc.ui.lock

import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import ir.foroosh.calc.R
import ir.foroosh.calc.data.SettingsStore
import ir.foroosh.calc.ui.theme.Red600
import ir.foroosh.calc.ui.theme.Yellow500

/**
 * صفحه ورود رمز در شروع برنامه — پس‌زمینه: عکس شکوفه‌های بهاری انتخاب کاربر
 * با لایه تیره برای خوانایی بهتر
 */
@Composable
fun LockScreen(store: SettingsStore, onUnlock: () -> Unit) {
    val context = LocalContext.current
    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }
    var bioUsed by remember { mutableStateOf(false) }

    fun typeKey(k: String) {
        if (pin.length < 6) {
            pin += k
            error = false
        }
    }

    fun submit() {
        if (pin.length < 4) return
        if (store.verifyPin(pin)) onUnlock() else {
            error = true
            pin = ""
        }
    }

    fun showBiometric() {
        if (bioUsed) return
        (context as? FragmentActivity)?.let { act ->
            val prompt = BiometricPrompt(
                act,
                ContextCompat.getMainExecutor(act),
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        bioUsed = true
                        onUnlock()
                    }
                }
            )
            val info = BiometricPrompt.PromptInfo.Builder()
                .setTitle("ورود به «فروش»")
                .setSubtitle("احراز هویت با اثر انگشت یا چهره")
                .setNegativeButtonText("استفاده از رمز عددی")
                .build()
            prompt.authenticate(info)
        }
    }

    // ورود خودکار با بیومتریک هنگام شروع (در صورت فعال بودن)
    LaunchedEffect(Unit) {
        if (store.biometricEnabled && store.canBiometric()) showBiometric()
    }

    Box(Modifier.fillMaxSize()) {
        // ===== پس‌زمینه: عکس شکوفه‌های بهاری =====
        Image(
            painter = painterResource(id = R.drawable.lock_bg),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        // لایه تیره برای خوانایی
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0x9917181C), Color(0xCC17181C), Color(0xF017181C))
                    )
                )
        )

        Column(
            Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // نشان برنامه
            Surface(shape = CircleShape, color = Red600, modifier = Modifier.size(76.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        "فروش",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }
            }
            Spacer(Modifier.height(14.dp))
            Text(
                "محاسبه‌گر تسهیلات و تعهدات بانکی",
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFFE8E9EC)
            )
            Spacer(Modifier.height(26.dp))

            // نقاط رمز
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                repeat(6) { i ->
                    val filled = i < pin.length
                    Box(
                        Modifier
                            .size(15.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    filled && error -> Color(0xFFFF8A80)
                                    filled -> Yellow500
                                    else -> Color(0x55FFFFFF)
                                }
                            )
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            Text(
                if (error) "رمز نادرست است؛ دوباره تلاش کنید" else "رمز عددی خود را وارد کنید",
                style = MaterialTheme.typography.bodyMedium,
                color = if (error) Color(0xFFFF8A80) else Color(0xFFC9CCD3)
            )
            Spacer(Modifier.height(26.dp))

            // صفحه‌کلید عددی
            listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
            ).forEach { row ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(18.dp),
                    modifier = Modifier.padding(vertical = 7.dp)
                ) {
                    row.forEach { k -> KeyCap(k) { typeKey(k) } }
                }
            }
            // ردیف آخر: بیومتریک / صفر / پاک‌کن
            Row(
                horizontalArrangement = Arrangement.spacedBy(18.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 7.dp)
            ) {
                Box(Modifier.size(width = 74.dp, height = 58.dp), contentAlignment = Alignment.Center) {
                    if (store.biometricEnabled && store.canBiometric()) {
                        IconButton(onClick = { showBiometric() }) {
                            Icon(
                                Icons.Filled.Fingerprint,
                                contentDescription = "ورود بیومتریک",
                                tint = Yellow500,
                                modifier = Modifier.size(34.dp)
                            )
                        }
                    }
                }
                KeyCap("0") { typeKey("0") }
                Box(Modifier.size(width = 74.dp, height = 58.dp), contentAlignment = Alignment.Center) {
                    IconButton(onClick = {
                        if (pin.isNotEmpty()) pin = pin.dropLast(1)
                        error = false
                    }) {
                        Icon(
                            Icons.Filled.Backspace,
                            contentDescription = "حذف",
                            tint = Color(0xFFE8E9EC),
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
            // دکمه ورود
            val enterInteraction = remember { MutableInteractionSource() }
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (pin.length >= 4) Red600 else Color(0x33FFFFFF),
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(50.dp)
                    .clickable(
                        interactionSource = enterInteraction,
                        indication = null
                    ) { if (pin.length >= 4) submit() }
            ) {
                Row(
                    Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "ورود",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun KeyCap(label: String, onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = Color(0x2BFFFFFF),
        border = BorderStroke(1.dp, Color(0x33FFFFFF)),
        modifier = Modifier
            .size(width = 74.dp, height = 58.dp)
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                label,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}
