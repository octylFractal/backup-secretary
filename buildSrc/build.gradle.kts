plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
    kotlin("jvm") version embeddedKotlinVersion
}

repositories {
    jcenter()
    gradlePluginPortal()
}

dependencies {
    implementation(gradleApi())
    implementation("gradle.plugin.com.techshroom:incise-blue:0.5.6")
    implementation("de.undercouch:gradle-download-task:4.0.4")
    implementation("com.google.gradle:osdetector-gradle-plugin:1.6.2")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.3.61") {
        exclude("de.undercouch:gradle-download-task")
    }
}

gradlePlugin {
    plugins {
        create("capnproto") {
            id = "net.octyl.capnproto"
            implementationClass = "capnproto.CapnProtoPlugin"
        }
    }
}
