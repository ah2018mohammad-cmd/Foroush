package ir.foroosh.calc.data

import android.content.Context
import androidx.biometric.BiometricManager
import java.security.MessageDigest
import java.security.SecureRandom

/** حالت تم برنامه */
enum class ThemeMode(val label: String) {
    SYSTEM("سیستمی"), WHITE("سفید"), GRAY("خاکستری")
}

/** ذخیره‌سازی تنظیمات: تم، رمز عددی (هش‌شده با نمک) و بیومتریک */
class SettingsStore(context: Context) {

    private val appContext = context.applicationContext
    private val sp = appContext.getSharedPreferences("foroosh_prefs", Context.MODE_PRIVATE)

    // ---------- تم ----------
    var themeMode: ThemeMode
        get() = ThemeMode.entries.getOrElse(sp.getInt("theme", 0)) { ThemeMode.SYSTEM }
        set(v) = sp.edit().putInt("theme", v.ordinal).apply()

    // ---------- رمز ----------
    val pinEnabled: Boolean get() = sp.getBoolean("pin_on", false)
    val biometricEnabled: Boolean get() = sp.getBoolean("bio_on", false)

    fun canBiometric(): Boolean = try {
        BiometricManager.from(appContext)
            .canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK) ==
            BiometricManager.BIOMETRIC_SUCCESS
    } catch (e: Exception) {
        false
    }

    /** تنظیم رمز جدید — هش SHA-256 با نمک تصادفی ذخیره می‌شود (رمز خام هرگز ذخیره نمی‌شود) */
    fun setPin(pin: String) {
        val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }
            .joinToString("") { "%02x".format(it) }
        sp.edit()
            .putString("pin_salt", salt)
            .putString("pin_hash", hash(salt, pin))
            .putBoolean("pin_on", true)
            .apply()
    }

    fun verifyPin(pin: String): Boolean {
        val salt = sp.getString("pin_salt", "") ?: ""
        val stored = sp.getString("pin_hash", "") ?: ""
        return stored.isNotEmpty() && hash(salt, pin) == stored
    }

    fun disablePin() = sp.edit().putBoolean("pin_on", false).putBoolean("bio_on", false).apply()

    // ---------- بیومتریک ----------
    fun setBiometric(on: Boolean) = sp.edit().putBoolean("bio_on", on).apply()

    private fun hash(salt: String, pin: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest((salt + pin).toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
}
