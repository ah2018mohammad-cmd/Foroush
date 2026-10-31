package ir.foroosh.calc.data

import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.round

/**
 * موتور محاسبات — بازنویسی ۱ به ۱ فرمول‌های شیت‌های اکسل «فروش»:
 *  محاسبه گر شایان، محاسبه گر نیک وام، محاسبات گشایش اعتبار اسنادی، محاسبات کاربردی
 * (تست شده: ۴۳/۴۳ خروجی با مقادیر کش‌شده اکسل مطابقت دارد)
 */
object CalcEngine {

    // ---------- توابع گرد کردن مطابق اکسل ----------
    /** ROUND — گرد کردن به نزدیک‌ترین، نیم به بالا (away from zero)؛ digits می‌تواند منفی باشد */
    fun xlRound(x: Double, digits: Int = 0): Double {
        val f = Math.pow(10.0, digits.toDouble())
        return round(abs(x) * f) / f * (if (x < 0) -1.0 else 1.0)
    }

    /** ROUNDDOWN — گرد کردن به پایین */
    fun xlRoundDown(x: Double, digits: Int = 0): Double {
        val f = Math.pow(10.0, digits.toDouble())
        return floor(abs(x) * f) / f * (if (x < 0) -1.0 else 1.0)
    }

    /** ROUNDUP — گرد کردن به بالا */
    fun xlRoundUp(x: Double, digits: Int = 0): Double {
        val f = Math.pow(10.0, digits.toDouble())
        return ceil(abs(x) * f) / f * (if (x < 0) -1.0 else 1.0)
    }

    // ---------- شایان (مرابحه شایان) ----------
    data class FacilityResult(
        val coef: Double,        // ضریب تسهیلات
        val main: Double,        // مبلغ تسهیلات قابل اعطاء (حالت مستقیم) یا مبلغ سپرده مورد نیاز (حالت معکوس)
        val profit: Double,      // کل سود
        val total: Double,       // اصل و سود
        val installment: Double, // اقساط ماهانه
        val collateral: Double   // مبلغ وثیقه مورد نیاز
    )

    /** Excel: B9=ضریب×مبلغ | B10=تسهیلات×نرخ×(بازپرداخت+5.1)/2400 | B12=اصل و سود/تعداد اقساط | B13=اصل و سود+اقساط×2 */
    fun shayan(forward: Boolean, amount: Double, rate: Int, avg: Int, repay: Int): FacilityResult? {
        val row = Coefficients.shayanFor(rate, avg, repay) ?: return null
        val facility = if (forward) amount * row.pct else amount
        val mainOut = if (forward) facility else amount / row.pct
        val profit = facility * rate * (repay + 5.1) / 2400.0
        val total = facility + profit
        val installment = total / repay
        return FacilityResult(row.pct, mainOut, profit, total, installment, total + installment * 2)
    }

    // ---------- نیک وام (قرض‌الحسنه) ----------
    data class NikVamResult(
        val coef: Double,
        val main: Double,               // مبلغ تسهیلات قابل اعطاء یا مبلغ سپرده مورد نیاز
        val feeSchedule: List<Double>,  // کارمزد سالانه (سال ۱ تا n، فقط مقادیر مثبت مثل IF(...,""))
        val totalFee: Double,           // کل کارمزد
        val total: Double,              // اصل و کارمزد
        val installment: Double,        // اقساط ماهانه
        val collateral: Double          // مبلغ وثیقه مورد نیاز
    )

    /**
     * Excel: B10=مبلغ×ضریب | کارمزد سال k = پایه×(1−(k−1)/سال‌ها)×نرخ/100 | B12=اصل+کارمزد
     * B13=اصل و کارمزد/تعداد اقساط | B14=وثیقه=اصل و کارمزد
     * پایه کارمزد در حالت مستقیم مبلغ تسهیلات (B10) و در حالت معکوس مبلغ واردشده (B3) است.
     */
    fun nikvam(forward: Boolean, amount: Double, rate: Int, avg: Int, repay: Int): NikVamResult? {
        val row = Coefficients.nikvamFor(avg, repay) ?: return null
        val facility = if (forward) amount * row.pct else amount
        val mainOut = if (forward) facility else amount / row.pct
        val feeBase = facility
        val years = repay / 12.0
        val schedule = (1..10)
            .map { k -> feeBase * (1 - (k - 1) / years) * rate / 100.0 }
            .takeWhile { it > 1e-9 }
        val totalFee = schedule.sum()
        val total = feeBase + totalFee
        return NikVamResult(row.pct, mainOut, schedule, totalFee, total, total / repay, total)
    }

    // ---------- گشایش اعتبار اسنادی داخلی ریالی ----------
    data class LcResult(
        val prepay: Double,          // پیش دریافت = مبلغ×درصد/100
        val remainPct: Double,       // درصد مانده تعهدات = 100−درصد
        val remain: Double,          // مبلغ مانده تعهدات = مبلغ−پیش دریافت
        val openingFee: Double,      // کارمزد گشایش = مانده×(1+0.5×ROUNDUP((روز−90)/30))/1000
        val reviewFee: Double,       // کارمزد بررسی اسناد = اسناد×0.5/1000 (دیداری بدون گرد کردن، مدت‌دار ROUNDDOWN)
        val acceptFee: Double?,      // کارمزد پذیرش و تعهد پرداخت (فقط مدت‌دار)
        val midReceive: Double?,     // میان دریافت = اسناد×درصد/100 (فقط مدت‌دار)
        val sumAcceptReview: Double?,// جمع کارمزد پذیرش و بررسی
        val totalFee: Double         // کل کارمزد دریافتی
    )

    fun lc(forTerm: Boolean, days: Int, amount: Double, prepayPct: Double, docs: Double, acceptDays: Int): LcResult {
        val prepay = amount * prepayPct / 100.0
        val remainPct = 100.0 - prepayPct
        val remain = amount - prepay
        val openingFee = if (days < 90) 0.0
        else remain * (1 + 0.5 * xlRoundUp((days - 90.0) / 30.0, 0)) / 1000.0
        return if (forTerm) {
            val review = xlRoundDown(docs * 0.5 / 1000.0, 0)
            val accept = xlRoundDown(docs * 2 * acceptDays / 36500.0 * acceptDays / 100.0, 0)
            val sum = accept + review
            LcResult(prepay, remainPct, remain, openingFee, review, accept, docs * prepayPct / 100.0, sum, openingFee + sum)
        } else {
            val review = docs * 0.5 / 1000.0
            LcResult(prepay, remainPct, remain, openingFee, review, null, null, null, openingFee + review)
        }
    }

    // ---------- محاسبات کاربردی ----------
    /** سفته مورد نیاز تسهیلات — H6=مبلغ×نرخ×(ماه+5.1)/2400 | H8=اصل و سود/(ماه×فاصله) | H9=اصل و سود+اقساط×2 */
    fun sakhtTashilat(amount: Double, rate: Int, gap: Int, months: Int): FacilityResult {
        val profit = amount * rate * (months + 5.1) / 2400.0
        val total = amount + profit
        val installment = total / (months * gap)
        return FacilityResult(rate / 100.0, amount, profit, total, installment, total + installment * 2)
    }

    /** سفته مورد نیاز ضمانتنامه — W4=مبلغ×درصد/100 | W8=(مبلغ−سپرده)/100×120 */
    fun sakhtZemanat(amount: Double, cashPct: Double): Pair<Double, Double> {
        val cash = amount * cashPct / 100.0
        return cash to (amount - cash) / 100.0 * 120.0
    }

    /** کارمزد صدور ضمانتنامه — AL5=مبلغ×درصد/100 | AL8=(مبلغ−سپرده)/36500×2.25×روز */
    fun karMOZdZemanat(amount: Double, cashPct: Double, days: Int): Pair<Double, Double> {
        val cash = amount * cashPct / 100.0
        return cash to (amount - cash) / 36500.0 * 2.25 * days
    }

    /** انواع حق بیمه — نرخ‌ها و مالیات دقیقاً مطابق شیت محاسبات کاربردی */
    data class BimehType(
        val key: String,
        val label: String,          // عنوان مطابق اکسل
        val rateLabel: String,      // برچسب نرخ مطابق اکسل
        val ratePerMille: Double,   // نرخ در هزار
        val withVat: Boolean,       // مالیات بر ارزش افزوده ۱۰٪ (ضریب ۱۱۰ در فرمول)
        val plus730: Boolean        // تسهیلات بیش از ۳ سال: روزهای مانده + ۷۳۰
    )

    val bimehTypes = listOf(
        BimehType("res_lt3", "اموال مسکونی و اداری — تسهیلات کمتر از ۳ سال", "یک و نیم در هزار (0.15 درصد)", 1.5, true, false),
        BimehType("res_gt3", "اموال مسکونی و اداری — تسهیلات بیش از ۳ سال", "یک و نیم در هزار (0.15 درصد)", 1.5, true, true),
        BimehType("nonres_lt3", "اموال غیرمسکونی و غیراداری — تسهیلات کمتر از ۳ سال", "دو در هزار (0.2 درصد)", 2.0, true, false),
        BimehType("nonres_gt3", "اموال غیرمسکونی و غیراداری — تسهیلات بیش از ۳ سال", "دو در هزار (0.2 درصد)", 2.0, true, true),
        BimehType("construction", "طرح‌ها و پروژه‌های ساختمانی و نصب ماشین‌آلات", "یک و نیم در هزار (0.15 درصد)", 1.5, true, false),
        BimehType("working_capital", "آتش‌سوزی اموال (فاکتور سرمایه در گردش)", "دو در هزار (0.2 درصد)", 2.0, true, false),
        BimehType("life_balance", "حق بیمه عمر مانده بدهی", "یک و چهار در هزار (0.14 درصد)", 1.4, false, false),
    )

    /**
     * حق بیمه قابل وصول برای کل مدت:
     * عادی: (سرمایه/365000)×(نرخ×روز/100)×110
     * عمر مانده بدهی (بدون مالیات): (سرمایه/36500)×14×روز/100
     */
    fun bimeh(type: BimehType, amount: Double, days: Int): Double =
        if (type.key == "life_balance") amount / 36500.0 * (14 * days) / 100.0
        else (amount / 365000.0) * (type.ratePerMille * days / 100.0) * 110.0

    /** محاسبات تسهیلات قرض‌الحسنه */
    data class QarzResult(
        val schedule: List<Double>,  // کارمزد سال اول تا n
        val totalFee: Double,        // کل کارمزد
        val instNoFee: Double,       // قسط ماهانه بدون کارمزد = ROUND(مبلغ/(ماه−سال‌ها)، −3)
        val instWithFee: Double,     // قسط ماهانه با تقسیط کارمزد = ROUND((مبلغ+کارمزد)/ماه، 0)
        val collateral: Double       // مبلغ وثیقه مورد نیاز = مبلغ + کل کارمزد
    )

    fun qarz(amount: Double, karMOZ: Double, months: Int): QarzResult {
        val years = months / 12.0
        val schedule = (1..10)
            .map { k -> amount * (1 - (k - 1) / years) * karMOZ / 100.0 }
            .takeWhile { it > 1e-9 }
        val totalFee = schedule.sum()
        return QarzResult(
            schedule,
            totalFee,
            xlRound(amount / (months - years), -3),
            xlRound((amount + totalFee) / months, 0),
            amount + totalFee
        )
    }

    val qarzTypes = listOf("ازدواج", "فرزندآوری")
    /** نرخ کارمزد ثابت قرض‌الحسنه (درصد) — طبق دستور کاربر، فقط ۴ */
    const val QARZ_FEE_PCT = 4.0
    val qarzMonths = listOf(12, 24, 36, 48, 60, 72, 84, 96, 108, 120)
}
