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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import ir.foroosh.calc.data.SettingsStore
import ir.foroosh.calc.data.ThemeMode
import ir.foroosh.calc.data.TitlesRepo
import ir.foroosh.calc.data.DepositPlans
import ir.foroosh.calc.ui.common.SectionCard
import ir.foroosh.calc.ui.deposit.PlansTableDialog
import ir.foroosh.calc.ui.shayan.CoefTableDialog
import ir.foroosh.calc.ui.theme.Red600
import ir.foroosh.calc.ui.theme.Vazir
import ir.foroosh.calc.ui.theme.Yellow500
import ir.foroosh.calc.data.Coefficients

/** تب «تنظیمات» — رمز عددی، بیومتریک، تم سه‌حالته و مشاهده/ویرایش جداول ضرایب (با رمز امنیتی) */
@Composable
fun SettingsScreen(
    store: SettingsStore,
    themeMode: ThemeMode,
    onThemeChange: (ThemeMode) -> Unit
) {
    // ---------- ویرایشگر جداول ضرایب و طرح ها (پس از ورود رمز امنیتی) ----------
    var editorOpen by remember { mutableStateOf(false) }
    if (editorOpen) {
        CoefEditorPane(onExit = { editorOpen = false })
        return
    }

    // ---------- ویرایشگر عناوین تسهیلات و طرح ها (پس از ورود رمز امنیتی) ----------
    var titleEditorOpen by remember { mutableStateOf(false) }
    if (titleEditorOpen) {
        TitlesEditorPane(onExit = { titleEditorOpen = false })
        return
    }

    var pinEnabledUi by rememberSaveable { mutableStateOf(store.pinEnabled) }
    var bioUi by rememberSaveable { mutableStateOf(store.biometricEnabled) }
    var showSetPin by remember { mutableStateOf(false) }
    var showChangePin by remember { mutableStateOf(false) }
    var showDisableConfirm by remember { mutableStateOf(false) }
    var showTable by remember { mutableStateOf(false) }
    var tableWhich by remember { mutableIntStateOf(0) }   // 0=شایان 1=نیک وام 2=طرح ها
    var showEditGate by remember { mutableStateOf(false) }
    var showTitleGate by remember { mutableStateOf(false) }
    var showSecChange by remember { mutableStateOf(false) }
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
                            "قفل شدن برنامه هنگام شروع با رمز ۸ رقمی",
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
                        "انتخاب بین حالت سیستمی، سفید روشن و مشکی تیره",
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

        // ---------- طرح های سپرده گذاری و تسهیلاتی ----------
        SectionCard(title = "طرح های سپرده گذاری و تسهیلاتی", accentColor = Yellow500) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.TableChart,
                    contentDescription = null,
                    tint = Yellow500,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(Modifier.width(10.dp))
                Column {
                    Text("مشاهده و ویرایش جداول ضرایب و طرح ها", style = MaterialTheme.typography.titleSmall)
                    Text(
                        "ضرایب و طرح‌ها مطابق فایل‌های اکسل؛ ویرایش نیازمند ورود رمز امنیتی است",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            // گزینه‌های مشاهده جداول به‌صورت عمودی و دکمه ویرایش به‌صورت افقی (تمام‌عرض)
            Column {
                TextButton(
                    onClick = { tableWhich = 0; showTable = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        Icons.Filled.TableChart,
                        contentDescription = null,
                        tint = Red600,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("جدول ${TitlesRepo.shayan}", fontWeight = FontWeight.Bold, color = Red600)
                }
                TextButton(
                    onClick = { tableWhich = 1; showTable = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        Icons.Filled.TableChart,
                        contentDescription = null,
                        tint = Red600,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("جدول ${TitlesRepo.nikvam}", fontWeight = FontWeight.Bold, color = Red600)
                }
                TextButton(
                    onClick = { tableWhich = 2; showTable = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        Icons.Filled.TableChart,
                        contentDescription = null,
                        tint = Red600,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("جدول طرح ها", fontWeight = FontWeight.Bold, color = Red600)
                }
                TextButton(
                    onClick = { showEditGate = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        Icons.Filled.Edit,
                        contentDescription = null,
                        tint = Yellow500,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("ویرایش جداول", fontWeight = FontWeight.ExtraBold, color = Yellow500)
                }
            }
            Spacer(Modifier.height(2.dp))
            TextButton(onClick = { showSecChange = true }) {
                Icon(
                    Icons.Filled.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    "تغییر رمز امنیتی جداول",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // ---------- ویرایش تسهیلات و طرح های سپرده گذاری ----------
        SectionCard(title = "ویرایش تسهیلات و طرح های سپرده گذاری", accentColor = Red600) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.Edit,
                    contentDescription = null,
                    tint = Red600,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(Modifier.width(10.dp))
                Column {
                    Text("ویرایش عناوین", style = MaterialTheme.typography.titleSmall)
                    Text(
                        "تغییر عنوان هر تسهیلات و هر طرح سپرده‌گذاری؛ ورود با رمز امنیتی",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            TextButton(
                onClick = { showTitleGate = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    Icons.Filled.Edit,
                    contentDescription = null,
                    tint = Yellow500,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text("ورود به ویرایش عناوین", fontWeight = FontWeight.ExtraBold, color = Yellow500)
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
                title = "جدول ضرایب ${TitlesRepo.shayan}",
                rows = Coefficients.shayan,
                highlight = null,
                onDismiss = { showTable = false }
            )
        } else if (tableWhich == 1) {
            CoefTableDialog(
                title = "جدول ضرایب ${TitlesRepo.nikvam}",
                rows = Coefficients.nikvam,
                highlight = null,
                onDismiss = { showTable = false }
            )
        } else {
            PlansTableDialog(
                title = "جدول طرح ها",
                rows = DepositPlans.plans,
                onDismiss = { showTable = false }
            )
        }
    }

    // ---------- رمز امنیتی: دروازه ویرایش جداول ----------
    if (showEditGate) {
        SecurityPinGateDialog(
            store = store,
            onGranted = {
                showEditGate = false
                editorOpen = true
            },
            onDismiss = { showEditGate = false }
        )
    }

    // ---------- رمز امنیتی: دروازه ویرایش عناوین ----------
    if (showTitleGate) {
        SecurityPinGateDialog(
            store = store,
            onGranted = {
                showTitleGate = false
                titleEditorOpen = true
            },
            onDismiss = { showTitleGate = false }
        )
    }

    // ---------- تغییر رمز امنیتی ----------
    if (showSecChange) {
        SecurityPinChangeDialog(
            store = store,
            onDismiss = { showSecChange = false }
        )
    }
}

/** دیالوگ ورود رمز (با تأیید رمز فعلی در حالت تغییر) — رمز ۸ رقمی */
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
        if (pin.length != 8) return "رمز باید ۸ رقم باشد"
        if (pin != confirm) return "تکرار رمز مطابقت ندارد"
        return null
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            // اعداد از چپ به راست مثل ورود رمز در گوشی
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Column {
                    if (withCurrent) {
                        OutlinedTextField(
                            value = current,
                            onValueChange = { if (it.all { c -> c.isDigit() }) current = it.take(8) },
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
                        onValueChange = { if (it.all { c -> c.isDigit() }) pin = it.take(8) },
                        label = { Text("رمز جدید (۸ رقم)") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = confirm,
                        onValueChange = { if (it.all { c -> c.isDigit() }) confirm = it.take(8) },
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

/** دیالوگ ورود رمز امنیتی برای ویرایش جداول ضرایب */
@Composable
private fun SecurityPinGateDialog(
    store: SettingsStore,
    onGranted: () -> Unit,
    onDismiss: () -> Unit
) {
    var pin by remember { mutableStateOf("") }
    var err by remember { mutableStateOf<String?>(null) }
    var confirmResetSec by remember { mutableStateOf(false) }

    fun tryVerify() {
        if (store.verifySecurityPin(pin)) onGranted() else err = "رمز امنیتی نادرست است؛ اگر رمز پیش‌فرض است: 12345678"
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("رمز امنیتی") },
        text = {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Column {
                    Text(
                        "برای ادامه، رمز امنیتی را وارد کنید.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = pin,
                        onValueChange = { if (it.all { c -> c.isDigit() }) { pin = it.take(8); err = null } },
                        label = { Text("رمز امنیتی (۸ رقم)") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        singleLine = true,
                        isError = err != null,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (err != null) {
                        Spacer(Modifier.height(8.dp))
                        Text(err ?: "", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                    Spacer(Modifier.height(10.dp))
                    if (!store.securityPinChanged) {
                        // نمایش رمز پیش‌فرض تا کاربر بتواند وارد شود
                        Text(
                            "رمز امنیتی پیش‌فرض: 12345678",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        TextButton(onClick = { confirmResetSec = true }) {
                            Text(
                                "رمز را فراموش کرده‌اید؟ بازنشانی به رمز پیش‌فرض",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { tryVerify() }) { Text("ورود", fontWeight = FontWeight.Bold, color = Red600) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("انصراف") }
        }
    )

    if (confirmResetSec) {
        AlertDialog(
            onDismissRequest = { confirmResetSec = false },
            title = { Text("بازنشانی رمز امنیتی") },
            text = { Text("رمز امنیتی جداول به مقدار پیش‌فرض (12345678) بازگردانده می‌شود. ادامه می‌دهید؟") },
            confirmButton = {
                TextButton(onClick = {
                    store.resetSecurityPin()
                    confirmResetSec = false
                }) { Text("بازنشانی", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { confirmResetSec = false }) { Text("انصراف") }
            }
        )
    }
}

/** دیالوگ تغییر رمز امنیتی جداول ضرایب (رمز جدید ۸ رقم) */
@Composable
private fun SecurityPinChangeDialog(
    store: SettingsStore,
    onDismiss: () -> Unit
) {
    var current by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var err by remember { mutableStateOf<String?>(null) }

    fun validate(): String? {
        if (!store.verifySecurityPin(current)) return "رمز امنیتی فعلی نادرست است"
        if (pin.length != 8) return "رمز جدید باید ۸ رقم باشد"
        if (pin != confirm) return "تکرار رمز مطابقت ندارد"
        return null
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("تغییر رمز امنیتی جداول") },
        text = {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Column {
                    OutlinedTextField(
                        value = current,
                        onValueChange = { if (it.all { c -> c.isDigit() }) current = it.take(8) },
                        label = { Text("رمز امنیتی فعلی") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = pin,
                        onValueChange = { if (it.all { c -> c.isDigit() }) pin = it.take(8) },
                        label = { Text("رمز جدید (۸ رقم)") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = confirm,
                        onValueChange = { if (it.all { c -> c.isDigit() }) confirm = it.take(8) },
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
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val e = validate()
                if (e == null) {
                    store.setSecurityPin(pin)
                    onDismiss()
                } else err = e
            }) { Text("ذخیره", fontWeight = FontWeight.Bold, color = Red600) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("انصراف") }
        }
    )
}
