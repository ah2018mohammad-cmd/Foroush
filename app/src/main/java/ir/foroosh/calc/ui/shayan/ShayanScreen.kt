package ir.foroosh.calc.ui.shayan

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.foroosh.calc.data.CalcEngine
import ir.foroosh.calc.data.Coefficients
import ir.foroosh.calc.ui.common.BigResult
import ir.foroosh.calc.ui.common.ChoiceChips
import ir.foroosh.calc.ui.common.MoneyField
import ir.foroosh.calc.ui.common.ResultRow
import ir.foroosh.calc.ui.common.SectionCard
import ir.foroosh.calc.ui.common.fmtMoney
import ir.foroosh.calc.ui.common.fmtPct
import ir.foroosh.calc.ui.theme.Yellow500
import ir.foroosh.calc.ui.theme.Red600

/** تب «شایان» — مرابحه شایان (فرمول‌های دقیق شیت «محاسبه گر شایان») */
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun ShayanScreen() {
    var forward by remember { mutableStateOf(true) }               // نوع محاسبه
    var amount by remember { mutableStateOf("7000000000") }        // مبلغ سپرده / تسهیلات
    var rate by remember { mutableIntStateOf(23) }                 // نرخ تسهیلات
    var avg by remember { mutableIntStateOf(3) }                   // دوره معدل‌گیری
    var repay by remember { mutableIntStateOf(24) }                // حداکثر دوره بازپرداخت

    // گزینه‌های داینامیک مطابق لیست‌های اکسل (اقساط داینامیک / دوره معدل‌گیری داینامیک)
    val avgOpts = Coefficients.shayanAvgOptions(rate)
    val effAvg = if (avg in avgOpts) avg else (if (3 in avgOpts) 3 else avgOpts.first())
    val repayOpts = Coefficients.shayanRepayOptions(rate, effAvg)
    val effRepay = if (repay in repayOpts) repay else (if (24 in repayOpts) 24 else repayOpts.last())

    val amt = amount.toLongOrNull()?.toDouble() ?: 0.0
    val res = if (amt > 0) CalcEngine.shayan(forward, amt, rate, effAvg, effRepay) else null

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {

        // ----- ورودی‌ها -----
        SectionCard(title = "اطلاعات زیر را تکمیل نمائید") {
            // نوع محاسبه
            Text(
                "نوع محاسبه",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(6.dp))
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = forward,
                    onClick = { forward = true },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                ) {
                    Text("تسهیلات از روی سپرده", style = MaterialTheme.typography.labelMedium)
                }
                SegmentedButton(
                    selected = !forward,
                    onClick = { forward = false },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                ) {
                    Text("سپرده برای تسهیلات", style = MaterialTheme.typography.labelMedium)
                }
            }
            Spacer(Modifier.height(12.dp))

            MoneyField(
                label = if (forward) "مبلغ سپرده مشتری (ریال)" else "مبلغ تسهیلات درخواستی (ریال)",
                value = amount,
                onChange = { amount = it }
            )
            Spacer(Modifier.height(12.dp))

            ChoiceChips(
                label = "نرخ تسهیلات (%)",
                options = Coefficients.shayanRates,
                selected = rate,
                onSelect = { rate = it }
            )
            Spacer(Modifier.height(10.dp))

            ChoiceChips(
                label = "دوره معدل‌گیری (ماه)",
                options = avgOpts,
                selected = effAvg,
                onSelect = { avg = it }
            )
            Spacer(Modifier.height(10.dp))

            ChoiceChips(
                label = "حداکثر دوره بازپرداخت (ماه)",
                options = repayOpts,
                selected = effRepay,
                onSelect = { repay = it },
                format = { "$it" }
            )
        }

        // ----- نتایج -----
        SectionCard(title = "نتیجه محاسبه", accentColor = Yellow500) {
            if (res == null) {
                Text(
                    "مبلغ را وارد کنید",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                ResultRow("ضریب تسهیلات", fmtPct(res.coef))
                BigResult(
                    label = if (forward) "مبلغ تسهیلات قابل اعطاء (ریال)" else "مبلغ سپرده مورد نیاز (ریال)",
                    value = fmtMoney(res.main)
                )
                Spacer(Modifier.height(10.dp))
                ResultRow("کل سود", fmtMoney(res.profit))
                ResultRow("اصل و سود", fmtMoney(res.total))
                ResultRow("اقساط ماهانه", fmtMoney(res.installment))
                ResultRow(
                    "مبلغ وثیقه مورد نیاز",
                    fmtMoney(res.collateral),
                    key = true
                )
            }
        }
    }
}

/** نمایش جدول ضرایب به صورت دیالوگ */
@Composable
fun CoefTableDialog(
    title: String,
    rows: List<ir.foroosh.calc.data.CoefRow>,
    highlight: ir.foroosh.calc.data.CoefRow?,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("بستن", fontFamily = ir.foroosh.calc.ui.theme.Vazir) }
        },
        title = { Text(title, style = MaterialTheme.typography.titleLarge) },
        text = {
            LazyColumn {
                item {
                    Text(
                        "نرخ | معدل‌گیری | بازپرداخت | درصد تسهیلات نسبت به سپرده",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(6.dp))
                }
                items(rows) { r ->
                    val sel = r == highlight
                    Text(
                        "${r.rate}%   |   ${r.avg} ماه   |   ${r.repay} ماه   |   ${fmtPct(r.pct)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (sel) FontWeight.ExtraBold else FontWeight.Normal,
                        color = if (sel) Red600 else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(30.dp)
                    )
                }
            }
        }
    )
}
