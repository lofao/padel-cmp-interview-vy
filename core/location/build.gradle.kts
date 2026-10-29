private val moduleNamespace = "io.bifri.interview.cmp.padel.core.location"

plugins {
    alias(libs.plugins.convention.library)
    alias(libs.plugins.convention.library.compose)
    alias(libs.plugins.convention.koin)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.koin.compose)
        }
        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
            implementation(libs.compose.ui)
            implementation(libs.koin.compose)
        }
    }
}

android {
    namespace = moduleNamespace
}
