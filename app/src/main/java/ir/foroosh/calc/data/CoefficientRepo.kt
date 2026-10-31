package ir.foroosh.calc.data

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

/**
 * ذخیره‌سازی ماندگار جداول ضرایب ویرایش‌شده.
 * مقادیر به‌صورت JSON در SharedPreferences نگهداری می‌شوند؛ در نبودِ داده ذخیره‌شده،
 * مقادیر پیش‌فرض اکسل استفاده می‌شود. «بازگردانی به پیش‌فرض» یعنی حذف JSON ذخیره‌شده.
 */
object CoefficientRepo {

    private const val KEY_SHAYAN = "coef_shayan_json"
    private const val KEY_NIKVAM = "coef_nikvam_json"

    private lateinit var sp: SharedPreferences

    /** در Application.onCreate فراخوانی شود */
    fun init(context: Context) {
        sp = context.applicationContext.getSharedPreferences("foroosh_prefs", Context.MODE_PRIVATE)
        Coefficients.shayan = decode(sp.getString(KEY_SHAYAN, null)) ?: Coefficients.shayanDefaultsList()
        Coefficients.nikvam = decode(sp.getString(KEY_NIKVAM, null)) ?: Coefficients.nikvamDefaultsList()
    }

    fun saveShayan(rows: List<CoefRow>) {
        Coefficients.shayan = rows
        sp.edit().putString(KEY_SHAYAN, encode(rows)).apply()
    }

    fun saveNikvam(rows: List<CoefRow>) {
        Coefficients.nikvam = rows
        sp.edit().putString(KEY_NIKVAM, encode(rows)).apply()
    }

    /** بازگردانی جدول شایان به پیش‌فرض اکسل و حذف مقادیر ذخیره‌شده */
    fun resetShayan() {
        Coefficients.shayan = Coefficients.shayanDefaultsList()
        sp.edit().remove(KEY_SHAYAN).apply()
    }

    /** بازگردانی جدول نیک وام به پیش‌فرض اکسل و حذف مقادیر ذخیره‌شده */
    fun resetNikvam() {
        Coefficients.nikvam = Coefficients.nikvamDefaultsList()
        sp.edit().remove(KEY_NIKVAM).apply()
    }

    private fun encode(rows: List<CoefRow>): String {
        val arr = JSONArray()
        rows.forEach {
            arr.put(
                JSONObject()
                    .put("r", it.rate)
                    .put("a", it.avg)
                    .put("p", it.repay)
                    .put("v", it.pct)
            )
        }
        return arr.toString()
    }

    private fun decode(json: String?): List<CoefRow>? = try {
        if (json.isNullOrBlank()) null
        else {
            val arr = JSONArray(json)
            (0 until arr.length())
                .map { arr.getJSONObject(it) }
                .map { CoefRow(it.getInt("r"), it.getInt("a"), it.getInt("p"), it.getDouble("v")) }
                .takeIf { it.isNotEmpty() }
        }
    } catch (e: Exception) {
        null
    }
}
