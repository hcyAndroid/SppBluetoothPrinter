import org.apache.commons.logging.LogFactory.release

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("maven-publish")
}
android {
    namespace = "com.issyzone.classicblulib"
    compileSdk = libs.versions.compileSdk.get().toInt()


    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }


    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
        }
//        debug {
//            ndk {
//                abiFilters += listOf("armeabi")
//            }
//        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    viewBinding {
        enable = true
    }
    sourceSets["main"].jniLibs.srcDir("libs")

}
//val libsDir = file("libs")
//val arrAar = fileTree(libsDir) {
//    include("feasyblue.jar")
//}
dependencies {
    implementation(libs.core.ktx)
    implementation(libs.appcompat)
    implementation(libs.material)

    //implementation(files(arrAar))
    implementation(libs.bundles.proto3)
    // api(files("libs/feasyblue.jar"))
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.espresso.core)

    //androidTestImplementation(libs.commons.ma)
}

//plugins {
//    id("io.objectbox") version "3.0.0" apply false
//}
//afterEvaluate {
//    publishing {
//        publications {
//            create<MavenPublication>("release") {
//                from(components["release"])
//                groupId = "com.issyzone.jx800r18_sdk"
//                artifactId = "syz-editor"
//                version = "v1.0.0-alpha"
//            }
//        }
//        repositories {
//            maven {
//                url = uri("file:///D:\\BleLib")
//            }
//        }
//    }
//}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                from(components["release"])
                groupId = "com.issyzone.sdk"
                artifactId = "syz-device"
                version = "v1.6.7.14-alpha"
//                artifact("$buildDir/outputs/aar/${project.name}-release.aar")
            }
        }
        repositories {
            maven {
                url = uri("file:///D:\\syz-blu-lib")
            }
        }
    }
//    repositories {
//        maven {
//            name = "JitPack"
//            url = uri("https://jitpack.io/")
//        }
//    }
}








