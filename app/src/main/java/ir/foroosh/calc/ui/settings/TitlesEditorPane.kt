package ir.foroosh.calc.ui.settings

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.unit.dp
import ir.foroosh.calc.data.DepositPlans
import ir.foroosh.calc.data.PlanRepo
import ir.foroosh.calc.data.TitlesRepo
import ir.foroosh.calc.ui.common.SectionCard
import ir.foroosh.calc.ui.theme.Red600
import ir.foroosh.calc.ui.theme.Vazir
import ir.foroosh.calc.ui.theme.Yellow500

/**
 * ویرایشگر عناوین — بخش «ویرایش تسهیلات و طرح های سپرده گذاری» (پس از ورود رمز امنیتی).
 * عنوان هر تسهیلات (شایان / نیک وام) و عنوان هر طرح سپرده‌گذاری از «جدول طرح ها»
 * از همین‌جا تغییر می‌کند و بلافاصله در کل برنامه اعمال می‌شود.
 */
@Composable
fun TitlesEditorPane(onExit: () -> Unit) {

    var shayanT by remember { mutableStateOf(TitlesRepo.shayan) }
    var nikvamT by remember { mutableStateOf(TitlesRepo.nikvam) }
    var planNames by remember { mutableStateOf(DepositPlans.plans.map { it.name }) }

    var editTarget by remember { mutableIntStateOf(-1) }   // -1 هیچ / 0,1 تسهیلات / 10+i طرح
    var editText by remember { mutableStateOf("") }

    val openEdit: (Int, String) -> Unit = { target, current ->
        editTarget = target
        editText = current
    }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {

        // ---------- راهنما و بازگشت ----------
        SectionCard(title = "ویرایش عناوین", accentColor = Yellow500) {
            Text(
                "عنوان هر تسهیلات و هر طرح سپرده‌گذاری را از این بخش تغییر دهید؛ عنوان جدید بلافاصله در تب‌ها، سربرگ‌ها و جدول‌ها جایگزین می‌شود. برای ویرایش، روی آیکن مداد هر ردیف بزنید.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = onExit) {
                Text("بازگشت", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        // ---------- عناوین تسهیلات ----------
        SectionCard(title = "عناوین تسهیلات", accentColor = Red600) {
            TitleRow(label = "تسهیلات اول (ضرایب شایان)", value = shayanT, onEdit = { openEdit(0, shayanT) })
            TitleRow(label = "تسهیلات دوم (قرض‌الحسنه)", value = nikvamT, onEdit = { openEdit(1, nikvamT) })
        }

        // ---------- عناوین طرح‌های سپرده ----------
        SectionCard(title = "عناوین طرح‌های سپرده گذاری", accentColor = Yellow500) {
            DepositPlans.plans.forEachIndexed { i, p ->
                TitleRow(
                    label = "${p.group} — پرداخت ${p.payNo}",
                    value = planNames.getOrElse(i) { p.name },
                    onEdit = { openEdit(10 + i, planNames.getOrElse(i) { p.name }) }
                )
            }
        }
    }

    // ---------- دیالوگ ویرایش عنوان ----------
    if (editTarget >= 0) {
        val isPlan = editTarget >= 10
        val ctx = LocalContext.current
        AlertDialog(
            onDismissRequest = { editTarget = -1 },
            title = { Text(if (isPlan) "ویرایش عنوان طرح" else "ویرایش عنوان تسهیلات") },
            text = {
                Column {
                    OutlinedTextField(
                        value = editText,
                        onValueChange = { editText = it.take(30) },
                        label = { Text("عنوان جدید", fontFamily = Vazir) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val v = editText.trim()
                        if (v.isNotEmpty()) {
                            if (isPlan) {
                                val i = editTarget - 10
                                PlanRepo.rename(i, v)
                                planNames = DepositPlans.plans.map { it.name }
                            } else {
                                if (editTarget == 0) shayanT = v else nikvamT = v
                                TitlesRepo.save(shayanT, nikvamT)
                            }
                            Toast.makeText(ctx, "عنوان جدید با موفقیت ذخیره شد", Toast.LENGTH_SHORT).show()
                        }
                        editTarget = -1
                    }
                ) { Text("ذخیره", fontWeight = FontWeight.ExtraBold, color = Red600) }
            },
            dismissButton = {
                TextButton(onClick = { editTarget = -1 }) { Text("انصراف") }
            }
        )
    }
}

/** ردیف عنوان با دکمه مداد */
@Composable
private fun TitleRow(label: String, value: String, onEdit: () -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    value,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            IconButton(onClick = onEdit) {
                Icon(
                    Icons.Filled.Edit,
                    contentDescription = "ویرایش عنوان",
                    tint = Red600,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
