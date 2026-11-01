package ir.foroosh.calc.ui.deposit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.foroosh.calc.data.DepositCalc
import ir.foroosh.calc.data.DepositPlans
import ir.foroosh.calc.ui.common.BigResult
import ir.foroosh.calc.ui.common.MoneyField
import ir.foroosh.calc.ui.common.ResultRow
import ir.foroosh.calc.ui.common.SectionCard
import ir.foroosh.calc.ui.common.fmtMoney
import ir.foroosh.calc.ui.theme.Green600
import ir.foroosh.calc.ui.theme.Red600
import ir.foroosh.calc.ui.theme.Vazir
import ir.foroosh.calc.ui.theme.Yellow500
import ir.foroosh.calc.ui.theme.Yellow600

/**
 * تب «سپرده گذاری» — بازسازی دقیق فایل اکسل «محاسبه گر سپرده»:
 * ورودی مبلغ و مدت، سپس مقایسه همه طرح‌های «جدول طرح ها»
 * (وضعیت، نرخ مؤثر، سود دوره/ماهانه/سالانه مطابق فرمول‌های شیت «محاسبه گر»).
 */
@Composable
fun DepositScreen() {
    var amount by remember { mutableStateOf("100000000000") }   // مبلغ سپرده (ریال)
    var days by remember { mutableStateOf("36") }               // مدت سپرده‌گذاری (روز)

    val amt = amount.toLongOrNull()?.toDouble() ?: 0.0
    val d = days.toIntOrNull() ?: 0
    val results = if (amt > 0 && d > 0) DepositCalc.evaluate(amt, d) else null

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {

        // ----- ورودی‌ها -----
        SectionCard(title = "ورودی‌های محاسبه") {
            MoneyField(
                label = "مبلغ سپرده (ریال)",
                value = amount,
                onChange = { amount = it }
            )
            Spacer(Modifier.height(12.dp))
            MoneyField(
                label = "مدت سپرده‌گذاری (روز)",
                value = days,
                hint = "محاسبه تقریبی بر مبنای روزشمار ۳۶۵ روزه",
                onChange = { days = it }
            )
        }

        // ----- مقایسه طرح‌ها -----
        SectionCard(title = "نتیجه مقایسه طرح‌ها", accentColor = Yellow500) {
            if (results == null) {
                Text(
                    "مبلغ و مدت سپرده‌گذاری را وارد کنید",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Text(
                    "وضعیت و سود هر طرح برای ورودی شما؛ فرمول‌ها دقیقاً مطابق فایل اکسل",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(10.dp))
                results.forEach { r -> PlanResultCard(r, d) }
            }
        }
    }
}

/** کارت نتیجه یک طرح — ستون‌های جدول اکسل به‌صورت ردیف‌های خوانا */
@Composable
private fun PlanResultCard(r: DepositCalc.PlanResult, days: Int) {
    val plan = DepositPlans.plans.getOrNull(r.index)
    val statusColor = when (r.status) {
        DepositCalc.ST_ELIGIBLE -> Green600
        DepositCalc.ST_BELOW_MIN -> Red600
        else -> Yellow600
    }
    Card(
        shape = RoundedCornerShape(14.dp),
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
                    r.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (plan != null) {
                    Text(
                        "${plan.group} — پرداخت ${plan.payNo}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.height(6.dp))
            Text(
                r.status,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = statusColor
            )
            Spacer(Modifier.height(4.dp))
            if (r.effRate > 0.0) {
                BigResult(
                    label = "سود برآوردی دوره ($days روز × نرخ ${fmtRate(r.effRate)})",
                    value = fmtMoney(r.periodProfit)
                )
            }
            Spacer(Modifier.height(6.dp))
            ResultRow("حداقل سپرده", fmtMoney(r.minDeposit) + " ریال")
            ResultRow("حداقل ماندگاری", "${r.minDays} روز")
            ResultRow("نرخ عادی سالانه", fmtRate(r.rate))
            ResultRow("نرخ مؤثر محاسباتی", if (r.effRate > 0.0) fmtRate(r.effRate) else "—")
            ResultRow("سود ماهانه تقریبی", if (r.effRate > 0.0) fmtMoney(r.monthlyProfit) else "—")
            ResultRow("سود سالانه تقریبی", if (r.effRate > 0.0) fmtMoney(r.yearlyProfit) else "—")
            plan?.note?.takeIf { it.isNotBlank() }?.let {
                Spacer(Modifier.height(4.dp))
                Text(
                    "توضیح: $it",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun fmtRate(pct: Double): String =
    if (pct == Math.floor(pct)) "${pct.toLong()}%"
    else String.format(java.util.Locale.US, "%.2f", pct).trimEnd('0').trimEnd('.') + "%"

/** نمایش جدول طرح ها به صورت دیالوگ (از تب تنظیمات هم استفاده می‌شود) */
@Composable
fun PlansTableDialog(
    title: String,
    rows: List<ir.foroosh.calc.data.PlanRow>,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("بستن", fontFamily = Vazir) }
        },
        title = { Text(title, style = MaterialTheme.typography.titleLarge) },
        text = {
            LazyColumn {
                item {
                    Text(
                        "طرح | گروه | شماره پرداخت | حداقل سپرده | ماندگاری | نرخ سالانه",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(6.dp))
                }
                items(rows) { p ->
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Text(
                            "${p.name}   |   ${p.group}   |   ${p.payNo}   |   ${fmtMoney(p.minDeposit)}   |   ${p.minMonths} ماه   |   ${fmtRate(p.rate)}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (p.note.isNotBlank()) {
                            Text(
                                p.note,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    )
}
