import com.techshroom.inciseblue.invoke

subprojects {
    dependencies {
        "compileOnly"(libs.autoService("annotations"))
        "annotationProcessor"(libs.autoService())
        "implementation"(project(":core"))
    }
}
