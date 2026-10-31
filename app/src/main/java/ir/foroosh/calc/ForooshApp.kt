package ir.foroosh.calc

import android.app.Application
import android.content.res.Configuration
import java.util.Locale

/** کلاس اپلیکیشن — اعمال زبان فارسی در کل برنامه */
class ForooshApp : Application() {
    override fun attachBaseContext(base: android.content.Context) {
        val locale = Locale("fa")
        val config = Configuration(base.resources.configuration)
        config.setLocale(locale)
        super.attachBaseContext(base.createConfigurationContext(config))
    }
}
