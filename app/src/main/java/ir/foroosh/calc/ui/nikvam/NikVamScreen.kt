package ir.foroosh.calc.ui.nikvam

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
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
import ir.foroosh.calc.ui.shayan.CoefTableDialog
import ir.foroosh.calc.ui.theme.Yellow500

/** تب «نیک وام» — قرض‌الحسنه (فرمول‌های دقیق شیت «محاسبه گر نیک وام») */
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun NikVamScreen() {
    var forward by remember { mutableStateOf(true) }
    var amount by remember { mutableStateOf("7000000000") }
    var avg by remember { mutableIntStateOf(1) }
    var repay by remember { mutableIntStateOf(24) }
    var showTable by remember { mutableStateOf(false) }

    val rate = Coefficients.nikvamRate
    val repayOpts = Coefficients.nikvamRepayOptions(avg)
    val effRepay = if (repay in repayOpts) repay else (if (24 in repayOpts) 24 else repayOpts.last())

    val amt = amount.toLongOrNull()?.toDouble() ?: 0.0
    val res = if (amt > 0) CalcEngine.nikvam(forward, amt, rate, avg, effRepay) else null
    val coefRow = Coefficients.nikvamFor(avg, effRepay)

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {

        SectionCard(title = "ورودی‌های محاسبه") {
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

            // نرخ مطابق جدول ضرایب نیک وام (پیش‌فرض ۴٪)
            Row {
                Text(
                    "نرخ تسهیلات: ",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "${Coefficients.nikvamRate}% (قرض‌الحسنه)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(Modifier.height(10.dp))

            ChoiceChips(
                label = "دوره معدل‌گیری (ماه)",
                options = Coefficients.nikvamAvgOptions,
                selected = avg,
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

        SectionCard(title = "کارمزد سالانه", accentColor = Yellow500) {
            if (res == null) {
                Text(
                    "مبلغ را وارد کنید",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                res.feeSchedule.forEachIndexed { i, fee ->
                    ResultRow("کارمزد سال ${i + 1}", fmtMoney(fee))
                }
                ResultRow("تعداد سال کارمزد", "${effRepay / 12}")
                ResultRow("کل کارمزد", fmtMoney(res.totalFee), key = true)
            }
        }

        SectionCard(title = "نتیجه محاسبه", accentColor = Yellow500) {
            if (res != null) {
                ResultRow("ضریب تسهیلات", fmtPct(res.coef))
                BigResult(
                    label = if (forward) "مبلغ تسهیلات قابل اعطاء (ریال)" else "مبلغ سپرده مورد نیاز (ریال)",
                    value = fmtMoney(res.main)
                )
                Spacer(Modifier.height(10.dp))
                ResultRow("اصل و کارمزد", fmtMoney(res.total))
                ResultRow("اقساط ماهانه", fmtMoney(res.installment))
                ResultRow("مبلغ وثیقه مورد نیاز", fmtMoney(res.collateral), key = true)
            }
            Spacer(Modifier.height(8.dp))
            androidx.compose.material3.TextButton(onClick = { showTable = true }) {
                Text(
                    "مشاهده جدول ضرایب نیک وام",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (showTable) {
            CoefTableDialog(
                title = "جدول ضرایب نیک وام",
                rows = Coefficients.nikvam,
                highlight = coefRow,
                onDismiss = { showTable = false }
            )
        }
    }
}
