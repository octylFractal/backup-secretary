package capnproto

import com.google.gradle.osdetector.OsDetector
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.ProjectLayout
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.Copy
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.dir
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.getByName
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.getPlugin
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.repositories
import org.gradle.plugins.ide.idea.model.IdeaModel

/**
 * A plugin for the Cap'n Proto Java target.
 */
open class CapnProtoPlugin : Plugin<Project> {
    companion object {
        const val TASK_GROUP = "Cap'n Proto"
        const val CAPN_PROTO_EXEC_CONFIG = "capnprotoExecutables"
    }

    override fun apply(target: Project) {
        with(target) {
            apply(plugin = "com.google.osdetector")
            extensions.create<CapnProtoExtension>("capnproto")
            declareCapnProtoDependencies()
            declareCapnProtoTasks()
        }
    }

    private fun Project.declareCapnProtoTasks() {
        val unpackExecutables = tasks.register<Copy>("unpackCapnProtoExecutables") {
            group = TASK_GROUP
            description = "Unpacks Cap'n Proto executables from their JARs"
            into(layout.capnprotoExecutables)
            from({
                files(configurations[CAPN_PROTO_EXEC_CONFIG]).map { zipTree(it) }
            })
        }
        convention.getPlugin<JavaPluginConvention>().sourceSets.configureEach {
            val sourceSet = this
            with(extensions) {
                val extension = create<CapnProtoSourceSetExtension>("capnproto")
                val sourceDir = project.objects.sourceDirectorySet(
                    "capnproto", "${sourceSet.name} Cap'n Proto source"
                )
                sourceDir.srcDir(project.layout.projectDirectory.dir("src/${sourceSet.name}/capnproto"))
                sourceDir.include("**/*.capnp")
                sourceDir.destinationDirectory.convention(
                    project.layout.buildDirectory.dir("generated/source/capnproto/${sourceSet.name}")
                )

                extension.capnproto = sourceDir
                val task = tasks.register<CapnProtoCompile>(sourceSet.getCompileTaskName("capnproto")) {
                    dependsOn(unpackExecutables)
                    group = TASK_GROUP
                    description = "Compiles ${sourceDir.displayName}"
                    sourceFiles = sourceDir
                }
                extension.capnproto.compiledBy(task) { it.outputDirectoryProperty }
                sourceSet.java.srcDir(sourceDir.destinationDirectory)
                sourceSet.allSource.source(sourceDir)
                sourceSet.output.dir(sourceDir.destinationDirectory, "builtBy" to task)
                project.plugins.withId("idea") {
                    afterEvaluate {
                        configure<IdeaModel> {
                            module.generatedSourceDirs.add(sourceDir.destinationDirectory.asFile.get())
                            module.sourceDirs.addAll(sourceDir.srcDirs)
                        }
                    }
                }
            }
        }
    }

    private fun Project.declareCapnProtoDependencies() {
        val classifier = when (val classifier = osClassifier) {
            "linux-x86_64" -> classifier
            else -> throw UnsupportedOperationException("Unsupported OS: $classifier")
        }
        val extension = extensions.getByType<CapnProtoExtension>()
        repositories {
            maven {
                name = "Cap'n Proto Executables"
                url = uri("https://dl.bintray.com/octylfractal/unofficial-capn-proto-executables")
            }
        }
        configurations.register(CAPN_PROTO_EXEC_CONFIG)
        dependencies {
            CAPN_PROTO_EXEC_CONFIG("net.octyl.capnproto:capnproto-exec:${extension.executableVersion}:$classifier")
            CAPN_PROTO_EXEC_CONFIG("net.octyl.capnproto:capnproto-java-exec:${extension.javaExecutableVersion}:$classifier")
            "implementation"("org.capnproto:runtime:${extension.runtimeVersion}")
        }
    }
}

val Project.osClassifier
    get() = extensions.getByName<OsDetector>("osdetector").classifier

val ProjectLayout.capnprotoExecutables
    get() = buildDirectory.dir("capnproto-exec")
