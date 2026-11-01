package ir.foroosh.calc.data

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONObject

/**
 * عناوین قابل ویرایش تسهیلات (بخش «ویرایش تسهیلات و طرح‌های سپرده گذاری» در تنظیمات).
 * عنوان پیش‌فرض «شایان» و «نیک وام» است؛ کاربر می‌تواند آن‌ها را تغییر دهد.
 * (عنوان طرح‌های سپرده در خود PlanRow ذخیره می‌شود.)
 */
object TitlesRepo {

    private const val KEY_TITLES = "titles_json"
    private const val DEFAULT_SHAYAN = "شایان"
    private const val DEFAULT_NIKVAM = "نیک وام"

    private lateinit var sp: SharedPreferences

    var shayan: String = DEFAULT_SHAYAN
        private set
    var nikvam: String = DEFAULT_NIKVAM
        private set

    /** در Application.onCreate فراخوانی شود */
    fun init(context: Context) {
        sp = context.applicationContext.getSharedPreferences("foroosh_prefs", Context.MODE_PRIVATE)
        try {
            val obj = JSONObject(sp.getString(KEY_TITLES, "{}") ?: "{}")
            shayan = obj.optString("shayan", DEFAULT_SHAYAN).ifBlank { DEFAULT_SHAYAN }
            nikvam = obj.optString("nikvam", DEFAULT_NIKVAM).ifBlank { DEFAULT_NIKVAM }
        } catch (e: Exception) {
            shayan = DEFAULT_SHAYAN
            nikvam = DEFAULT_NIKVAM
        }
    }

    /** ذخیره عناوین تسهیلات (خالی بودن = بازگشت به پیش‌فرض) */
    fun save(shayanTitle: String, nikvamTitle: String) {
        shayan = shayanTitle.trim().ifBlank { DEFAULT_SHAYAN }.take(30)
        nikvam = nikvamTitle.trim().ifBlank { DEFAULT_NIKVAM }.take(30)
        persist()
    }

    /** بازگردانی عناوین پیش‌فرض */
    fun reset() {
        shayan = DEFAULT_SHAYAN
        nikvam = DEFAULT_NIKVAM
        persist()
    }

    private fun persist() {
        val obj = JSONObject().put("shayan", shayan).put("nikvam", nikvam)
        sp.edit().putString(KEY_TITLES, obj.toString()).apply()
    }
}
