package ir.foroosh.calc.data

/**
 * جداول ضرایب — منبع: فایل اکسل «فروش»، شیت‌های «جدول ضرایب شایان» و «جدول ضرایب نیک وام»
 * pct = درصد تسهیلات نسبت به سپرده (به صورت کسر: 0.5 یعنی ۵۰٪)
 */
data class CoefRow(val rate: Int, val avg: Int, val repay: Int, val pct: Double)

object Coefficients {

    // ===== جدول ضرایب شایان (۱۹ ردیف) =====
    val shayan = listOf(
        CoefRow(23, 1, 12, 0.5),
        CoefRow(23, 2, 12, 1.5), CoefRow(23, 2, 24, 0.55), CoefRow(23, 2, 36, 0.3),
        CoefRow(23, 3, 12, 2.0), CoefRow(23, 3, 24, 0.8), CoefRow(23, 3, 36, 0.5), CoefRow(23, 3, 48, 0.3),
        CoefRow(23, 6, 12, 2.2), CoefRow(23, 6, 24, 1.5), CoefRow(23, 6, 36, 1.1), CoefRow(23, 6, 48, 0.75),
        CoefRow(18, 3, 12, 0.8), CoefRow(18, 3, 24, 0.4),
        CoefRow(18, 6, 12, 1.7), CoefRow(18, 6, 24, 0.75), CoefRow(18, 6, 36, 0.5),
        CoefRow(14, 6, 12, 1.1), CoefRow(14, 6, 24, 0.55),
    )

    // ===== جدول ضرایب نیک وام (۳۲ ردیف — نرخ ۴٪) =====
    val nikvam = listOf(
        CoefRow(4, 1, 12, 0.27), CoefRow(4, 1, 24, 0.15), CoefRow(4, 1, 36, 0.10),
        CoefRow(4, 2, 12, 0.55), CoefRow(4, 2, 24, 0.30), CoefRow(4, 2, 36, 0.20), CoefRow(4, 2, 48, 0.15),
        CoefRow(4, 3, 12, 0.80), CoefRow(4, 3, 24, 0.40), CoefRow(4, 3, 36, 0.30), CoefRow(4, 3, 48, 0.20), CoefRow(4, 3, 60, 0.15),
        CoefRow(4, 4, 12, 1.10), CoefRow(4, 4, 24, 0.55), CoefRow(4, 4, 36, 0.40), CoefRow(4, 4, 48, 0.30), CoefRow(4, 4, 60, 0.20),
        CoefRow(4, 6, 12, 1.60), CoefRow(4, 6, 24, 0.80), CoefRow(4, 6, 36, 0.60), CoefRow(4, 6, 48, 0.45), CoefRow(4, 6, 60, 0.30),
        CoefRow(4, 9, 12, 2.35), CoefRow(4, 9, 24, 1.20), CoefRow(4, 9, 36, 0.80), CoefRow(4, 9, 48, 0.60), CoefRow(4, 9, 60, 0.45),
        CoefRow(4, 12, 12, 3.20), CoefRow(4, 12, 24, 1.60), CoefRow(4, 12, 36, 1.10), CoefRow(4, 12, 48, 0.80), CoefRow(4, 12, 60, 0.60),
    )

    val shayanRates = listOf(14, 18, 23)
    val nikvamRate = 4
    val nikvamAvgOptions = listOf(1, 2, 3, 4, 6, 9, 12)

    fun shayanFor(rate: Int, avg: Int, repay: Int): CoefRow? =
        shayan.firstOrNull { it.rate == rate && it.avg == avg && it.repay == repay }

    fun shayanAvgOptions(rate: Int): List<Int> =
        shayan.filter { it.rate == rate }.map { it.avg }.distinct().sorted()

    fun shayanRepayOptions(rate: Int, avg: Int): List<Int> =
        shayan.filter { it.rate == rate && it.avg == avg }.map { it.repay }.distinct().sorted()

    fun nikvamFor(avg: Int, repay: Int): CoefRow? =
        nikvam.firstOrNull { it.avg == avg && it.repay == repay }

    fun nikvamRepayOptions(avg: Int): List<Int> =
        nikvam.filter { it.avg == avg }.map { it.repay }.distinct().sorted()
}
