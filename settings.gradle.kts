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
        mavenCentral()
    }
    versionCatalogs {
        //创建独立文件去管理依赖
        create("libs") {
            from(files("libs.versions.toml"))
        }
    }
}


rootProject.name = "bluetoothPrinter"
include(":app")
include(":BleLibs")
