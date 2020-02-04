import com.techshroom.inciseblue.invoke

plugins {
    application
}

dependencies {
    "api"(libs.okio())
    "api"(libs.kotlin("reflect"))
    "api"(libs.kotlinxCoroutines("jdk8"))
    "api"(libs.kotlinxCoroutines("core"))

    "api"(libs.guava())

    "api"(project(":config"))
    "api"(project(":util"))

    "implementation"(libs.dagger())
    "kapt"(libs.dagger("compiler"))
}

application.mainClassName = "net.octyl.backup.core.main.MainKt"
