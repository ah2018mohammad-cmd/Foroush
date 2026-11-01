package ir.foroosh.calc.ui.lc

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.unit.dp
import ir.foroosh.calc.data.CalcEngine
import ir.foroosh.calc.ui.common.BigResult
import ir.foroosh.calc.ui.common.ChoiceChips
import ir.foroosh.calc.ui.common.DropdownSelector
import ir.foroosh.calc.ui.common.MoneyField
import ir.foroosh.calc.ui.common.ResultRow
import ir.foroosh.calc.ui.common.SectionCard
import ir.foroosh.calc.ui.common.fmtMoney
import ir.foroosh.calc.ui.theme.Yellow500

/** تب «محاسبات گشایش اعتبار اسنادی» — گشایش اعتبار اسنادی داخلی ریالی (فرمول‌های دقیق شیت اکسل) */
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun LcScreen() {
    val durations = listOf(90, 120, 150, 180, 210, 240, 270, 300, 330, 360)
    val prepayOptions = listOf(5.0, 10.0)

    var forTerm by remember { mutableStateOf(false) }        // دیداری / مدت دار
    var durationIdx by remember { mutableIntStateOf(0) }     // 90 روز
    var amount by remember { mutableStateOf("1910825950000") }
    var prepayIdx by remember { mutableIntStateOf(1) }       // 10%
    var docs by remember { mutableStateOf("527276755720") }
    var acceptDays by remember { mutableStateOf("90") }

    val days = durations[durationIdx]
    val prepayPct = prepayOptions[prepayIdx]
    val amt = amount.toLongOrNull()?.toDouble() ?: 0.0
    val dcs = docs.toLongOrNull()?.toDouble() ?: 0.0
    val accDays = acceptDays.toIntOrNull() ?: 0

    val res = if (amt > 0) CalcEngine.lc(forTerm, days, amt, prepayPct, dcs, accDays) else null

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {

        // ---------- مشخصات اعتبار ----------
        SectionCard(title = "مشخصات اعتبار اسنادی") {
            Text(
                "نوع اعتبار اسنادی",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(6.dp))
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = !forTerm,
                    onClick = { forTerm = false },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                ) { Text("دیداری", style = MaterialTheme.typography.labelMedium) }
                SegmentedButton(
                    selected = forTerm,
                    onClick = { forTerm = true },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                ) { Text("مدت دار", style = MaterialTheme.typography.labelMedium) }
            }
            Spacer(Modifier.height(12.dp))

            DropdownSelector(
                label = "مدت اعتبار اسنادی (روز)",
                options = durations.map { "$it" },
                selectedIndex = durationIdx,
                onSelect = { durationIdx = it }
            )
            Spacer(Modifier.height(10.dp))
            MoneyField("مبلغ گشایش (ریال)", amount) { amount = it }
            Spacer(Modifier.height(10.dp))
            ChoiceChips(
                "درصد پیش دریافت و میان دریافت",
                prepayOptions,
                prepayPct,
                onSelect = { prepayIdx = prepayOptions.indexOf(it) },
                format = { "${it.toLong()}%" }
            )
        }

        // ---------- محاسبات گشایش ----------
        SectionCard(title = "محاسبات گشایش", accentColor = Yellow500) {
            if (res != null) {
                ResultRow("پیش دریافت", fmtMoney(res.prepay))
                ResultRow("درصد مانده تعهدات مشتری بابت گشایش", "${res.remainPct.toLong()}%")
                ResultRow("مبلغ مانده تعهدات مشتری", fmtMoney(res.remain))
                Spacer(Modifier.height(6.dp))
                BigResult("کارمزد گشایش (ریال)", fmtMoney(res.openingFee))
            }
        }

        // ---------- اسناد رسیده ----------
        SectionCard(title = "اسناد رسیده اعتبار اسنادی (سیاهه تجاری یا صورتحساب ارائه خدمات)") {
            MoneyField("مبلغ اسناد رسیده (ریال)", docs) { docs = it }

            if (forTerm) {
                Spacer(Modifier.height(10.dp))
                MoneyField("مدت تعهد پرداخت — توافق شده پس از صدور اسناد (روز)", acceptDays) { acceptDays = it }
            }

            if (res != null && dcs > 0) {
                Spacer(Modifier.height(10.dp))
                if (forTerm) {
                    ResultRow("میان دریافت", fmtMoney(res.midReceive ?: 0.0))
                    ResultRow("کارمزد بررسی اسناد", fmtMoney(res.reviewFee))
                    ResultRow("کارمزد پذیرش و تعهد پرداخت", fmtMoney(res.acceptFee ?: 0.0))
                    ResultRow("جمع کارمزد پذیرش و بررسی اسناد", fmtMoney(res.sumAcceptReview ?: 0.0), key = true)
                } else {
                    ResultRow("مدت تعهد پرداخت", "ندارد")
                    ResultRow("میان دریافت", "ندارد")
                    ResultRow("کارمزد بررسی اسناد", fmtMoney(res.reviewFee))
                    ResultRow("کارمزد پذیرش و تعهد پرداخت", "ندارد")
                }
            }
        }

        // ---------- کل ----------
        SectionCard(title = "جمع‌بندی", accentColor = Yellow500) {
            if (res != null) {
                BigResult("کل کارمزد دریافتی (ریال)", fmtMoney(res.totalFee))
            }
        }
    }
}
