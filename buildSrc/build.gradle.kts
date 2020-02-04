plugins {
    `kotlin-dsl`
    kotlin("jvm") version embeddedKotlinVersion
}

repositories {
    jcenter()
    gradlePluginPortal()
}

dependencies {
    implementation(gradleApi())
    implementation("gradle.plugin.com.techshroom:incise-blue:0.5.6")
    implementation(kotlin("gradle-plugin", version = "1.3.61"))
}
