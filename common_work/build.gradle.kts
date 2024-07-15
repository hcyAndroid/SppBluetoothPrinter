plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.issyzone.common_work"
    compileSdk = libs.versions.compileSdk.get().toInt()
    buildFeatures {
        dataBinding = true
    }
    defaultConfig {
        minSdkPreview = "TiramisuPrivacySandbox"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        ndk {
            // 设置支持的SO库架构
            abiFilters.add("armeabi")
            // 可以添加更多的架构，如 'x86', 'armeabi-v7a', 'x86_64', 'arm64-v8a'
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    viewBinding {
        enable = true
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation(libs.bundles.androidx)
    implementation(libs.bundles.kotlin)
    api(libs.bugly)
    implementation(libs.glide)
    api(libs.bundles.koin)
    implementation(libs.bundles.retrofit2)

}