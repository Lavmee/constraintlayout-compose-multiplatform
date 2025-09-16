plugins {
    alias(libs.plugins.multiplatform).apply(false)
    alias(libs.plugins.compose).apply(false)
    alias(libs.plugins.android.application).apply(false)
    alias(libs.plugins.android.kotlin.library).apply(false)
    alias(libs.plugins.maven.publish)
}

extra.apply {
    set("jvmTarget", "11")
}
