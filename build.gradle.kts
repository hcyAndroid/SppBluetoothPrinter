// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.1.2" apply false
    id("org.jetbrains.kotlin.android") version "1.8.10" apply false
    id("com.android.library") version "8.1.2" apply false
}
buildscript {
    repositories {
        google()  // Google Maven 仓库
        jcenter() // JCenter Maven 仓库
        maven { url = uri("https://jitpack.io") }
        maven { url = uri("https://zendesk.jfrog.io/zendesk/repo") }
        maven { url = uri("https://zendesk.jfrog.io/ui/native/repo") }
        maven { url = uri("https://zendesk.jfrog.io/artifactory/repo") }
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.1.2")
        //  classpath("io.objectbox:objectbox-gradle-plugin:3.0.0") // Android 插件依赖
    }
}
