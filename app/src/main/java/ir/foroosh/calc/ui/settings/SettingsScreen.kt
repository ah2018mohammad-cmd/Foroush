package ir.foroosh.calc.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import ir.foroosh.calc.data.SettingsStore
import ir.foroosh.calc.data.ThemeMode
import ir.foroosh.calc.ui.common.SectionCard
import ir.foroosh.calc.ui.shayan.CoefTableDialog
import ir.foroosh.calc.ui.theme.Red600
import ir.foroosh.calc.ui.theme.Vazir
import ir.foroosh.calc.ui.theme.Yellow500
import ir.foroosh.calc.data.Coefficients

/** تب «تنظیمات» — رمز عددی، بیومتریک، تم سه‌حالته و مشاهده جداول ضرایب */
@Composable
fun SettingsScreen(
    store: SettingsStore,
    themeMode: ThemeMode,
    onThemeChange: (ThemeMode) -> Unit
) {
    var pinEnabledUi by rememberSaveable { mutableStateOf(store.pinEnabled) }
    var bioUi by rememberSaveable { mutableStateOf(store.biometricEnabled) }
    var showSetPin by remember { mutableStateOf(false) }
    var showChangePin by remember { mutableStateOf(false) }
    var showDisableConfirm by remember { mutableStateOf(false) }
    var showTable by remember { mutableStateOf(false) }
    var tableWhich by remember { mutableIntStateOf(0) }
    val canBio = store.canBiometric()

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {

        // ---------- قفل برنامه ----------
        SectionCard(title = "قفل برنامه") {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.Key,
                        contentDescription = null,
                        tint = Red600,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text("ورود با رمز عددی", style = MaterialTheme.typography.titleSmall)
                        Text(
                            "قفل شدن برنامه هنگام شروع با رمز ۴ تا ۶ رقمی",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Switch(
                    checked = pinEnabledUi,
                    onCheckedChange = { on ->
                        if (on) {
                            showSetPin = true
                        } else {
                            showDisableConfirm = true
                        }
                    }
                )
            }

            if (pinEnabledUi) {
                Spacer(Modifier.height(10.dp))
                TextButton(onClick = { showChangePin = true }) {
                    Text("تغییر رمز", fontWeight = FontWeight.Bold, color = Red600)
                }

                Spacer(Modifier.height(6.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            Icons.Filled.Fingerprint,
                            contentDescription = null,
                            tint = if (bioUi) Yellow500 else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text("ورود با بیومتریک", style = MaterialTheme.typography.titleSmall)
                            Text(
                                if (canBio) "اثر انگشت یا چهره دستگاه" else "این دستگاه از بیومتریک پشتیبانی نمی‌کند",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Switch(
                        checked = bioUi,
                        enabled = canBio,
                        onCheckedChange = { on ->
                            store.setBiometric(on)
                            bioUi = on
                        }
                    )
                }
            }
        }

        // ---------- ظاهر برنامه ----------
        SectionCard(title = "ظاهر برنامه") {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.Palette,
                    contentDescription = null,
                    tint = Red600,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(Modifier.width(10.dp))
                Column {
                    Text("تم برنامه", style = MaterialTheme.typography.titleSmall)
                    Text(
                        "انتخاب بین حالت سیستمی، سفید و خاکستری",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            ThemeMode.entries.forEach { mode ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable { onThemeChange(mode) }
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = themeMode == mode,
                        onClick = { onThemeChange(mode) }
                    )
                    Text(mode.label, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }

        // ---------- جداول ضرایب ----------
        SectionCard(title = "جداول ضرایب", accentColor = Yellow500) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.TableChart,
                    contentDescription = null,
                    tint = Yellow500,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(Modifier.width(10.dp))
                Column {
                    Text("مشاهده جداول ضرایب", style = MaterialTheme.typography.titleSmall)
                    Text(
                        "ضرایب استفاده‌شده در محاسبات، مطابق فایل اکسل فروش",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = { tableWhich = 0; showTable = true }) {
                    Text("جدول شایان", fontWeight = FontWeight.Bold, color = Red600)
                }
                TextButton(onClick = { tableWhich = 1; showTable = true }) {
                    Text("جدول نیک وام", fontWeight = FontWeight.Bold, color = Red600)
                }
            }
        }

        // ---------- درباره ----------
        SectionCard(title = "درباره برنامه", accentColor = MaterialTheme.colorScheme.primary) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(Modifier.width(10.dp))
                Column {
                    Text("فروش — نسخه ۱.۰.۰", style = MaterialTheme.typography.titleSmall)
                    Text(
                        "محاسبه‌گر تسهیلات و تعهدات بانکی؛ فرمول‌ها و ضرایب دقیقاً مطابق فایل اکسل فروش (شایان، نیک وام، محاسبات کاربردی و گشایش اعتبار اسنادی داخلی ریالی).",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    // ---------- دیالوگ تنظیم رمز جدید ----------
    if (showSetPin) {
        PinDialog(
            title = "تعیین رمز جدید",
            withCurrent = false,
            store = store,
            onDismiss = { showSetPin = false },
            onDone = {
                store.setPin(it)
                pinEnabledUi = true
                showSetPin = false
            }
        )
    }
    if (showChangePin) {
        PinDialog(
            title = "تغییر رمز",
            withCurrent = true,
            store = store,
            onDismiss = { showChangePin = false },
            onDone = {
                store.setPin(it)
                showChangePin = false
            }
        )
    }
    if (showDisableConfirm) {
        AlertDialog(
            onDismissRequest = { showDisableConfirm = false },
            title = { Text("حذف رمز") },
            text = { Text("آیا مطمئن هستید؟ با حذف رمز، بیومتریک هم غیرفعال می‌شود.") },
            confirmButton = {
                TextButton(onClick = {
                    store.disablePin()
                    pinEnabledUi = false
                    bioUi = false
                    showDisableConfirm = false
                }) { Text("حذف شود", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDisableConfirm = false }) { Text("انصراف") }
            }
        )
    }

    if (showTable) {
        if (tableWhich == 0) {
            CoefTableDialog(
                title = "جدول ضرایب شایان",
                rows = Coefficients.shayan,
                highlight = null,
                onDismiss = { showTable = false }
            )
        } else {
            CoefTableDialog(
                title = "جدول ضرایب نیک وام",
                rows = Coefficients.nikvam,
                highlight = null,
                onDismiss = { showTable = false }
            )
        }
    }
}

/** دیالوگ ورود رمز (با تأیید رمز فعلی در حالت تغییر) */
@Composable
private fun PinDialog(
    title: String,
    withCurrent: Boolean,
    store: SettingsStore,
    onDismiss: () -> Unit,
    onDone: (String) -> Unit
) {
    var current by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var err by remember { mutableStateOf<String?>(null) }

    fun validate(): String? {
        if (withCurrent && !store.verifyPin(current)) return "رمز فعلی نادرست است"
        if (pin.length !in 4..6) return "رمز باید ۴ تا ۶ رقم باشد"
        if (pin != confirm) return "تکرار رمز مطابقت ندارد"
        return null
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                if (withCurrent) {
                    OutlinedTextField(
                        value = current,
                        onValueChange = { if (it.all { c -> c.isDigit() }) current = it.take(6) },
                        label = { Text("رمز فعلی") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(10.dp))
                }
                OutlinedTextField(
                    value = pin,
                    onValueChange = { if (it.all { c -> c.isDigit() }) pin = it.take(6) },
                    label = { Text("رمز جدید (۴ تا ۶ رقم)") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = confirm,
                    onValueChange = { if (it.all { c -> c.isDigit() }) confirm = it.take(6) },
                    label = { Text("تکرار رمز جدید") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (err != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(err ?: "", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val e = validate()
                if (e == null) onDone(pin) else err = e
            }) { Text("ذخیره", fontWeight = FontWeight.Bold, color = Red600) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("انصراف") }
        }
    )
}
