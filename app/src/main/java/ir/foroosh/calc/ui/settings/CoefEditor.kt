package ir.foroosh.calc.ui.settings

import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import ir.foroosh.calc.data.CoefficientRepo
import ir.foroosh.calc.data.CoefRow
import ir.foroosh.calc.data.Coefficients
import ir.foroosh.calc.data.DepositPlans
import ir.foroosh.calc.data.PlanRepo
import ir.foroosh.calc.data.PlanRow
import ir.foroosh.calc.data.TitlesRepo
import ir.foroosh.calc.ui.common.SectionCard
import ir.foroosh.calc.ui.theme.Red600
import ir.foroosh.calc.ui.theme.Yellow500
import ir.foroosh.calc.ui.theme.Vazir

/** یک ردیف جدول ضرایب در حالت ویرایش (درصد به‌صورت عدد درصدی؛ ۵۰ یعنی ۵۰٪) */
private data class EditRow(val rate: String, val avg: String, val repay: String, val pct: String)

/** یک ردیف «جدول طرح ها» در حالت ویرایش */
private data class EditPlanRow(
    val name: String, val group: String, val payNo: String,
    val minDep: String, val months: String, val rate: String, val note: String
)

/**
 * ویرایشگر جداول — پس از ورود رمز امنیتی باز می‌شود.
 * سه جدول قابل ویرایش است: ضرایب شایان، ضرایب نیک وام و «جدول طرح ها» (سپرده‌گذاری).
 */
@Composable
fun CoefEditorPane(onExit: () -> Unit) {

    val ctx = LocalContext.current

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

    fun fromPlan(p: PlanRow) = EditPlanRow(
        name = p.name,
        group = p.group,
        payNo = p.payNo.toString(),
        minDep = p.minDeposit.toLong().toString(),
        months = p.minMonths.toString(),
        rate = if (p.rate == Math.floor(p.rate)) p.rate.toLong().toString()
        else String.format(java.util.Locale.US, "%.2f", p.rate).trimEnd('0').trimEnd('.'),
        note = p.note
    )

    fun snapshot(rows: List<EditRow>) = rows.joinToString("|") { "${it.rate},${it.avg},${it.repay},${it.pct}" }
    fun snapshotPlans(rows: List<EditPlanRow>) =
        rows.joinToString("|") { "${it.name}~${it.group}~${it.payNo}~${it.minDep}~${it.months}~${it.rate}~${it.note}" }

    var which by remember { mutableIntStateOf(0) } // 0=شایان، 1=نیک وام، 2=طرح ها
    var rows by remember { mutableStateOf(Coefficients.shayan.map { fromCoef(it) }) }
    var saved by remember { mutableStateOf(snapshot(Coefficients.shayan.map { fromCoef(it) })) }
    var planRows by remember { mutableStateOf(DepositPlans.plans.map { fromPlan(it) }) }
    var savedPlans by remember { mutableStateOf(snapshotPlans(DepositPlans.plans.map { fromPlan(it) })) }
    var err by remember { mutableStateOf<String?>(null) }
    var confirmBack by remember { mutableStateOf(false) }
    var confirmReset by remember { mutableStateOf(false) }
    var pendingWhich by remember { mutableStateOf<Int?>(null) }

    fun dirty(): Boolean =
        if (which == 2) snapshotPlans(planRows) != savedPlans else snapshot(rows) != saved

    fun loadTable(w: Int) {
        which = w
        err = null
        if (w == 2) {
            planRows = DepositPlans.plans.map { fromPlan(it) }
            savedPlans = snapshotPlans(planRows)
        } else {
            val list = (if (w == 0) Coefficients.shayan else Coefficients.nikvam).map { fromCoef(it) }
            rows = list
            saved = snapshot(list)
        }
    }

    fun valid(r: EditRow): Boolean {
        val rate = r.rate.toIntOrNull() ?: return false
        val avg = r.avg.toIntOrNull() ?: return false
        val repay = r.repay.toIntOrNull() ?: return false
        val pct = r.pct.toDoubleOrNull() ?: return false
        return rate in 1..99 && avg in 1..360 && repay in 1..360 && pct > 0.0 && pct <= 1000.0
    }

    fun validPlan(r: EditPlanRow): Boolean {
        if (r.name.isBlank() || r.group.isBlank()) return false
        val payNo = r.payNo.toIntOrNull() ?: return false
        val dep = r.minDep.toLongOrNull() ?: return false
        val months = r.months.toIntOrNull() ?: return false
        val rate = r.rate.toDoubleOrNull() ?: return false
        return payNo >= 0 && dep >= 0 && months in 1..360 && rate > 0.0 && rate <= 100.0
    }

    fun save() {
        if (which == 2) {
            val bad = planRows.indexOfFirst { !validPlan(it) }
            if (bad >= 0) {
                err = "ردیف ${bad + 1}: عنوان و گروه را خالی نگذارید؛ شماره و حداقل سپرده عددی، ماندگاری ۱ تا ۳۶۰ ماه و نرخ بین ۰ تا ۱۰۰ باشد"
                return
            }
            PlanRepo.save(
                planRows.map {
                    PlanRow(
                        name = it.name.trim(),
                        group = it.group.trim(),
                        payNo = it.payNo.toInt(),
                        minDeposit = it.minDep.toLong().toDouble(),
                        minMonths = it.months.toInt(),
                        rate = it.rate.toDouble(),
                        note = it.note.trim()
                    )
                }
            )
            Toast.makeText(ctx, "جدول طرح ها با موفقیت ذخیره شد", Toast.LENGTH_SHORT).show()
            onExit()
            return
        }
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
        Toast.makeText(ctx, "جدول ضرایب با موفقیت ذخیره شد", Toast.LENGTH_SHORT).show()
        onExit()
    }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {

        // ---------- هدر ویرایشگر ----------
        SectionCard(title = "ویرایش جداول ضرایب و طرح ها", accentColor = Yellow500) {
            Text(
                if (which == 2)
                    "عنوان، گروه، شماره پرداخت، حداقل سپرده، حداقل ماندگاری (ماه)، نرخ سالانه و توضیح هر طرح را ویرایش کنید. تغییرات با «ذخیره» اعمال و در حافظهٔ برنامه ماندگار می‌شود."
                else
                    "مقادیر نرخ، دوره معدل‌گیری، حداکثر دوره بازپرداخت و درصد تسهیلات (نسبت به سپرده) هر ردیف را ویرایش کنید. درصد را مثل نمایش جدول وارد کنید (۵۰ یعنی ۵۰٪ و ۳۲۰ یعنی ۳.۲ برابر سپرده). تغییرات با «ذخیره» اعمال و در حافظهٔ برنامه ماندگار می‌شود.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(10.dp))
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = which == 0,
                    onClick = { if (which != 0) { if (dirty()) pendingWhich = 0 else loadTable(0) } },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3)
                ) { Text("جدول ${TitlesRepo.shayan}", style = MaterialTheme.typography.labelMedium) }
                SegmentedButton(
                    selected = which == 1,
                    onClick = { if (which != 1) { if (dirty()) pendingWhich = 1 else loadTable(1) } },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3)
                ) { Text("جدول ${TitlesRepo.nikvam}", style = MaterialTheme.typography.labelMedium) }
                SegmentedButton(
                    selected = which == 2,
                    onClick = { if (which != 2) { if (dirty()) pendingWhich = 2 else loadTable(2) } },
                    shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3)
                ) { Text("جدول طرح ها", style = MaterialTheme.typography.labelMedium) }
            }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                TextButton(onClick = { if (dirty()) confirmBack = true else onExit() }) {
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

        // ---------- ردیف‌های جدول جاری ----------
        if (which == 2) {
            planRows.forEachIndexed { idx, r ->
                PlanEditCard(
                    idx = idx,
                    r = r,
                    isValid = validPlan(r),
                    canDelete = planRows.size > 1,
                    onName = { v -> planRows = planRows.toMutableList().also { it[idx] = r.copy(name = v.take(30)) } },
                    onGroup = { v -> planRows = planRows.toMutableList().also { it[idx] = r.copy(group = v.take(20)) } },
                    onPayNo = { v -> planRows = planRows.toMutableList().also { it[idx] = r.copy(payNo = v.filter { c -> c.isDigit() }.take(6)) } },
                    onMinDep = { v -> planRows = planRows.toMutableList().also { it[idx] = r.copy(minDep = v.filter { c -> c.isDigit() }.take(15)) } },
                    onMonths = { v -> planRows = planRows.toMutableList().also { it[idx] = r.copy(months = v.filter { c -> c.isDigit() }.take(3)) } },
                    onRate = { v -> planRows = planRows.toMutableList().also { it[idx] = r.copy(rate = v.filter { c -> c.isDigit() || c == '.' }.take(5)) } },
                    onNote = { v -> planRows = planRows.toMutableList().also { it[idx] = r.copy(note = v.take(80)) } },
                    onDelete = {
                        if (planRows.size > 1) {
                            planRows = planRows.toMutableList().also { it.removeAt(idx) }
                            err = null
                        }
                    }
                )
            }
        } else {
            rows.forEachIndexed { idx, r ->
                CoefEditCard(
                    idx = idx,
                    r = r,
                    isValid = valid(r),
                    canDelete = rows.size > 1,
                    onRate = { v -> rows = rows.toMutableList().also { it[idx] = r.copy(rate = v.filter { c -> c.isDigit() }.take(2)) } },
                    onAvg = { v -> rows = rows.toMutableList().also { it[idx] = r.copy(avg = v.filter { c -> c.isDigit() }.take(3)) } },
                    onRepay = { v -> rows = rows.toMutableList().also { it[idx] = r.copy(repay = v.filter { c -> c.isDigit() }.take(3)) } },
                    onPct = { v -> rows = rows.toMutableList().also { it[idx] = r.copy(pct = v.filter { c -> c.isDigit() || c == '.' }.take(5)) } },
                    onDelete = {
                        if (rows.size > 1) {
                            rows = rows.toMutableList().also { it.removeAt(idx) }
                            err = null
                        }
                    }
                )
            }
        }

        // ---------- افزودن ردیف / بازگردانی پیش‌فرض ----------
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            TextButton(onClick = {
                if (which == 2) {
                    val last = planRows.lastOrNull() ?: EditPlanRow("طرح جدید", "", "0", "0", "1", "20", "")
                    planRows = planRows + last
                } else {
                    val last = rows.lastOrNull() ?: EditRow("4", "12", "12", "10")
                    rows = rows + last
                }
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
            title = {
                Text(
                    "بازگردانی " + when (which) {
                        0 -> "جدول ${TitlesRepo.shayan}"
                        1 -> "جدول ${TitlesRepo.nikvam}"
                        else -> "جدول طرح ها"
                    }
                )
            },
            text = { Text("جدول به مقادیر پیش‌فرض فایل اکسل بازگردانده و ذخیره می‌شود. ادامه می‌دهید؟") },
            confirmButton = {
                TextButton(onClick = {
                    when (which) {
                        0 -> CoefficientRepo.resetShayan()
                        1 -> CoefficientRepo.resetNikvam()
                        else -> PlanRepo.reset()
                    }
                    loadTable(which)
                    confirmReset = false
                    Toast.makeText(ctx, "جدول به مقادیر پیش‌فرض اکسل بازگشت", Toast.LENGTH_SHORT).show()
                }) { Text("بازگردانی", color = Red600, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { confirmReset = false }) { Text("انصراف") }
            }
        )
    }
}

/** کارت ویرایش یک ردیف جدول ضرایب (شایان / نیک وام) */
@Composable
private fun CoefEditCard(
    idx: Int,
    r: EditRow,
    isValid: Boolean,
    canDelete: Boolean,
    onRate: (String) -> Unit,
    onAvg: (String) -> Unit,
    onRepay: (String) -> Unit,
    onPct: (String) -> Unit,
    onDelete: () -> Unit
) {
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
                IconButton(onClick = onDelete, enabled = canDelete) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = "حذف ردیف",
                        tint = if (canDelete) Red600 else MaterialTheme.colorScheme.outlineVariant,
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
                    onValueChange = onRate,
                    label = { Text("نرخ", style = MaterialTheme.typography.labelSmall, fontFamily = Vazir) },
                    textStyle = MaterialTheme.typography.titleSmall.copy(fontFamily = Vazir, fontWeight = FontWeight.Bold),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    isError = !isValid && r.rate.isNotEmpty(),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = r.avg,
                    onValueChange = onAvg,
                    label = { Text("معدل", style = MaterialTheme.typography.labelSmall, fontFamily = Vazir) },
                    textStyle = MaterialTheme.typography.titleSmall.copy(fontFamily = Vazir, fontWeight = FontWeight.Bold),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    isError = !isValid && r.avg.isNotEmpty(),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = r.repay,
                    onValueChange = onRepay,
                    label = { Text("بازپرداخت", style = MaterialTheme.typography.labelSmall, fontFamily = Vazir) },
                    textStyle = MaterialTheme.typography.titleSmall.copy(fontFamily = Vazir, fontWeight = FontWeight.Bold),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    isError = !isValid && r.repay.isNotEmpty(),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = r.pct,
                    onValueChange = onPct,
                    label = { Text("درصد", style = MaterialTheme.typography.labelSmall, fontFamily = Vazir) },
                    textStyle = MaterialTheme.typography.titleSmall.copy(fontFamily = Vazir, fontWeight = FontWeight.Bold),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    isError = !isValid && r.pct.isNotEmpty(),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/** کارت ویرایش یک ردیف «جدول طرح ها» */
@Composable
private fun PlanEditCard(
    idx: Int,
    r: EditPlanRow,
    isValid: Boolean,
    canDelete: Boolean,
    onName: (String) -> Unit,
    onGroup: (String) -> Unit,
    onPayNo: (String) -> Unit,
    onMinDep: (String) -> Unit,
    onMonths: (String) -> Unit,
    onRate: (String) -> Unit,
    onNote: (String) -> Unit,
    onDelete: () -> Unit
) {
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
                    "طرح ${idx + 1}",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                IconButton(onClick = onDelete, enabled = canDelete) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = "حذف طرح",
                        tint = if (canDelete) Red600 else MaterialTheme.colorScheme.outlineVariant,
                        modifier = Modifier.width(20.dp)
                    )
                }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = r.name,
                    onValueChange = onName,
                    label = { Text("عنوان طرح", style = MaterialTheme.typography.labelSmall, fontFamily = Vazir) },
                    textStyle = MaterialTheme.typography.titleSmall.copy(fontFamily = Vazir, fontWeight = FontWeight.Bold),
                    singleLine = true,
                    isError = !isValid && r.name.isEmpty(),
                    modifier = Modifier.weight(1.4f)
                )
                OutlinedTextField(
                    value = r.group,
                    onValueChange = onGroup,
                    label = { Text("گروه", style = MaterialTheme.typography.labelSmall, fontFamily = Vazir) },
                    textStyle = MaterialTheme.typography.titleSmall.copy(fontFamily = Vazir, fontWeight = FontWeight.Bold),
                    singleLine = true,
                    isError = !isValid && r.group.isEmpty(),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = r.payNo,
                    onValueChange = onPayNo,
                    label = { Text("شماره پرداخت", style = MaterialTheme.typography.labelSmall, fontFamily = Vazir) },
                    textStyle = MaterialTheme.typography.titleSmall.copy(fontFamily = Vazir, fontWeight = FontWeight.Bold),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    isError = !isValid && r.payNo.isNotEmpty(),
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = r.minDep,
                    onValueChange = onMinDep,
                    label = { Text("حداقل سپرده (ریال)", style = MaterialTheme.typography.labelSmall, fontFamily = Vazir) },
                    textStyle = MaterialTheme.typography.titleSmall.copy(fontFamily = Vazir, fontWeight = FontWeight.Bold),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    isError = !isValid && r.minDep.isNotEmpty(),
                    modifier = Modifier.weight(1.6f)
                )
                OutlinedTextField(
                    value = r.months,
                    onValueChange = onMonths,
                    label = { Text("ماندگاری (ماه)", style = MaterialTheme.typography.labelSmall, fontFamily = Vazir) },
                    textStyle = MaterialTheme.typography.titleSmall.copy(fontFamily = Vazir, fontWeight = FontWeight.Bold),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    isError = !isValid && r.months.isNotEmpty(),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = r.rate,
                    onValueChange = onRate,
                    label = { Text("نرخ (٪)", style = MaterialTheme.typography.labelSmall, fontFamily = Vazir) },
                    textStyle = MaterialTheme.typography.titleSmall.copy(fontFamily = Vazir, fontWeight = FontWeight.Bold),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    isError = !isValid && r.rate.isNotEmpty(),
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = r.note,
                onValueChange = onNote,
                label = { Text("توضیحات", style = MaterialTheme.typography.labelSmall, fontFamily = Vazir) },
                textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = Vazir),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
