import com.techshroom.inciseblue.commonLib
import org.gradle.api.Project

class Libs(project: Project) {
    val kotlin = project.commonLib("org.jetbrains.kotlin", "kotlin", "1.3.61")
    val kotlinxCoroutines = project.commonLib("org.jetbrains.kotlinx", "kotlinx-coroutines", "1.3.3")

    val okio = project.commonLib("com.squareup.okio", "okio", "2.4.3")

    val nightConfig = project.commonLib("com.electronwill.night-config", "", "3.6.2")

    val guava = project.commonLib("com.google.guava", "guava", "28.2-jre")

    val dagger = project.commonLib("com.google.dagger", "dagger", "2.26")
    val autoService = project.commonLib("com.google.auto.service", "auto-service", "1.0-rc6")

    val junitJupiter = project.commonLib("org.junit.jupiter", "junit-jupiter", "5.6.0")
}

val Project.libs get() = Libs(this)
