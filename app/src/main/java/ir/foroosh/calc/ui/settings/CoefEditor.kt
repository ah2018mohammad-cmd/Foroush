package ir.foroosh.calc.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import ir.foroosh.calc.data.CoefficientRepo
import ir.foroosh.calc.data.CoefRow
import ir.foroosh.calc.data.Coefficients
import ir.foroosh.calc.ui.common.SectionCard
import ir.foroosh.calc.ui.theme.Red600
import ir.foroosh.calc.ui.theme.Yellow500
import ir.foroosh.calc.ui.theme.Vazir

/**
 * ویرایشگر جداول ضرایب — پس از ورود رمز امنیتی باز می‌شود.
 * نرخ، دوره معدل‌گیری، حداکثر بازپرداخت و درصد تسهیلات نسبت به سپرده
 * برای هر دو جدول «شایان» و «نیک وام» قابل ویرایش است.
 */
@Composable
fun CoefEditorPane(onExit: () -> Unit) {

    /** یک ردیف جدول در حالت ویرایش (درصد به‌صورت عدد درصدی؛ ۵۰ یعنی ۵۰٪) */
    data class EditRow(val rate: String, val avg: String, val repay: String, val pct: String)

    fun fromCoef(r: CoefRow) = EditRow(
        rate = r.rate.toString(),
        avg = r.avg.toString(),
        repay = r.repay.toString(),
        pct = run {
            val p = r.pct * 100.0
            if (p == Math.floor(p)) p.toLong().toString()
            else String.format(java.util.Locale.US, "%.2f", p).trimEnd('0').trimEnd('.')
        }
    )

    fun snapshot(rows: List<EditRow>) = rows.joinToString("|") { "${it.rate},${it.avg},${it.repay},${it.pct}" }

    var which by remember { mutableIntStateOf(0) } // 0=شایان، 1=نیک وام
    var rows by remember { mutableStateOf(Coefficients.shayan.map { fromCoef(it) }) }
    var saved by remember { mutableStateOf(snapshot(Coefficients.shayan.map { fromCoef(it) })) }
    var err by remember { mutableStateOf<String?>(null) }
    var confirmBack by remember { mutableStateOf(false) }
    var confirmReset by remember { mutableStateOf(false) }
    var pendingWhich by remember { mutableStateOf<Int?>(null) }

    fun loadTable(w: Int) {
        which = w
        val list = (if (w == 0) Coefficients.shayan else Coefficients.nikvam).map { fromCoef(it) }
        rows = list
        saved = snapshot(list)
        err = null
    }

    fun valid(r: EditRow): Boolean {
        val rate = r.rate.toIntOrNull() ?: return false
        val avg = r.avg.toIntOrNull() ?: return false
        val repay = r.repay.toIntOrNull() ?: return false
        val pct = r.pct.toDoubleOrNull() ?: return false
        return rate in 1..99 && avg in 1..360 && repay in 1..360 && pct > 0.0 && pct <= 1000.0
    }

    fun save() {
        val bad = rows.indexOfFirst { !valid(it) }
        if (bad >= 0) {
            err = "ردیف ${bad + 1}: مقادیر معتبر وارد کنید (نرخ ۱ تا ۹۹، معدل و بازپرداخت ۱ تا ۳۶۰، درصد بیشتر از صفر)"
            return
        }
        if (rows.isEmpty()) {
            err = "جدول نمی‌تواند خالی باشد"
            return
        }
        val list = rows.map {
            CoefRow(
                rate = it.rate.toInt(),
                avg = it.avg.toInt(),
                repay = it.repay.toInt(),
                pct = it.pct.toDouble() / 100.0
            )
        }
        if (which == 0) CoefficientRepo.saveShayan(list) else CoefficientRepo.saveNikvam(list)
        onExit()
    }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {

        // ---------- هدر ویرایشگر ----------
        SectionCard(title = "ویرایش جداول ضرایب", accentColor = Yellow500) {
            Text(
                "مقادیر نرخ، دوره معدل‌گیری، حداکثر دوره بازپرداخت و درصد تسهیلات (نسبت به سپرده) هر ردیف را ویرایش کنید. درصد را مثل نمایش جدول وارد کنید (۵۰ یعنی ۵۰٪ و ۳۲۰ یعنی ۳.۲ برابر سپرده). تغییرات با «ذخیره» اعمال و در حافظهٔ برنامه ماندگار می‌شود.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(10.dp))
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = which == 0,
                    onClick = { if (which != 0) { if (snapshot(rows) != saved) pendingWhich = 0 else loadTable(0) } },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                ) { Text("جدول شایان", style = MaterialTheme.typography.labelMedium) }
                SegmentedButton(
                    selected = which == 1,
                    onClick = { if (which != 1) { if (snapshot(rows) != saved) pendingWhich = 1 else loadTable(1) } },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                ) { Text("جدول نیک وام", style = MaterialTheme.typography.labelMedium) }
            }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                TextButton(onClick = { if (snapshot(rows) != saved) confirmBack = true else onExit() }) {
                    Text("بازگشت", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                TextButton(onClick = { save() }) {
                    Icon(Icons.Filled.Save, contentDescription = null, tint = Red600, modifier = Modifier.width(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("ذخیره", fontWeight = FontWeight.ExtraBold, color = Red600)
                }
            }
            if (err != null) {
                Spacer(Modifier.height(6.dp))
                Text(err ?: "", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
        }

        // ---------- ردیف‌های جدول ----------
        rows.forEachIndexed { idx, r ->
            Card(
                shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "ردیف ${idx + 1}",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        IconButton(
                            onClick = {
                                if (rows.size > 1) {
                                    rows = rows.toMutableList().also { it.removeAt(idx) }
                                    err = null
                                }
                            },
                            enabled = rows.size > 1
                        ) {
                            Icon(
                                Icons.Filled.Delete,
                                contentDescription = "حذف ردیف",
                                tint = if (rows.size > 1) Red600 else MaterialTheme.colorScheme.outlineVariant,
                                modifier = Modifier.width(20.dp)
                            )
                        }
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = r.rate,
                            onValueChange = { v -> rows = rows.toMutableList().also { it[idx] = r.copy(rate = v.filter { c -> c.isDigit() }.take(2)) } },
                            label = { Text("نرخ", style = MaterialTheme.typography.labelSmall, fontFamily = Vazir) },
                            textStyle = MaterialTheme.typography.titleSmall.copy(fontFamily = Vazir, fontWeight = FontWeight.Bold),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            isError = !valid(r) && r.rate.isNotEmpty(),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = r.avg,
                            onValueChange = { v -> rows = rows.toMutableList().also { it[idx] = r.copy(avg = v.filter { c -> c.isDigit() }.take(3)) } },
                            label = { Text("معدل", style = MaterialTheme.typography.labelSmall, fontFamily = Vazir) },
                            textStyle = MaterialTheme.typography.titleSmall.copy(fontFamily = Vazir, fontWeight = FontWeight.Bold),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            isError = !valid(r) && r.avg.isNotEmpty(),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = r.repay,
                            onValueChange = { v -> rows = rows.toMutableList().also { it[idx] = r.copy(repay = v.filter { c -> c.isDigit() }.take(3)) } },
                            label = { Text("بازپرداخت", style = MaterialTheme.typography.labelSmall, fontFamily = Vazir) },
                            textStyle = MaterialTheme.typography.titleSmall.copy(fontFamily = Vazir, fontWeight = FontWeight.Bold),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            isError = !valid(r) && r.repay.isNotEmpty(),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = r.pct,
                            onValueChange = { v -> rows = rows.toMutableList().also { it[idx] = r.copy(pct = v.filter { c -> c.isDigit() || c == '.' }.take(5)) } },
                            label = { Text("درصد", style = MaterialTheme.typography.labelSmall, fontFamily = Vazir) },
                            textStyle = MaterialTheme.typography.titleSmall.copy(fontFamily = Vazir, fontWeight = FontWeight.Bold),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            isError = !valid(r) && r.pct.isNotEmpty(),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // ---------- افزودن ردیف / بازگردانی پیش‌فرض ----------
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            TextButton(onClick = {
                val last = rows.lastOrNull() ?: EditRow("4", "12", "12", "10")
                rows = rows + last
                err = null
            }) {
                Icon(Icons.Filled.Add, contentDescription = null, tint = Red600, modifier = Modifier.width(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("افزودن ردیف (مشابه آخرین)", fontWeight = FontWeight.Bold, color = Red600)
            }
            TextButton(onClick = { confirmReset = true }) {
                Icon(Icons.Filled.Restore, contentDescription = null, tint = Yellow500, modifier = Modifier.width(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("بازگردانی پیش‌فرض اکسل", fontWeight = FontWeight.Bold, color = Yellow500)
            }
        }
    }

    // ---------- دیالوگ‌ها ----------
    if (confirmBack) {
        AlertDialog(
            onDismissRequest = { confirmBack = false },
            title = { Text("تغییرات ذخیره نشده") },
            text = { Text("با ادامه، تغییرات این جدول از بین می‌رود. ادامه می‌دهید؟") },
            confirmButton = {
                TextButton(onClick = {
                    confirmBack = false
                    val target = pendingWhich
                    pendingWhich = null
                    if (target != null) loadTable(target) else onExit()
                }) {
                    Text("ادامه بدون ذخیره", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmBack = false }) { Text("ادامه ویرایش") }
            }
        )
    }
    if (confirmReset) {
        AlertDialog(
            onDismissRequest = { confirmReset = false },
            title = { Text("بازگردانی جدول ${if (which == 0) "شایان" else "نیک وام"}") },
            text = { Text("جدول به مقادیر پیش‌فرض فایل اکسل بازگردانده و ذخیره می‌شود. ادامه می‌دهید؟") },
            confirmButton = {
                TextButton(onClick = {
                    if (which == 0) CoefficientRepo.resetShayan() else CoefficientRepo.resetNikvam()
                    loadTable(which)
                    confirmReset = false
                }) { Text("بازگردانی", color = Red600, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { confirmReset = false }) { Text("انصراف") }
            }
        )
    }
}
