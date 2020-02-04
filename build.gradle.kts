import com.techshroom.inciseblue.InciseBluePlugin
import com.techshroom.inciseblue.invoke
import org.jetbrains.kotlin.gradle.internal.Kapt3GradleSubplugin
import org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("com.techshroom.incise-blue")
}

inciseBlue {
    ide()
}

subprojects {
    apply<InciseBluePlugin>()
    apply<KotlinPluginWrapper>()
    apply<Kapt3GradleSubplugin>()
    inciseBlue {
        ide()
        license()
        util {
            javaVersion = JavaVersion.VERSION_13
            enableJUnit5()
        }
    }
    tasks.withType<KotlinCompile>().configureEach {
        kotlinOptions.freeCompilerArgs = listOf(
            "-Xuse-experimental=kotlinx.coroutines.FlowPreview",
            "-Xuse-experimental=kotlinx.coroutines.ExperimentalCoroutinesApi"
        )
    }
    repositories {
        maven("https://dl.bintray.com/kotlin/kotlinx/") {
            name = "KotlinX"
        }
    }
    dependencies {
        "implementation"(libs.kotlin("stdlib-jdk8"))

        "testImplementation"(libs.junitJupiter("api"))
        "testRuntimeOnly"(libs.junitJupiter("engine"))
    }
}
