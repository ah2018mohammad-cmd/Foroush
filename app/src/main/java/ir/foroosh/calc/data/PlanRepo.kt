package ir.foroosh.calc.data

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

/**
 * ذخیره‌سازی ماندگار طرح‌های سپرده‌گذاری ویرایش‌شده (شیت «جدول طرح ها»).
 * ساختار و منطق مشابه CoefficientRepo؛ در نبودِ داده ذخیره‌شده، پیش‌فرض اکسل استفاده می‌شود.
 */
object PlanRepo {

    private const val KEY_PLANS = "plans_json"

    private lateinit var sp: SharedPreferences

    /** در Application.onCreate فراخوانی شود */
    fun init(context: Context) {
        sp = context.applicationContext.getSharedPreferences("foroosh_prefs", Context.MODE_PRIVATE)
        DepositPlans.plans = decode(sp.getString(KEY_PLANS, null)) ?: DepositPlans.planDefaultsList()
    }

    fun save(rows: List<PlanRow>) {
        DepositPlans.plans = rows
        sp.edit().putString(KEY_PLANS, encode(rows)).apply()
    }

    /** تغییر عنوان یک طرح (بخش «ویرایش تسهیلات و طرح‌های سپرده گذاری») */
    fun rename(index: Int, newName: String) {
        val rows = DepositPlans.plans.toMutableList()
        if (index !in rows.indices || newName.isBlank()) return
        rows[index] = rows[index].copy(name = newName.trim())
        save(rows)
    }

    /** بازگردانی جدول طرح ها به پیش‌فرض اکسل */
    fun reset() {
        DepositPlans.plans = DepositPlans.planDefaultsList()
        sp.edit().remove(KEY_PLANS).apply()
    }

    private fun encode(rows: List<PlanRow>): String {
        val arr = JSONArray()
        rows.forEach {
            arr.put(
                JSONObject()
                    .put("n", it.name)
                    .put("g", it.group)
                    .put("p", it.payNo)
                    .put("d", it.minDeposit)
                    .put("m", it.minMonths)
                    .put("r", it.rate)
                    .put("t", it.note)
                    .put("s", it.shortRate90)
            )
        }
        return arr.toString()
    }

    private fun decode(json: String?): List<PlanRow>? = try {
        if (json.isNullOrBlank()) null
        else {
            val arr = JSONArray(json)
            (0 until arr.length())
                .map { arr.getJSONObject(it) }
                .map {
                    PlanRow(
                        name = it.getString("n"),
                        group = it.optString("g", ""),
                        payNo = it.optInt("p", 0),
                        minDeposit = it.optDouble("d", 0.0),
                        minMonths = it.optInt("m", 1),
                        rate = it.optDouble("r", 0.0),
                        note = it.optString("t", ""),
                        shortRate90 = it.optBoolean("s", false)
                    )
                }
                .takeIf { list -> list.isNotEmpty() }
        }
    } catch (e: Exception) {
        null
    }
}
