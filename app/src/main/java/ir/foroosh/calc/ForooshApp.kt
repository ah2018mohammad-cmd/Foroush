package ir.foroosh.calc

import android.app.Application
import android.content.res.Configuration
import ir.foroosh.calc.data.CoefficientRepo
import ir.foroosh.calc.data.PlanRepo
import ir.foroosh.calc.data.TitlesRepo
import java.util.Locale

/** کلاس اپلیکیشن — اعمال زبان فارسی و بازیابی جداول ضرایب، طرح‌ها و عناوین ویرایش‌شده */
class ForooshApp : Application() {

    override fun onCreate() {
        super.onCreate()
        // بارگذاری داده‌های ذخیره‌شده (در نبودِ داده، پیش‌فرض اکسل)
        CoefficientRepo.init(this)
        PlanRepo.init(this)
        TitlesRepo.init(this)
    }

    override fun attachBaseContext(base: android.content.Context) {
        val locale = Locale("fa")
        val config = Configuration(base.resources.configuration)
        config.setLocale(locale)
        super.attachBaseContext(base.createConfigurationContext(config))
    }
}
