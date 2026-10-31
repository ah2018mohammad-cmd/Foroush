pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        // آینه‌ای سریع و مطمئن برای Maven Central (رفع خطای موقت 429)
        maven("https://maven.aliyun.com/repository/central")
        mavenCentral()
    }
}
rootProject.name = "Foroosh"
include(":app")
