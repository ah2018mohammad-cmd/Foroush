package ir.foroosh.calc.data

/**
 * طرح‌های سپرده‌گذاری — منبع: فایل اکسل «محاسبه گر سپرده»، شیت‌های «جدول طرح ها» و «محاسبه گر»
 *
 * shortRate90 = قاعده خاص شیت محاسبه‌گر برای «میزان ۲» و «میزان ۳»:
 * اگر سپرده بین ۳۰ تا ۹۰ روز بماند، نرخ مؤثر ۴٪ لحاظ می‌شود.
 * این قاعده به ردیف طرح چسبیده است تا با تغییر عنوان طرح حفظ شود.
 */
data class PlanRow(
    val name: String,        // طرح
    val group: String,       // گروه
    val payNo: Int,          // شماره پرداخت
    val minDeposit: Double,  // حداقل سپرده (ریال)
    val minMonths: Int,      // حداقل ماندگاری (ماه)
    val rate: Double,        // نرخ سود عادی سالانه (٪)
    val note: String,        // توضیحات
    val shortRate90: Boolean = false
)

object DepositPlans {

    private val planDefaults = listOf(
        PlanRow("میزان", "میزان", 4895, 1_000_000_000.0, 1, 25.0, "کمتر از یک ماه: بدون سود"),
        PlanRow("میزان ۲", "میزان", 4898, 10_000_000_000.0, 3, 27.0, "بین یک تا سه ماه: ۴٪ (مبنای اعمال نیازمند تأیید)", true),
        PlanRow("میزان ۳", "میزان", 4901, 30_000_000_000.0, 3, 27.5, "بین یک تا سه ماه: ۴٪ (مبنای اعمال نیازمند تأیید)", true),
        PlanRow("مهرگان ۳۰", "مهرگان", 4853, 800_000_000.0, 1, 22.5, "کمتر از یک ماه: بدون سود"),
        PlanRow("مهرگان پلاس ۳", "مهرگان", 4875, 2_000_000_000.0, 1, 25.5, "کمتر از یک ماه: بدون سود"),
        PlanRow("جهش ۱", "جهش", 4886, 5_000_000_000.0, 1, 26.0, "کمتر از یک ماه: بدون سود"),
        PlanRow("جهش ۲", "جهش", 4889, 20_000_000_000.0, 1, 26.5, "کمتر از یک ماه: بدون سود"),
        PlanRow("جهش ۳", "جهش", 4892, 100_000_000_000.0, 1, 27.0, "کمتر از یک ماه: بدون سود"),
    )

    /** جدول طرح‌های جاری — توسط PlanRepo (بعد از ویرایش کاربر) جایگزین می‌شود */
    var plans: List<PlanRow> = planDefaults

    fun planDefaultsList(): List<PlanRow> = planDefaults
}

/** محاسبه‌گر و مقایسه طرح‌های سپرده — بازسازی دقیق فرمول‌های شیت «محاسبه گر» اکسل */
object DepositCalc {

    /** وضعیت‌های اکسل */
    const val ST_BELOW_MIN = "مبلغ کمتر از حداقل"
    const val ST_UNDER_MONTH = "کمتر از یک ماه: بدون سود"
    const val ST_SHORT_4PCT = "دوره ۱ تا ۳ ماه: نرخ ۴٪ طبق توضیح تصویر"
    const val ST_NEED_INQUIRY = "نیازمند استعلام شرایط شکست"
    const val ST_ELIGIBLE = "واجد شرایط طبق حداقل جدول"

    data class PlanResult(
        val index: Int,
        val name: String,        // عنوان (احتمالاً ویرایش‌شده)
        val minDeposit: Double,  // حداقل سپرده (ریال)
        val minDays: Int,        // حداقل ماندگاری (روز = ماه×۳۰)
        val rate: Double,        // نرخ عادی سالانه (٪)
        val status: String,      // وضعیت برای ورودی شما
        val effRate: Double,     // نرخ مؤثر محاسباتی (٪)
        val periodProfit: Double,// سود برآوردی دوره (ریال)
        val monthlyProfit: Double, // سود ماهانه تقریبی (ریال)
        val yearlyProfit: Double   // سود سالانه تقریبی (ریال)
    )

    /**
     * مقایسه همه طرح‌ها برای مبلغ و مدت داده‌شده — دقیقاً مطابق فرمول‌های اکسل:
     * E: وضعیت / F: نرخ مؤثر / G: سود دوره / H: سود ماهانه / I: سود سالانه
     */
    fun evaluate(amount: Double, days: Int, names: List<String>? = null): List<PlanResult> =
        DepositPlans.plans.mapIndexed { i, p ->
            val name = names?.getOrNull(i) ?: p.name
            val minDays = p.minMonths * 30          // اکسل: E*30

            // ستون E — وضعیت برای ورودی شما
            val status = when {
                amount < p.minDeposit -> ST_BELOW_MIN
                days < 30 -> ST_UNDER_MONTH
                p.shortRate90 && days < 90 -> ST_SHORT_4PCT
                days < minDays -> ST_NEED_INQUIRY
                else -> ST_ELIGIBLE
            }

            // ستون F — نرخ مؤثر محاسباتی
            val effRate = when {
                amount < p.minDeposit -> 0.0
                days < 30 -> 0.0
                p.shortRate90 && days < 90 -> 4.0
                days < minDays -> 0.0
                else -> p.rate
            }

            // ستون G — سود برآوردی دوره (صفر برای دو وضعیت «مبلغ کمتر از حداقل» و «استعلام شکست»)
            val periodProfit =
                if (status == ST_BELOW_MIN || status == ST_NEED_INQUIRY) 0.0
                else amount * effRate / 100.0 * days / 365.0

            // ستون H و I — سود ماهانه و سالانه تقریبی
            val monthly = if (effRate == 0.0) 0.0 else amount * effRate / 100.0 / 12.0
            val yearly = if (effRate == 0.0) 0.0 else amount * effRate / 100.0

            PlanResult(i, name, p.minDeposit, minDays, p.rate, status, effRate, periodProfit, monthly, yearly)
        }
}
