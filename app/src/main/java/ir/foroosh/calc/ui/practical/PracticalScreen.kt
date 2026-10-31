package ir.foroosh.calc.ui.practical

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
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
import ir.foroosh.calc.ui.common.LockedField
import ir.foroosh.calc.ui.common.MoneyField
import ir.foroosh.calc.ui.common.ResultRow
import ir.foroosh.calc.ui.common.SectionCard
import ir.foroosh.calc.ui.common.fmtMoney
import ir.foroosh.calc.ui.theme.Red600
import ir.foroosh.calc.ui.theme.Yellow500

/** تب «محاسبات کاربردی» — سفته، ضمانت‌نامه، حق بیمه و قرض‌الحسنه (فرمول‌های دقیق شیت اکسل) */
@Composable
fun PracticalScreen() {

    // ===== ۱) سفته مورد نیاز تسهیلات =====
    var sRate by remember { mutableIntStateOf(23) }
    var sGap by remember { mutableIntStateOf(3) }
    var sAmount by remember { mutableStateOf("3000000000") }
    var sMonths by remember { mutableStateOf("6") }

    val sAmt = sAmount.toLongOrNull()?.toDouble() ?: 0.0
    val sMon = sMonths.toIntOrNull() ?: 0
    val sakht = if (sAmt > 0 && sMon > 0) CalcEngine.sakhtTashilat(sAmt, sRate, sGap, sMon) else null

    // ===== ۲) سفته مورد نیاز ضمانتنامه =====
    var gAmount by remember { mutableStateOf("1000000000") }
    var gPct by remember { mutableStateOf("10") }
    val gAmt = gAmount.toLongOrNull()?.toDouble() ?: 0.0
    val gP = gPct.toDoubleOrNull() ?: 0.0
    val gRes = if (gAmt > 0) CalcEngine.sakhtZemanat(gAmt, gP) else null

    // ===== ۳) کارمزد صدور ضمانتنامه =====
    var fAmount by remember { mutableStateOf("871200000") }
    var fPct by remember { mutableStateOf("10") }
    var fDays by remember { mutableStateOf("365") }
    val fAmt = fAmount.toLongOrNull()?.toDouble() ?: 0.0
    val fP = fPct.toDoubleOrNull() ?: 0.0
    val fD = fDays.toIntOrNull() ?: 0
    val fRes = if (fAmt > 0 && fD > 0) CalcEngine.karMOZdZemanat(fAmt, fP, fD) else null

    // ===== ۴) حق بیمه =====
    var bTypeIdx by remember { mutableIntStateOf(0) }
    val bType = CalcEngine.bimehTypes[bTypeIdx]
    var bAmount by remember { mutableStateOf("5000000000") }
    var bDays by remember { mutableStateOf("365") }
    val bAmt = bAmount.toLongOrNull()?.toDouble() ?: 0.0
    val bD = bDays.toIntOrNull() ?: 0
    val effBDays = if (bType.plus730) bD + 730 else bD
    val bRes = if (bAmt > 0 && bD > 0) CalcEngine.bimeh(bType, bAmt, effBDays) else null

    // ===== ۵) قرض‌الحسنه =====
    var qType by remember { mutableIntStateOf(0) } // ازدواج
    var qMonthIdx by remember { mutableIntStateOf(4) } // 60
    var qAmount by remember { mutableStateOf("2000000000") }
    val qM = CalcEngine.qarzMonths[qMonthIdx]
    val qKar = CalcEngine.QARZ_FEE_PCT // کارمزد ثابت ۴٪ — غیرقابل ویرایش
    val qAmt = qAmount.toLongOrNull()?.toDouble() ?: 0.0
    val qRes = if (qAmt > 0 && qKar > 0) CalcEngine.qarz(qAmt, qKar, qM) else null

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {

        // ---------- ۱ ----------
        SectionCard(title = "سفته مورد نیاز تسهیلات") {
            ChoiceChips("نرخ تسهیلات (%)", listOf(14, 18, 23), sRate) { sRate = it }
            Spacer(Modifier.height(8.dp))
            ChoiceChips("فاصله اقساط (ماه)", listOf(1, 3, 6), sGap) { sGap = it }
            Spacer(Modifier.height(10.dp))
            MoneyField("مبلغ تسهیلات (ریال)", sAmount) { sAmount = it }
            Spacer(Modifier.height(10.dp))
            MoneyField("مدت به ماه", sMonths) { sMonths = it }
            if (sakht != null) {
                Spacer(Modifier.height(10.dp))
                ResultRow("کل سود", fmtMoney(sakht.profit))
                ResultRow("اصل و سود", fmtMoney(sakht.total))
                ResultRow("اقساط ماهانه", fmtMoney(sakht.installment))
                BigResult("مبلغ وثیقه مورد نیاز (ریال)", fmtMoney(sakht.collateral))
            }
        }

        // ---------- ۲ ----------
        SectionCard(title = "سفته مورد نیاز ضمانتنامه", accentColor = Yellow500) {
            MoneyField("مبلغ ضمانتنامه (ریال)", gAmount) { gAmount = it }
            Spacer(Modifier.height(10.dp))
            MoneyField("درصد سپرده نقدی", gPct) { gPct = it }
            if (gRes != null) {
                Spacer(Modifier.height(10.dp))
                ResultRow("مبلغ سپرده نقدی", fmtMoney(gRes.first))
                BigResult("مبلغ وثیقه مورد نیاز (ریال)", fmtMoney(gRes.second))
            }
        }

        // ---------- ۳ ----------
        SectionCard(title = "کارمزد صدور ضمانتنامه", accentColor = Red600) {
            MoneyField("مبلغ ضمانتنامه (ریال)", fAmount) { fAmount = it }
            Spacer(Modifier.height(10.dp))
            MoneyField("درصد سپرده نقدی", fPct) { fPct = it }
            Spacer(Modifier.height(10.dp))
            MoneyField("مدت به روز", fDays) { fDays = it }
            if (fRes != null) {
                Spacer(Modifier.height(10.dp))
                ResultRow("مبلغ سپرده نقدی", fmtMoney(fRes.first))
                BigResult("مبلغ کارمزد صدور (ریال)", fmtMoney(fRes.second))
            }
        }

        // ---------- ۴ ----------
        SectionCard(title = "محاسبه حق بیمه", accentColor = Yellow500) {
            DropdownSelector(
                label = "نوع بیمه‌شده / نوع اموال",
                options = CalcEngine.bimehTypes.map { it.label },
                selectedIndex = bTypeIdx,
                onSelect = { bTypeIdx = it }
            )
            Spacer(Modifier.height(10.dp))
            Text(
                "نرخ حق بیمه سالانه: ${bType.rateLabel}" +
                        if (bType.withVat) " — شامل مالیات بر ارزش افزوده ۱۰٪" else " — بدون مالیات",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(10.dp))
            MoneyField(
                if (bType.key == "working_capital") "مبلغ فاکتور (ریال)" else "مبلغ ترهین / سرمایه بیمه (ریال)",
                bAmount
            ) { bAmount = it }
            Spacer(Modifier.height(10.dp))
            MoneyField(
                if (bType.plus730) "روزهای مانده تا پایان سال (+۷۳۰ روز خودکار)" else "تمام مدت (به روز)",
                bDays
            ) { bDays = it }
            if (bRes != null) {
                Spacer(Modifier.height(6.dp))
                if (bType.plus730) {
                    ResultRow("مدت محاسبه‌شده", "$effBDays روز")
                }
                BigResult("حق بیمه قابل وصول برای کل مدت (ریال)", fmtMoney(bRes))
            }
        }

        // ---------- ۵ ----------
        SectionCard(title = "محاسبات تسهیلات قرض‌الحسنه", accentColor = Red600) {
            ChoiceChips("نوع تسهیلات", CalcEngine.qarzTypes, CalcEngine.qarzTypes[qType]) { qType = CalcEngine.qarzTypes.indexOf(it) }
            Spacer(Modifier.height(10.dp))
            LockedField("کارمزد (%) — ثابت", "${CalcEngine.QARZ_FEE_PCT.toInt()}")
            Spacer(Modifier.height(10.dp))
            DropdownSelector(
                label = "مدت تسهیلات (ماه)",
                options = CalcEngine.qarzMonths.map { "$it" },
                selectedIndex = qMonthIdx,
                onSelect = { qMonthIdx = it }
            )
            Spacer(Modifier.height(10.dp))
            MoneyField("مبلغ تسهیلات (تا دو میلیارد ریال)", qAmount) { qAmount = it }
            if (qRes != null) {
                Spacer(Modifier.height(10.dp))
                qRes.schedule.forEachIndexed { i, fee ->
                    ResultRow("کارمزد سال ${i + 1}", fmtMoney(fee))
                }
                ResultRow("کل کارمزد", fmtMoney(qRes.totalFee), key = true)
                Spacer(Modifier.height(6.dp))
                ResultRow("قسط ماهانه بدون کارمزد", fmtMoney(qRes.instNoFee))
                BigResult("قسط ماهانه با تقسیط کارمزد (ریال)", fmtMoney(qRes.instWithFee))
                Spacer(Modifier.height(10.dp))
                ResultRow("مبلغ وثیقه مورد نیاز", fmtMoney(qRes.collateral), key = true)
            }
        }
    }
}
